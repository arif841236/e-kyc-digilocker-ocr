package com.ekyc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.ekyc.exception.KycException;
import com.ekyc.model.Request;
import com.ekyc.service.IDigilockerService;
import com.ekyc.util.LoggingMessageUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

/**
 * This is service layer 
 * here written all logic
 * and consume multiple api
 * @author Md Arif
 *
 */
@Slf4j
@Service
public class DigilockerServiceImpl implements IDigilockerService {

	@Autowired
	Gson gson;

	@Autowired
	LoggingMessageUtil logger;	

	@Autowired
	Environment environment;

	@Value("${header.name}")
	String headerName;

	@Value("${entity.body}")
	String body;

	@Value("${header.auth}")
	String bearer;

	@Value("${url.error}")
	String urlError;

	@Value("${header.set.msg}")
	String message;

	/**
	 * This method to get token by consume another api
	 */
	@Override
	public Object getToken(Request request) {
		log.info(logger.getLogMessageSuccess("Get token method start.", request));

		String uri = environment.getProperty("endpoint.uri.token");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map2 = new LinkedMultiValueMap<>();
		map2.add("code", request.getCode());
		map2.add("grant_type", environment.getProperty("ekyc.granttype.value"));
		map2.add("client_id", environment.getProperty("ekyc.client.id.value"));
		map2.add("client_secret", environment.getProperty("ekyc.client.secret.value"));
		map2.add("redirect_uri", environment.getProperty("ekyc.redirect.url.value"));
		log.info(logger.getLogMessageSuccess(message, headers));

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity <> (map2, headers);
		Object returnResponse = null;
		if(uri!= null) {
			log.info(uri);
			ResponseEntity<Object> response = new RestTemplate().exchange(uri, HttpMethod.POST, entity,
					Object.class);
			log.info(uri);
			returnResponse = response.getBody();
		}
		else {
			throw new KycException(urlError);
		}

		return returnResponse;
	}

	/**
	 * This method to get userdetails by consume another api
	 */
	@Override
	public Object getUserDetails(String token) {
		log.info(logger.getLogMessageSuccess("Get user details method start.", token));

		String uri = environment.getProperty("endpoint.uri.user");
		HttpHeaders headers = new HttpHeaders();
		headers.set(headerName, bearer+token);
		HttpEntity<String> entity = new HttpEntity <> (body, headers);
		log.info(logger.getLogMessageSuccess(message, headers));

		Object returnResponse = null;
		if(uri!= null) {
			ResponseEntity<Object> response = new RestTemplate().exchange(uri, HttpMethod.POST, entity,
					Object.class);
			returnResponse = response.getBody();
		}
		else {
			throw new KycException(urlError);
		}

		return returnResponse;
	}

	/**
	 * This method to get issued document by consume another api
	 */
	@Override
	public Object getIssuedDocument(String token) {
		log.info(logger.getLogMessageSuccess("Get issued document method start.", token));

		String uri = environment.getProperty("endpoint.uri.issueddoc");
		HttpHeaders headers = new HttpHeaders();
		headers.set(headerName, bearer+token);
		HttpEntity<String> entity = new HttpEntity <> (body, headers);
		log.info(logger.getLogMessageSuccess(message, headers));

		Object returnResponse = null;
		if(uri!= null) {
			ResponseEntity<Object> response = new RestTemplate().exchange(uri, HttpMethod.POST, entity,
					Object.class);
			returnResponse = response.getBody();
		}
		else {
			throw new KycException(urlError);
		}

		return returnResponse;
	}

	/**
	 * This method to fetch document details through uri by consume another api
	 */
	@Override
	public ByteArrayInputStream fetchDocumentByUri(String token, String docUri) {
		log.info(logger.getLogMessageSuccess("Fetch document by uri method start.", token));

		String uri = environment.getProperty("endpoint.uri.docuri");
		HttpHeaders headers = new HttpHeaders();
		headers.set(headerName,bearer+ token);
		HttpEntity<String> entity = new HttpEntity <> (body, headers);
		log.info(logger.getLogMessageSuccess(message, headers));

		byte[] byteResponse = null;
		if(uri != null) {
			uri = uri.concat(docUri);
			ResponseEntity<byte[]> response = new RestTemplate().exchange(uri, HttpMethod.GET, entity,
					byte[].class);

			byteResponse = response.getBody();
		}
		else {
			throw new KycException(urlError);
		}

		return new ByteArrayInputStream(byteResponse);
	}

	/**
	 * This method to fetch issued document in xml file by consume another api
	 * and return its json file.
	 */
	@Override
	public Object fetchIssuedDocumentInxml(String token,String docUri) throws IOException {
		log.info(logger.getLogMessageSuccess("Fetch issued document in xml method start.", token));

		String uri = environment.getProperty("enpoint.uri.docxml");
		HttpHeaders headers = new HttpHeaders();
		headers.set(headerName,bearer+ token);
		HttpEntity<String> entity = new HttpEntity <> (body, headers);
		log.info(logger.getLogMessageSuccess(message, headers));

		byte[] byteResponse = null;
		if(uri != null) {
			uri = uri.concat(docUri);
			ResponseEntity<byte[]> response = new RestTemplate().exchange(uri, HttpMethod.GET, entity,
					byte[].class);

			byteResponse = response.getBody();
		}
		else {
			throw new KycException(urlError);
		}

		JSONObject jsonObject = XML.toJSONObject (new String(byteResponse));

		return gson.fromJson(jsonObject.toString(), Object.class);
	}

	/**
	 * This method to get aadhaar details by consume another api
	 * and return its json file.
	 */
	@Override
	public Object getAdharDetails(String token) {
		log.info(logger.getLogMessageSuccess("Get aadhaar details method start.", token));

		String uri = environment.getProperty("endpoint.uri.aadhaar");
		HttpHeaders headers = new HttpHeaders();
		headers.set(headerName,bearer+ token);
		HttpEntity<String> entity = new HttpEntity <> (body, headers);
		log.info(logger.getLogMessageSuccess(message, headers));

		byte[] byteResponse = null;
		if(uri != null) {

			ResponseEntity<byte[]> response = new RestTemplate().exchange(uri, HttpMethod.GET, entity,
					byte[].class);

			byteResponse = response.getBody();
		}
		else {
			throw new KycException(urlError);
		}

		JSONObject jsonObject = XML.toJSONObject (new String(byteResponse));

		return gson.fromJson(jsonObject.toString(), Object.class);
	}

}
