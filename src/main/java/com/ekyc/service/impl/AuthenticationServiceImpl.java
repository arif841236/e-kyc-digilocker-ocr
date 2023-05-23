package com.ekyc.service.impl;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ekyc.exception.KycException;
import com.ekyc.model.AadhaarRequest;
import com.ekyc.model.DlRequest;
import com.ekyc.model.PanRequest;
import com.ekyc.model.RationCardRequest;
import com.ekyc.model.common.PanResponse;
import com.ekyc.service.IDigilockerService;
import com.ekyc.service.IAuthenticationService;
import com.ekyc.util.AadhaarResponse;
import com.ekyc.util.DlAuthResponse;
import com.ekyc.util.LoggingMessageUtil;
import com.ekyc.util.PanAtuhenticationResponse;
import com.ekyc.util.RationCardResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {

	@Autowired
	IDigilockerService digilockerService;

	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	@Autowired
	Gson gson;

	@Autowired
	AadhaarResponse aadhaarResponse;

	@Autowired
	DlAuthResponse authResponse;

	@Autowired
	RationCardResponse cardResponse;

	@Autowired
	PanAtuhenticationResponse panAtuhenticationResponse;

	@Value("${consent.error}")
	String consentError;

	@Value("${consent.error2}")
	String consentError2;

	String items = "items";
	String doctype = "doctype";

	@Override
	public PanResponse panAuthentication(PanRequest panRequest, String token) throws IOException {
		log.info(loggingMessageUtil.getLogMessageSuccess("panAuthentication method start", panRequest));

		if(!panRequest.getConsent().equalsIgnoreCase("y") && !panRequest.getConsent().equalsIgnoreCase("n") ) {
			throw new KycException(consentError);
		}

		if(panRequest.getConsent().equalsIgnoreCase("n")) {
			throw new KycException(consentError2+panRequest.getConsent());
		}

		Object issuedDocument = digilockerService.getIssuedDocument(token);
		log.info(loggingMessageUtil.getLogMessageSuccess("Issued document list.", issuedDocument));

		JsonObject fromJson = gson.fromJson(gson.toJson(issuedDocument), JsonObject.class);
		log.info(loggingMessageUtil.getLogMessageSuccess("Issued document list in json object.", fromJson));

		if(fromJson.get(items) == null) {
			throw new KycException("Issued document list items is empty or null.");
		}
		List<JsonElement> asList = fromJson.get(items).getAsJsonArray().asList();

		log.info(loggingMessageUtil.getLogMessageSuccess("Issued document list.", asList));

		String uri = "";

		for(JsonElement je:asList) {
			if(je != null && je.getAsJsonObject().get(doctype) != null && je.getAsJsonObject().get(doctype).getAsString().equalsIgnoreCase("PANCR")) {
				uri = je.getAsJsonObject().get("uri").getAsString();
				log.info(loggingMessageUtil.getLogMessageSuccess("Document uri", uri));
			}
		}

		Object fetchIssuedDocumentInxml = null;
		if(uri!= null && !uri.isEmpty()) {
			fetchIssuedDocumentInxml = digilockerService.fetchIssuedDocumentInxml(token, uri);
			log.info(loggingMessageUtil.getLogMessageSuccess("Pan details by xml", fetchIssuedDocumentInxml));
		}
		else {
			throw new KycException("Pan card is not available in digilocker.");
		}

		if(fetchIssuedDocumentInxml == null) {
			throw new KycException("Fetched pan xml document is empty or null.");
		}
		return panAtuhenticationResponse.getPancardResponse(fetchIssuedDocumentInxml,panRequest);
	}

	@Override
	public Object aadhaarAuthentication(AadhaarRequest aadhaarRequest,String token) {
		log.info(loggingMessageUtil.getLogMessageSuccess("Aadhaar Authentication method start", aadhaarRequest));

		if(!aadhaarRequest.getConsent().equalsIgnoreCase("y") && !aadhaarRequest.getConsent().equalsIgnoreCase("n") ) {
			throw new KycException(consentError);
		}
		if(aadhaarRequest.getConsent().equalsIgnoreCase("n")) {
			throw new KycException(consentError2+aadhaarRequest.getConsent());
		}

		Object aadhaarDocument = digilockerService.getAdharDetails(token);

		log.info(loggingMessageUtil.getLogMessageSuccess("Aadhar details fetch successfully.", aadhaarDocument));

		Object aadhaarResponse2 = aadhaarResponse.getAadhaarResponse(aadhaarRequest,aadhaarDocument);

		log.info(loggingMessageUtil.getLogMessageSuccess("Aadhaar details in json object.", aadhaarResponse2));

		return aadhaarResponse2;
	}

	@Override
	public Object dlAuthentication(DlRequest dlRequest, String token) throws IOException {
		log.info(loggingMessageUtil.getLogMessageSuccess("DL Authentication method start", dlRequest));

		if(!dlRequest.getConsent().equalsIgnoreCase("y") && !dlRequest.getConsent().equalsIgnoreCase("n") ) {
			throw new KycException(consentError);
		}
		if(dlRequest.getConsent().equalsIgnoreCase("n")) {
			throw new KycException(consentError2 + dlRequest.getConsent());
		}
		Object document = digilockerService.getIssuedDocument(token);

		JsonObject fromJson = gson.fromJson(gson.toJson(document), JsonObject.class);
		List<JsonElement> asJsonArray = fromJson.get(items).getAsJsonArray().asList();
		String uri = "";
		for(JsonElement js:asJsonArray) {
			if(js.getAsJsonObject().get(doctype).getAsString().equalsIgnoreCase("DRVLC")) {
				uri = js.getAsJsonObject().get("uri").getAsString();
			}
		}
		log.info(loggingMessageUtil.getLogMessageSuccess("DL uri get successfully.", uri));

		if(uri.isEmpty()) {
			throw new KycException("DL is not available in digilocker.");
		}

		Object documentInxml = digilockerService.fetchIssuedDocumentInxml(token, uri);
		log.info(loggingMessageUtil.getLogMessageSuccess("DL document get successfully.", documentInxml));

		Object dlResponse = authResponse.getDlResponse(dlRequest, documentInxml);
		log.info(loggingMessageUtil.getLogMessageSuccess("DL authentication successfully.", dlResponse));

		return dlResponse;
	}

	@Override
	public Object rationCardAuthentication(RationCardRequest cardRequest,String token) throws IOException {
		log.info(loggingMessageUtil.getLogMessageSuccess("Ration card Authentication method start.", cardRequest));

		if(!cardRequest.getConsent().equalsIgnoreCase("y") && !cardRequest.getConsent().equalsIgnoreCase("n") ) {
			throw new KycException(consentError);
		}
		if(cardRequest.getConsent().equalsIgnoreCase("n")) {
			throw new KycException(consentError2 + cardRequest.getConsent());
		}
		Object document = digilockerService.getIssuedDocument(token);

		JsonObject fromJson = gson.fromJson(gson.toJson(document), JsonObject.class);
		List<JsonElement> asJsonArray = fromJson.get(items).getAsJsonArray().asList();
		String uri = "";
		for(JsonElement js:asJsonArray) {
			if(js.getAsJsonObject().get(doctype).getAsString().equalsIgnoreCase("RATCR")) {
				uri = js.getAsJsonObject().get("uri").getAsString();
			}
		}
		log.info(loggingMessageUtil.getLogMessageSuccess("Ration uri get successfully.", uri));

		if(uri.isEmpty()) {
			throw new KycException("Ration card is not available in digilocker.");
		}

		String[] rationNumberArray = uri.split("-");
		String rationNumber = rationNumberArray[rationNumberArray.length-1];
		log.info(loggingMessageUtil.getLogMessageSuccess("Ration card number get successfully.", rationNumber));

		Object documentInxml = digilockerService.fetchIssuedDocumentInxml(token, uri);
		log.info(loggingMessageUtil.getLogMessageSuccess("DL document get successfully.", documentInxml));

		Object rationResponse = cardResponse.getRationcardResponse(cardRequest,documentInxml,rationNumber);
		log.info(loggingMessageUtil.getLogMessageSuccess("Ration authentication successfully.", rationResponse));

		return rationResponse;
	}
}
