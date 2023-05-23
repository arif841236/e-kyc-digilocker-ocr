package com.ekyc.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ekyc.model.RationCardRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RationCardResponse {

	@Autowired
	Gson gson;

	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	String requestId2 = "requestId";
	String certificate2 = "Certificate";
	String certificateData2 = "CertificateData";
	String rationCard2 = "RationCard";
	String number2 = "number";
	String name2 = "name";
	String address2 = "address";
	String address3 = "Address";
	String dealerInfo2 = "dealerInfo";
	String issuedTo2 = "IssuedTo";
	String person2 = "Person";
	String gender2 = "gender";
	String dob2 = "dob";
	String age2 = "age";
	String house2 = "house";
	String line2 = "line1";
	String state2 = "state";
	String country2 = "country";
	String pin2 = "pin";
	String district2 = "district";
	String vtc2 = "vtc";
	String personInfo2 = "personInfo";
	String fairPriceShop2 = "FairPriceShop";
	String status2 = "status";

	public Object getRationcardResponse(RationCardRequest cardRequest,Object object,String rationNumber) {

		log.info(loggingMessageUtil.getLogMessageSuccess("Get Ration card Response method start.", object));
		String requestId = UUID.randomUUID().toString().substring(0, 32);

		if(rationNumber == null || rationNumber.isEmpty()) {
			return setRationResponse(requestId,null,null);
		}
		else {
			if(!rationNumber.equals(cardRequest.getRationNumber())) {
				return setRationResponse(requestId,null,null);
			}
		}

		if(object == null) {
			return setRationResponse(requestId,null,null);
		}

		JsonObject fromJson = gson.fromJson(gson.toJson(object), JsonObject.class);

		if(fromJson == null || fromJson.get(certificate2) == null) {
			return setRationResponse(requestId,null,null);
		}
		JsonObject crt = fromJson.get(certificate2).getAsJsonObject();

		if(crt.get(certificateData2)!=null) {

			return setRationData1(crt,requestId);
		}
		else {
			return setRationData2(crt,requestId);
		}
	}

	private Object setRationData1(JsonObject crt, String requestId) {

		JsonObject rationResponse = new JsonObject();
		rationResponse.addProperty(requestId2, requestId);
		JsonObject asJsonObject = crt.get(certificateData2).getAsJsonObject();
		JsonObject dealer = new JsonObject();

		if(asJsonObject.get(rationCard2) == null || asJsonObject.get(rationCard2).getAsJsonObject().get(fairPriceShop2) == null) {
			dealer = null;
		}
		else {
			JsonObject dealerData = asJsonObject.get(rationCard2).getAsJsonObject().get(fairPriceShop2).getAsJsonObject();

			String dlNumber = getJsonValueByJsonObject(dealerData, address2);
			String dlName = getJsonValueByJsonObject(dealerData, name2);
			String dlAddress = getJsonValueByJsonObject(dealerData, address2);
			log.info(dlNumber);
			dealer.addProperty(name2, dlName);
			dealer.addProperty(number2, dlNumber);
			dealer.addProperty(address2, dlAddress);
		}
		rationResponse.add(dealerInfo2, dealer);

		JsonElement jsonElement = crt.get(issuedTo2);

		if(jsonElement == null || jsonElement.getAsJsonObject().get(person2) == null) {
			return setRationResponse(requestId,dealer,null);
		}
		List<JsonElement> asJsonArray = new ArrayList<>();

		if(jsonElement.getAsJsonObject().get(person2).isJsonArray()) {
			asJsonArray = jsonElement.getAsJsonObject().get(person2).getAsJsonArray().asList();
		}
		else {
			asJsonArray.add(jsonElement.getAsJsonObject().get(person2).getAsJsonObject());
		}

		JsonArray personInfo = new JsonArray();

		for(JsonElement i:asJsonArray) {
			JsonObject pers = new JsonObject();

			String name = getJsonvalueByElement(i,name2);
			String dob = getJsonvalueByElement(i,dob2);
			String age = getJsonvalueByElement(i,age2);
			String gender = getJsonvalueByElement(i,gender2);

			if(!dob.isEmpty() && age.isEmpty()) {
				log.info(dob);
				String[] split = dob.split("-");
				String str1 = split[0];
				split[0] = split[2];
				split[2]=str1;
				String input = String.join("-", split);
				LocalDate dateTime = LocalDate.parse( input ) ;
				int years = Period.between(dateTime, LocalDate.now()).getYears();
				age = ""+years;
				log.info(age);
			}

			pers.addProperty(name2, name);
			pers.addProperty(dob2, dob);
			pers.addProperty(age2, age);
			pers.addProperty(gender2, gender);

			JsonElement jsonElement2 = i.getAsJsonObject().get(address3);
			JsonObject addrs = new JsonObject();
			if(jsonElement2 == null) {
				addrs = null;
			}
			else {
				String house = getJsonvalueByElement(jsonElement2,house2);
				String line1 = getJsonvalueByElement(jsonElement2,line2);
				String vtc = getJsonvalueByElement(jsonElement2,vtc2);
				String district = getJsonvalueByElement(jsonElement2,district2);
				String pin = getJsonvalueByElement(jsonElement2,pin2);
				String state = getJsonvalueByElement(jsonElement2,state2);
				String country = getJsonvalueByElement(jsonElement2,country2);
				addrs.addProperty(house2, house);
				addrs.addProperty(line2, line1);
				addrs.addProperty(vtc2, vtc);
				addrs.addProperty(district2, district);
				addrs.addProperty(pin2, pin);
				addrs.addProperty(state2, state);
				addrs.addProperty(country2, country);
			}

			pers.add(address2, addrs);

			personInfo.add(pers);
		}

		rationResponse.add(personInfo2, personInfo);
		rationResponse.addProperty(status2, "Active");
		return gson.fromJson(rationResponse, Object.class);
	}

	private Object setRationData2(JsonObject crt, String requestId) {

		if(crt.get(issuedTo2) == null ) {
			return setRationResponse(requestId,null,null);
		}

		JsonObject issuedTo = crt.getAsJsonObject().get(issuedTo2).getAsJsonObject();

		if(issuedTo.get(certificateData2) == null) {
			return setRationResponse(requestId,null,null);
		}
		JsonObject asJsonObject = issuedTo.get(certificateData2).getAsJsonObject();

		if(asJsonObject.get(rationCard2) == null || asJsonObject.get(rationCard2).getAsJsonObject().get(fairPriceShop2) == null) {
			return setRationResponse(requestId,null,null);
		}
		JsonObject dealerData = asJsonObject.get(rationCard2).getAsJsonObject().get(fairPriceShop2).getAsJsonObject();

		String dlNumber = getJsonvalueByElement(dealerData,number2);
		String dlName = getJsonvalueByElement(dealerData,name2);
		String dlAddress = getJsonvalueByElement(dealerData,address2);

		JsonObject rationResponse = new JsonObject();
		JsonObject dealer = new JsonObject();
		rationResponse.addProperty(requestId2, requestId);

		dealer.addProperty(name2, dlName);
		dealer.addProperty(number2, dlNumber);
		dealer.addProperty(address2, dlAddress);

		rationResponse.add(dealerInfo2, dealer);

		if(issuedTo.get(person2) == null) {
			return setRationResponse(requestId,dealer,null);
		}

		JsonObject persion1 = issuedTo.get(person2).getAsJsonObject();
		JsonArray personInfo = new JsonArray();

		JsonObject pers = new JsonObject();
		String name = getJsonvalueByElement(persion1,name2);
		String dob = getJsonvalueByElement(persion1,dob2);
		String age = getJsonvalueByElement(persion1,age2);
		String gender = getJsonvalueByElement(persion1,gender2);
		if(!dob.isEmpty() && age.isEmpty()) {

			String[] split = dob.split("-");
			String str1 = split[0];
			split[0] = split[2];
			split[2]=str1;
			String input = String.join("-", split);
			LocalDate dateTime = LocalDate.parse( input ) ;
			int years = Period.between(dateTime, LocalDate.now()).getYears();
			age = ""+years;
			log.info(age);
		}

		pers.addProperty(name2, name);
		pers.addProperty(dob2, dob);
		pers.addProperty(age2, age);
		pers.addProperty(gender2, gender);

		pers.add(address2, null);

		personInfo.add(pers);
		rationResponse.add(personInfo2, personInfo);
		rationResponse.addProperty(status2, "Active");

		return gson.fromJson(rationResponse, Object.class);
	}

	private Object setRationResponse(String substring, JsonObject dealer, JsonObject object) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(requestId2, substring);
		jsonObject.add(dealerInfo2, dealer);
		jsonObject.add(personInfo2, object);
		jsonObject.addProperty("statusCode", Integer.parseInt("102"));

		return gson.fromJson(jsonObject, Object.class);
	}

	public String getJsonvalueByElement(JsonElement element, String key) {
		return element.getAsJsonObject().get(key) != null?element.getAsJsonObject().get(key).getAsString():"";
	}

	public String getJsonValueByJsonObject(JsonObject jsonObject, String key) {
		return jsonObject.get(key) != null ? jsonObject.get(key).getAsString() : "";
	}
}
