package com.ekyc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ekyc.model.DlRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * This is class for Driving License Authentication response
 * and it is also a component layer to implements logic for
 * authentication response
 * @author Md Arif
 *
 */
@Component
@Slf4j
public class DlAuthResponse {

	@Autowired
	Gson gson;

	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	String status2 = "status";
	String issuedTo2 = "IssuedTo";
	String issueDate2 = "issueDate";
	String photo2 = "Photo";
	String certificateData2 = "CertificateData";
	String drivingLicense2 = "DrivingLicense";
	String categories2 = "Categories";
	String country2 = "country";
	String district2 = "district";
	String state2 = "state";

	/**
	 * This is a method for get driving license response
	 * @param dlRequest
	 * @param object
	 * @return Object
	 */
	public Object getDlResponse(DlRequest dlRequest,Object object) {
		log.info(loggingMessageUtil.getLogMessageSuccess("Get DL method start.", dlRequest));
		String requestId = UUID.randomUUID().toString().substring(0, 32);
		if(object == null) {
			return setDlResponse(requestId);
		}

		JsonObject fromJson = gson.fromJson(gson.toJson(object), JsonObject.class);

		if(checkNullable(fromJson,"Certificate",null)) {
			return setDlResponse(requestId);
		}
		JsonObject asJsonObject = fromJson.get("Certificate").getAsJsonObject();
		
		if(checkNullable(asJsonObject,issuedTo2,"Person")) {
			return setDlResponse(requestId);
		}
		JsonObject person = asJsonObject.get(issuedTo2).getAsJsonObject().get("Person").getAsJsonObject();
	
		String issueDate = getJsonValueByJsonObject(asJsonObject, issueDate2);

		String fatherOrhusband = getJsonValueByJsonObject(person, "swd");

		String name = getJsonValueByJsonObject(person, "name");

		String img = getJsonDataFromManyKey(person,photo2,"content");

		String bloodGroup = "";
		String dob = getJsonValueByJsonObject(person, "dob");

		String dlNumber = getJsonValueByJsonObject(asJsonObject, "number");

		dlNumber = String.join("", dlNumber.split(" "));

		if(!dlRequest.getDlNo().equalsIgnoreCase(dlNumber) || !dlRequest.getDob().equals(dob)) {
			return setDlResponse(requestId);
		}
		String exp = getJsonValueByJsonObject(asJsonObject, "expiryDate");
		List<JsonElement> list = getJsonValueWithManyVariablesByJsonObject(asJsonObject, certificateData2, drivingLicense2, categories2);

		String nonTransport = issueDate + " to "+exp;
		String transport = "";
		String cov = "";

		String addressLine1 = "";
		String state = "";
		String district = "";
		String pin = "";
		String country = "";
		String type = "";
		String completeAddress = "";
		String status = getJsonValueByJsonObject(asJsonObject, status2);
		String from = "";
		String to = "";
		String remarks = "";

		String initialIssuingOffice = getJsonValueByJsonObject(asJsonObject, "issuedAt");
		String lastEndorsementDate = issueDate;
		String lastEndorsedOffice = initialIssuingOffice;
		String endorsementReason = "ISSUE OF DRIVING LICENCE";
		String hazardousValidTill = "NA";
		String hillValidTill = "NA";
		int statusCode = Integer.parseInt("101");

		String[] adrs = new String[]{"Address","Address2"};

		JsonObject dlResponse = new JsonObject();

		dlResponse.addProperty("requestId", requestId);

		JsonObject result = new JsonObject();

		result.addProperty(issueDate2, issueDate);
		result.addProperty("father/husband", fatherOrhusband);
		result.addProperty("name", name);
		result.addProperty("img", img);
		result.addProperty("bloodGroup", bloodGroup);
		result.addProperty("dob", dob);
		result.addProperty("dlNumber", dlNumber);

		JsonObject validity = new JsonObject();

		validity.addProperty("nonTransport", nonTransport);
		validity.addProperty("transport", transport);

		result.add("validity", validity);

		JsonArray covDetailsArray = new JsonArray();

		if(list != null && !list.isEmpty()) {

			for(JsonElement js:list) {
				JsonObject covDetails = new JsonObject();
				cov = getJsonvalueByElement(js, "abbreviation");
				covDetails.addProperty("cov", cov);
				covDetails.addProperty(issueDate2, issueDate);
				covDetailsArray.add(covDetails);
			}
		}

		result.add("covDetails", covDetailsArray);

		JsonArray addressArray = new JsonArray();

		for(String str :adrs) {
			JsonObject address = new JsonObject();

			JsonObject address2 = person != null && person.get(str) != null ? person.get(str).getAsJsonObject():null;

			addressLine1 = getJsonValueByJsonObject(address2, "line1");

			state = getJsonValueByJsonObject(address2, state2);

			district = getJsonValueByJsonObject(address2, district2);

			pin = getJsonValueByJsonObject(address2, "pin");

			country = getJsonValueByJsonObject(address2, country2);

			completeAddress = addressLine1 + ", " + district + ", " + state + ", " + country + ", " + pin;

			type = getJsonValueByJsonObject(address2, "type");

			address.addProperty("addressLine1", addressLine1);
			address.addProperty(state2, state);
			address.addProperty(district2, district);
			address.addProperty("pin", pin);
			address.addProperty("completeAddress", completeAddress);
			address.addProperty(country2, country);
			address.addProperty("type", type);
			addressArray.add(address);
		}
		result.add("address", addressArray);
		result.addProperty(status2, status);

		JsonObject statusDetails = new JsonObject();

		statusDetails.addProperty("from", from);
		statusDetails.addProperty("to", to);
		statusDetails.addProperty("remarks", remarks);
		result.add("statusDetails", statusDetails);

		if(dlRequest.isAdditionalDetails()) {

			JsonObject endorsementAndHazardousDetails = new JsonObject();

			endorsementAndHazardousDetails.addProperty("initialIssuingOffice", initialIssuingOffice);
			endorsementAndHazardousDetails.addProperty("lastEndorsementDate", lastEndorsementDate);
			endorsementAndHazardousDetails.addProperty("lastEndorsedOffice", lastEndorsedOffice);
			endorsementAndHazardousDetails.addProperty("endorsementReason", endorsementReason);
			endorsementAndHazardousDetails.addProperty("hazardousValidTill", hazardousValidTill);
			endorsementAndHazardousDetails.addProperty("hillValidTill", hillValidTill);

			result.add("endorsementAndHazardousDetails", endorsementAndHazardousDetails);
		}

		dlResponse.add("result", result);
		dlResponse.addProperty("statusCode", statusCode);

		log.info(loggingMessageUtil.getLogMessageSuccess("Dl response set successfully.", dlResponse));

		return gson.fromJson(dlResponse, Object.class);
	}

