package com.ekyc.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.ekyc.exception.KycException;
import com.ekyc.model.KycRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OcrResponse {

	@Autowired
	Gson gson;

	@Autowired
	LoggingMessageUtil logger;

	@Autowired
	Environment environment;

	@Value("${uri.connection.error}")
	String uriError;

	@Value("${file.size.error}")
	String fileSizeError;

	@Value("${content.key}")
	String contentKey;

	@Value("${content.value}")
	String contentValue;

	@Value("${content.file.key}")
	String contentFileKey;

	String requestId2 = "requestId";
	String sStatusCode = "statusCode";

	public String getPanOcr(File kycImage) throws IOException {
		String url = environment.getProperty("endpoint.uri.ocr.pan");

		long length = kycImage.length();
		if(length > 5242880) {
			throw new KycException(fileSizeError);
		}

		Resource resource = new FileSystemResource(kycImage);

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		parts.add(contentKey, contentValue);
		parts.add(contentFileKey, resource);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		Object result = null;
		if(url != null && !url.isEmpty()) {
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(url,HttpMethod.POST,httpEntity,Object.class);
			result = resultResponse.getBody();
		}
		else {
			throw new KycException(uriError);
		}

		Files.delete(kycImage.toPath());

		String pan = gson.toJson(result);

		String requestId = UUID.randomUUID().toString().substring(0, 36);
		if(pan.substring(0, 1).equals("{")) {
			JsonObject jsonObject = gson.fromJson(pan, JsonObject.class);
			jsonObject.addProperty(requestId2, requestId);
			return gson.toJson(jsonObject);
		}

		JsonArray fromJson1 = gson.fromJson(pan, JsonArray.class);

		JsonObject asJsonObject = fromJson1.get(0).getAsJsonObject();
		asJsonObject.addProperty(requestId2, requestId);
		return gson.toJson(asJsonObject);
	}

	public String getAadhaarFront(KycRequest kycRequest, File kycImage) throws IOException {
		String uri = "";
		String val = "value";
		String adharFront = "aadhaar-front";
		if(kycRequest.getDocType().equalsIgnoreCase(adharFront)) {
			uri = environment.getProperty("endpoint.uri.ocr.aadhaar.front");
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("aadhaar-back")) {
			uri = environment.getProperty("endpoint.uri.ocr.aadhaar.back");
		}

		long length = kycImage.length();
		if(length > 5242880) {
			throw new KycException(fileSizeError);
		}

		Resource resource = new FileSystemResource(kycImage);

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		parts.add(contentKey, contentValue);
		parts.add(contentFileKey, resource);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		Object result = null;
		if(uri != null && !uri.isEmpty()) {
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(uri,HttpMethod.POST,httpEntity,Object.class);

			result = resultResponse.getBody();

		}
		else {
			throw new KycException(uriError);
		}

		Files.delete(kycImage.toPath());

		String aadhaar = gson.toJson(result);
		JsonObject responseObject =null;

		String requestId = UUID.randomUUID().toString().substring(0, 36);
		if(aadhaar.substring(0, 1).equals("{")) {
			responseObject = gson.fromJson(aadhaar, JsonObject.class);
		}
		else {
			JsonArray fromJson1 = gson.fromJson(aadhaar, JsonArray.class);
			responseObject = fromJson1.get(0).getAsJsonObject();
		}

		responseObject.addProperty(requestId2, requestId);
		if(responseObject.get("result") == null || responseObject.get("result").getAsJsonArray().get(0).getAsJsonObject().get("details") == null) {
			throw new KycException("Aadhaar details are not found.");
		}

		JsonObject details = responseObject.get("result").getAsJsonArray().get(0).getAsJsonObject().get("details").getAsJsonObject();
		if(kycRequest.getDocType().equalsIgnoreCase("aadhaar-back")) {
			details.remove("name");
			details.remove("gender");
			details.remove("dob");
			details.remove("yob");
			details.remove("phone");
		}

		details.get("imageUrl").getAsJsonObject().addProperty("value", "");
		JsonObject aadhaarJson1 = details.get("aadhaar").getAsJsonObject();
		String aadhaarNumber = aadhaarJson1.get(val).getAsString();
		if(kycRequest.getDocType().equalsIgnoreCase(adharFront) && kycRequest.isHideAadhaar()) {
			aadhaarJson1.addProperty(val, "");
		}
		else if(kycRequest.getDocType().equalsIgnoreCase(adharFront) && kycRequest.isMaskAadhaar()) {
			aadhaarNumber = String.join("", aadhaarNumber.split(" "));
			int length2 = aadhaarNumber.length();
			String replace = aadhaarNumber.substring(0, length2-4);
			aadhaarNumber = aadhaarNumber.replaceAll(replace, "xxxxxxxx");
			aadhaarJson1.addProperty(val, aadhaarNumber);
			aadhaarJson1.addProperty("isMasked", "yes");
		}

		return gson.toJson(responseObject);
	}

	public String getDlOcr(File kycImage, KycRequest kycRequest) throws IOException {
		String uri = null;
		String fileContent = "";
		if(kycRequest.getDocType().equalsIgnoreCase("dl-front")) {
			uri = environment.getProperty("endpoint.uri.ocr.dl");
			fileContent = "front_dl";
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("dl-back")) {
			uri = environment.getProperty("endpoint.uri.ocr.dl.back");
			fileContent = "back_dl";
		}
		else {
			throw new KycException("Doct type should be valid.");
		}
		
		long length = kycImage.length();
		if(length > 5242880) {
			throw new KycException(fileSizeError);
		}
       
		Resource resource = new FileSystemResource(kycImage);
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		parts.add(contentKey, contentValue);
		parts.add(fileContent, resource);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		Object result = null;
		if(uri != null && !uri.isEmpty()) {
			log.info(uri);
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(uri,HttpMethod.POST,httpEntity,Object.class);
			result = resultResponse.getBody();
			log.info(uri);
		}
		else {
			throw new KycException(uriError);
		}

		Files.delete(kycImage.toPath());
		
		String dl = gson.toJson(result);
		log.info(dl);
		String requestId = UUID.randomUUID().toString().substring(0, 36);
		JsonObject asJsonObject = dl.substring(0, 1).equals("{")?gson.fromJson(dl, JsonObject.class)
				:gson.fromJson(dl, JsonArray.class).get(0).getAsJsonObject();
		asJsonObject.addProperty(requestId2, requestId);
		asJsonObject.addProperty(sStatusCode, 101);
		return gson.toJson(asJsonObject);
	}

	public String getPassPostData(KycRequest kycRequest, File kycImage) throws IOException {
		String uri = "";
		String passPortFront = "passport-front";
		if(kycRequest.getDocType().equalsIgnoreCase(passPortFront)) {
			uri = environment.getProperty("endpoint.uri.ocr.passport.front");
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("passport-back")) {
			uri = environment.getProperty("endpoint.uri.ocr.passport.back");
		}

		long length = kycImage.length();
		if(length > 5242880) {
			throw new KycException(fileSizeError);
		}

		Resource resource = new FileSystemResource(kycImage);

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		parts.add(contentKey, contentValue);
		parts.add(contentFileKey, resource);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		Object result = null;
		if(uri != null && !uri.isEmpty()) {
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(uri,HttpMethod.POST,httpEntity,Object.class);

			result = resultResponse.getBody();

		}
		else {
			throw new KycException(uriError);
		}

		Files.delete(kycImage.toPath());

		String passport = gson.toJson(result);
		JsonObject responseObject =null;

		String requestId = UUID.randomUUID().toString().substring(0, 36);
		if(passport.substring(0, 1).equals("{")) {
			responseObject = gson.fromJson(passport, JsonObject.class);
		}
		else {
			JsonArray fromJson1 = gson.fromJson(passport, JsonArray.class);
			responseObject = fromJson1.get(0).getAsJsonObject();
		}

		responseObject.addProperty(requestId2, requestId);
		responseObject.addProperty(sStatusCode, 101);
		return gson.toJson(responseObject);
	}

	public String getPaySlipOcr(File kycImage) throws IOException {
		String uri = environment.getProperty("endpoint.uri.ocr.payslip");

		long length = kycImage.length();
		if(length > 5242880) {
			throw new KycException(fileSizeError);
		}

		Resource resource = new FileSystemResource(kycImage);

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		MultiValueMap<String, Object> contentParts = new LinkedMultiValueMap<>();
		contentParts.add(contentKey, contentValue);
		contentParts.add(contentKey, "pdf");
		parts.addAll(contentParts);
		parts.add(contentFileKey, resource);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		Object result = null;
		if(uri != null && !uri.isEmpty()) {
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(uri,HttpMethod.POST,httpEntity,Object.class);
			result = resultResponse.getBody();
		}
		else {
			throw new KycException(uriError);
		}

		Files.delete(kycImage.toPath());

		String payslip = gson.toJson(result);
		String requestId = UUID.randomUUID().toString().substring(0, 36);
		JsonObject jsonObject = null;

		if(payslip.substring(0, 1).equals("{")) {
			jsonObject = gson.fromJson(payslip, JsonObject.class);
		}
		else {
			JsonArray fromJson1 = gson.fromJson(payslip, JsonArray.class);
			jsonObject = fromJson1.get(0).getAsJsonObject();
		}

		jsonObject.addProperty(requestId2, requestId);
		jsonObject.addProperty(sStatusCode, 101);
		return gson.toJson(jsonObject);
	}

	public byte[] getBankStatementDataInFile(File kycImage) throws IOException {

		String uri = environment.getProperty("endpoint.uri.ocr.bankstatement");

		Resource resource = new FileSystemResource(kycImage);
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		MultiValueMap<String, Object> contentParts = new LinkedMultiValueMap<>();
		contentParts.add(contentKey, contentValue);
		log.info(kycImage.getName());
		contentParts.add(contentKey, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		parts.addAll(contentParts);
		parts.add(contentFileKey, resource);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		byte[] byteResponse = null;
		if(uri != null && !uri.isEmpty()) {
			log.info(uri);
			ResponseEntity<byte[]> response = new RestTemplate().exchange(uri, HttpMethod.POST, httpEntity,
					byte[].class);
			log.info(uri);
			byteResponse = response.getBody();
		}
		else {
			throw new KycException(uriError);
		}
		log.info(uri);
		Files.delete(kycImage.toPath());

		return byteResponse;
	}

	public String getBankStatementDataInJson(File kycImage) throws IOException {
		String uri = environment.getProperty("endpoint.uri.ocr.bankstatement.json");

		long length = kycImage.length();
		if(length > 5242880) {
			throw new KycException(fileSizeError);
		}

		Resource resource = new FileSystemResource(kycImage);

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		MultiValueMap<String, Object> contentParts = new LinkedMultiValueMap<>();
		contentParts.add(contentKey, contentValue);
		contentParts.add(contentKey, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		parts.addAll(contentParts);
		parts.add(contentFileKey, resource);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		Object result = null;
		if(uri != null && !uri.isEmpty()) {
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(uri,HttpMethod.POST,httpEntity,Object.class);
			result = resultResponse.getBody();
		}
		else {
			throw new KycException(uriError);
		}

		Files.delete(kycImage.toPath());

		String statement1 = gson.toJson(result);
		String requestId = UUID.randomUUID().toString().substring(0, 36);
		JsonObject jsonObject = null;

		if(statement1.substring(0, 1).equals("{")) {
			jsonObject = gson.fromJson(statement1, JsonObject.class);
		}
		else {
			JsonArray fromJson1 = gson.fromJson(statement1, JsonArray.class);
			jsonObject = fromJson1.get(0).getAsJsonObject();
		}

		jsonObject.addProperty(requestId2, requestId);
		jsonObject.addProperty(sStatusCode, 101);
		return gson.toJson(jsonObject);
	}
	
    public String getFaceDedupeResult(File file1, File file2) throws IOException {
    	String uri = environment.getProperty("endpoint.uri.ocr.face.dedupe");

		long length = file1.length();
		long length2 = file2.length();
		if(length > 5242880 || length2 > 5242880) {
			throw new KycException(fileSizeError);
		}

		Resource resource = new FileSystemResource(file1);
		Resource resource2 = new FileSystemResource(file2);

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		MultiValueMap<String, Object> contentParts = new LinkedMultiValueMap<>();
		contentParts.add(contentKey, contentValue);
		contentParts.add(contentKey, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		parts.addAll(contentParts);
		parts.add("img1", resource);
		parts.add("img2", resource2);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);

		Object result = null;
		if(uri != null && !uri.isEmpty()) {
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(uri,HttpMethod.POST,httpEntity,Object.class);
			result = resultResponse.getBody();
		}
		else {
			throw new KycException(uriError);
		}

		Files.delete(file1.toPath());
		Files.delete(file2.toPath());

		String statement1 = gson.toJson(result);
		String requestId = UUID.randomUUID().toString().substring(0, 36);
		JsonObject jsonObject = null;

		if(statement1.substring(0, 1).equals("{")) {
			jsonObject = gson.fromJson(statement1, JsonObject.class);
		}
		else {
			JsonArray fromJson1 = gson.fromJson(statement1, JsonArray.class);
			jsonObject = fromJson1.get(0).getAsJsonObject();
		}

		jsonObject.addProperty(requestId2, requestId);
		jsonObject.addProperty(sStatusCode, 101);
		return gson.toJson(jsonObject);
	}
}