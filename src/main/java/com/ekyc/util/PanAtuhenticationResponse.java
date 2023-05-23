package com.ekyc.util;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ekyc.model.PanRequest;
import com.ekyc.model.common.PanResponse;
import com.ekyc.model.common.Result;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PanAtuhenticationResponse {
	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	@Autowired
	Gson gson;

	@Value("${pan.active}")
	String active;

	String issuedTo = "IssuedTo";

	public PanResponse getPancardResponse(Object object,PanRequest panRequest) {
		loggingMessageUtil.getLogMessageSuccess("Get Pan Response method.", panRequest);
		JsonObject fromJson2 = gson.fromJson(gson.toJson(object), JsonObject.class);

		if(fromJson2 == null || fromJson2.get("Certificate") == null) {
			return setErrorPanResponse();
		}
		JsonObject asJsonObject = fromJson2.get("Certificate").getAsJsonObject();

		if(asJsonObject == null || asJsonObject.get("number") == null) {
			return setErrorPanResponse();
		}
		String pan = asJsonObject.get("number").getAsString();
		log.info(loggingMessageUtil.getLogMessageSuccess("Pan number.", pan));

		String status = getJsonValueByJsonObject(asJsonObject,"status");

		log.info(loggingMessageUtil.getLogMessageSuccess("Get status successfully.", status));

		if(asJsonObject.get(issuedTo) == null || asJsonObject.get(issuedTo).getAsJsonObject().get("Person") == null) {
			return setErrorPanResponse();
		}

		JsonObject personObject = asJsonObject.get(issuedTo).getAsJsonObject().get("Person").getAsJsonObject();

		String name = getJsonValueByJsonObject(personObject,"name");

		log.info(loggingMessageUtil.getLogMessageSuccess("Name of customer.", name));

		String dob = getJsonValueByJsonObject(personObject,"dob");

		log.info(loggingMessageUtil.getLogMessageSuccess("Dob of customer.", dob));

		return checkPan(panRequest,pan,name,dob,status);
	}

	private PanResponse checkPan(PanRequest panRequest, String pan, String name, String dob, String status) {
		log.info(loggingMessageUtil.getLogMessageSuccess("Check Pan method start.", pan+"  "+name+"  "+dob));

		if(dob != null && !dob.isEmpty()) {
			List<String> asList = Arrays.asList(dob.split("-"));
			dob = String.join("/", asList);
		}

		String requestId = UUID.randomUUID().toString().substring(0, 36);
		log.info(loggingMessageUtil.getLogMessageSuccess("Formated dob ", dob));

		PanResponse panResponse = null;
		if(panRequest.getPan().equals(pan) && panRequest.getDob().equals(dob) 
				&& panRequest.getName().equalsIgnoreCase(name) && status.equalsIgnoreCase("A")) {

			panResponse = setPanAuthResponse(requestId,true,false,true,active,"101");
			log.info(loggingMessageUtil.getLogMessageSuccess(null, panResponse));

		}
		else if(panRequest.getPan().equals(pan) &&
				panRequest.getName().equalsIgnoreCase(name) && status.equalsIgnoreCase("A")) {

			panResponse = setPanAuthResponse(requestId,false,false,true,active,"101");

			log.info(loggingMessageUtil.getLogMessageSuccess(null, panResponse));
		}
		else if((panRequest.getPan().equals(pan) && 
				panRequest.getDob().equals(dob) && status.equalsIgnoreCase("A")) || (panRequest.getPan().equals(pan) && panRequest.getDob().equals(dob))) {

			panResponse = setPanAuthResponse(requestId,true,false,false,active,"101");

			log.info(loggingMessageUtil.getLogMessageSuccess(null, panResponse));
		}
		else if(panRequest.getPan().equals(pan)) {

			panResponse = setPanAuthResponse(requestId,false,false,false,active,"101");

			log.info(loggingMessageUtil.getLogMessageSuccess(null, panResponse));
		}
		else {
			panResponse = setErrorPanResponse();
			log.info(loggingMessageUtil.getLogMessageSuccess(null, panResponse));
		}

		return panResponse;
	}
	
	private PanResponse setErrorPanResponse() {
		String requestId = UUID.randomUUID().toString().substring(0, 36);
		return PanResponse.builder().requestId(requestId).statusCode("102").build();
	}

	private PanResponse setPanAuthResponse(String requestId, boolean dob,
			boolean duplicate, boolean nameM, String active2, String status) {

		return PanResponse.builder()
				.requestId(requestId)
				.statusCode(status)
				.result(Result.builder()
						.dobMatch(dob)
						.duplicate(duplicate)
						.nameMatch(nameM)
						.status(active2)
						.build())
				.build();
	}

	private String getJsonValueByJsonObject(JsonObject jsonObject, String key) {
		return jsonObject != null && jsonObject.get(key) != null ? jsonObject.get(key).getAsString() : "";
	}
}