	private boolean checkNullable(JsonObject fromJson, String key,String key2) {
		log.info(fromJson.toString());
		if(fromJson.isJsonNull() || fromJson.get(key) == null) {
			return true;
		}
		else if(key2 != null && (fromJson.get(key).getAsJsonObject() == null || fromJson.get(key).getAsJsonObject().get(key2) == null)) {
			return true;
		}
			return false;
	}

	private String getJsonDataFromManyKey(JsonObject person, String photo22, String string) {
		return  person != null && person.get(photo22) != null && person.get(photo22).getAsJsonObject().get(string) != null ? person.get(photo22).getAsJsonObject().get(string).getAsString() :"";
	}

	private List<JsonElement> getJsonValueWithManyVariablesByJsonObject(JsonObject asJsonObject,
			String certificateData22, String drivingLicense22, String categories22) {

		List<JsonElement> list = new ArrayList<>();
		JsonObject jsonObject2 = new JsonObject();
		if(asJsonObject.get(certificateData2) != null && asJsonObject.get(certificateData2).getAsJsonObject().get(drivingLicense2) != null 
				&& asJsonObject.get(certificateData2).getAsJsonObject().get(drivingLicense2).getAsJsonObject().get(categories2) != null) {
			jsonObject2 = asJsonObject.get(certificateData22).getAsJsonObject().get(drivingLicense22).getAsJsonObject().get(categories22).getAsJsonObject();
		}

		if(jsonObject2 != null && jsonObject2.get("Category") != null) {
			JsonElement jsonElement = jsonObject2.get("Category");
			if(jsonElement.isJsonArray()) {
				list = jsonElement.getAsJsonArray().asList();
			}
			else {
				list.add(jsonElement.getAsJsonObject());
			}
		}

		return list;
	}

	/**
	 * This is method for set driving license response
	 * if some data is missing in authentication or some
	 * issue occur during getting data.
	 * @param substring
	 * @return Object
	 */
	private Object setDlResponse(String substring) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("requestId", substring);
		jsonObject.add("result", null);
		jsonObject.addProperty("statusCode", Integer.parseInt("102"));

		return gson.fromJson(jsonObject, Object.class);
	}

	private String getJsonvalueByElement(JsonElement element, String key) {
		return element != null && element.getAsJsonObject().get(key) != null?element.getAsJsonObject().get(key).getAsString():"";
	}

	private String getJsonValueByJsonObject(JsonObject jsonObject, String key) {
		return jsonObject != null && jsonObject.get(key) != null ? jsonObject.get(key).getAsString() : "";
	}
}
