package com.ekyc.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ekyc.model.AadhaarRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AadhaarResponse {

	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	@Autowired
	Gson gson;

	String country2 = "country";
	String state2 = "state";
	String gender2 = "gender";
	String signature2 = "Signature";
	String kycRes = "KycRes";

	public Object getAadhaarResponse(AadhaarRequest aadhaarRequest,Object object) {

		String substring = UUID.randomUUID().toString().substring(0, 36);

		if(object == null) {
			return setAadharResponse(substring);
		}

		log.info(loggingMessageUtil.getLogMessageSuccess("getAadhaarResponse method start.", aadhaarRequest));
		JsonObject fromJson = gson.fromJson(gson.toJson(object), JsonObject.class);

		log.info(loggingMessageUtil.getLogMessageSuccess("Get json data successfully.", fromJson));

		String certificate = "Certificate";

		if(fromJson == null ||fromJson.get(certificate) == null) {
			return setAadharResponse(substring);
		}

		JsonElement certificateData = fromJson.get(certificate).getAsJsonObject().get("CertificateData");
		if(certificateData == null) {
			return setAadharResponse(substring);
		}

		String sign = fromJson.get(certificate).getAsJsonObject().get(signature2) != null 
				&& fromJson.get(certificate).getAsJsonObject()
				.get(signature2).getAsJsonObject()
				.get("SignatureValue") != null ?fromJson.get(certificate).getAsJsonObject()
						.get(signature2).getAsJsonObject()
						.get("SignatureValue").getAsString() : "";

		if(certificateData.getAsJsonObject().get(kycRes) == null) {
			return setAadharResponse(substring);
		}

		if(certificateData.getAsJsonObject().get(kycRes).getAsJsonObject().get("UidData") == null) {
			return setAadharResponse(substring);
		}

		JsonObject asJsonObject = certificateData.getAsJsonObject().get(kycRes).getAsJsonObject().get("UidData").getAsJsonObject();
		log.info(loggingMessageUtil.getLogMessageSuccess("Get Json data successfully.", asJsonObject));

		if(asJsonObject == null) {
			return setAadharResponse(substring);
		}

		if(asJsonObject.get("Poa") == null) {
			return setAadharResponse(substring);
		}
		JsonObject poa = asJsonObject.get("Poa").getAsJsonObject();
		log.info(loggingMessageUtil.getLogMessageSuccess("Get proof of address data successfully.", poa));

		if(asJsonObject.get("Poi") == null) {
			return setAadharResponse(substring);
		}
		JsonObject poi = asJsonObject.get("Poi").getAsJsonObject();
		log.info(loggingMessageUtil.getLogMessageSuccess("Get proof of identity data successfully.", poi));

		if(asJsonObject.get("uid") == null) {
			return setAadharResponse(substring);
		}

		String maskedAadhaarNumber = asJsonObject.get("uid").getAsString();
		log.info(loggingMessageUtil.getLogMessageSuccess("Get aadhaar number successfully.", maskedAadhaarNumber));

		if(!maskedAadhaarNumber.contains(aadhaarRequest.getAadhaarNo().substring(8))) {
			log.info(maskedAadhaarNumber);
			return setAadharResponse(substring);
		}

		String name = getJsonValueByJsonObject(poi, "name");

		String dob = getJsonValueByJsonObject(poi, "dob");

		if(!dob.isEmpty()) {

			String[] split = dob.split("-");
			log.info(dob);
			String year = split[2];
			split[2] = split[0];
			split[0] = year;
			dob = String.join("-", split);
			log.info(dob);
		}

		String gender = getJsonValueByJsonObject(poi, gender2);

		String fatherName = getJsonValueByJsonObject(poa, "co");

		String houseNumber = 	getJsonValueByJsonObject(poa, "house");

		String streets = getJsonValueByJsonObject(poa, "street");

		String location = getJsonValueByJsonObject(poa, "loc");

		String district = getJsonValueByJsonObject(poa, "dist");

		log.info(district);
		String vtcName = getJsonValueByJsonObject(poa, "vtc");
		String state = 	getJsonValueByJsonObject(poa, state2);

		String country = getJsonValueByJsonObject(poa, country2);

		log.info(country);
		String pin = toCheckPinCode(poa,"pc");

		String combinedAddress = houseNumber+", "+streets+", "+ vtcName +", " +location+", " +district +", "+state +", "+country +", "+pin;
		log.info(combinedAddress);

		String image = getJsonValueByJsonObject(asJsonObject, "Pht");

		String xmlContent = sign;
		String message = "Aadhaar XML file downloaded successfully";
		String relativeName = "";
		String houseName = "";

		JsonObject adhaarResponse = new JsonObject();

		adhaarResponse.addProperty("requestId", substring);

		JsonObject result = new JsonObject();

		JsonObject dataFromAadhaar = new JsonObject();
		dataFromAadhaar.addProperty("generatedDateTime", LocalDateTime.now().toString());
		dataFromAadhaar.addProperty("maskedAadhaarNumber", maskedAadhaarNumber);
		dataFromAadhaar.addProperty("name", name);
		dataFromAadhaar.addProperty("dob", dob);
		dataFromAadhaar.addProperty("gender", gender);
		dataFromAadhaar.addProperty("mobileHash", "");
		dataFromAadhaar.addProperty("emailHash", "");
		dataFromAadhaar.addProperty("fatherName", fatherName);
		dataFromAadhaar.addProperty("relativeName", relativeName);
		dataFromAadhaar.addProperty("husbandName", houseName);

		JsonObject address = new JsonObject();

		JsonObject splitAddress = new JsonObject();
		splitAddress.addProperty("houseNumber", houseNumber);
		splitAddress.addProperty("street", streets);
		splitAddress.addProperty("landmark", "");
		splitAddress.addProperty("subdistrict", "");
		splitAddress.addProperty("district", district);
		splitAddress.addProperty("vtcName", vtcName);
		splitAddress.addProperty("location", location);
		splitAddress.addProperty("postOffice", "");
		splitAddress.addProperty("state", state);
		splitAddress.addProperty("country", country);
		splitAddress.addProperty("pincode", pin);

		address.add("splitAddress", splitAddress);
		address.addProperty("combinedAddress", combinedAddress);

		dataFromAadhaar.add("address", address);
		dataFromAadhaar.addProperty("image", image);

		JsonObject file = new JsonObject();
		file.addProperty("xmlContent", xmlContent);
		file.addProperty("pdfContent", "");

		dataFromAadhaar.add("file", file);

		result.add("dataFromAadhaar", dataFromAadhaar);

		adhaarResponse.add("result", result);
		result.addProperty("message", message);
		result.addProperty("shareCode", "4093");

		adhaarResponse.addProperty("statusCode", Integer.parseInt("101"));
		log.info(loggingMessageUtil.getLogMessageSuccess("Aadhaar response set successfully.", adhaarResponse));

		return gson.fromJson(adhaarResponse, Object.class);
	}

	private String toCheckPinCode(JsonObject poa, String string) {
		BigDecimal pincode = poa.get(string) != null ? poa.get(string).getAsBigDecimal() : null;

		return pincode != null ? pincode.toString().substring(0, 6) :"";
	}

	private Object setAadharResponse(String substring) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("requestId", substring);
		jsonObject.add("result", null);
		jsonObject.addProperty("statusCode", Integer.parseInt("102"));

		return gson.fromJson(jsonObject, Object.class);
	}

	private String getJsonValueByJsonObject(JsonObject jsonObject, String key) {
		return jsonObject.get(key) != null ? jsonObject.get(key).getAsString() : "";
	}
}
