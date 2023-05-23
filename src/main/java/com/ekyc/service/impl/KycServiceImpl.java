package com.ekyc.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ekyc.exception.KycException;
import com.ekyc.model.BankStatementRequest;
import com.ekyc.model.FaceDedupeRequest;
import com.ekyc.model.KycRequest;
import com.ekyc.service.IKycService;
import com.ekyc.util.LoggingMessageUtil;
import com.ekyc.util.OcrResponse;
import com.google.gson.Gson;
import com.itextpdf.io.source.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * This is service class
 * @author Md Arif
 *
 */
@Service
@Slf4j
public class KycServiceImpl implements IKycService {

	@Autowired
	Gson gson;

	@Autowired
	OcrResponse ocrResponse;

	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	@Value("${image.direc}")
	String dir;

	/**
	 * This method to get kyc details.
	 */
	@Override
	public String getKycDetails(KycRequest kycRequest,MultipartFile file) throws IOException {
		log.info(loggingMessageUtil.getLogMessageSuccess("getAadhaardDetails method start.", null));

		String respoString = "";

		if((kycRequest.getFileB64() != null && !kycRequest.getFileB64().isEmpty()) && (kycRequest.getUrl() != null && !kycRequest.getUrl().isEmpty())) {
			throw new KycException("Please fill only one image resource.");
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("aadhaar-front")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getAadhaarFront(kycRequest,kycImageFile);
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("payslip")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getPaySlipOcr(kycImageFile);
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("aadhaar-back")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getAadhaarFront(kycRequest,kycImageFile);
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("Pan")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getPanOcr(kycImageFile);
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("dl-front")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getDlOcr(kycImageFile,kycRequest);
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("dl-back")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getDlOcr(kycImageFile,kycRequest);
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("passport-front")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getPassPostData(kycRequest,kycImageFile);
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("passport-back")) {
			File kycImageFile = kycImage(kycRequest,file);
			respoString = ocrResponse.getPassPostData(kycRequest,kycImageFile);
		}
		else {
			throw new KycException("Please enter valid doctype.");
		}

		log.info(loggingMessageUtil.getLogMessageSuccess("getAadhaardDetails method end.", null));

		return respoString;
	}

	public File kycImage(KycRequest kycRequest,MultipartFile file) throws IOException {
		File file2 = null;
		if(file != null && !file.isEmpty()) {
			file2 = saveImageFromFile(file);
		}
		else if(kycRequest.getFileB64()!= null && !kycRequest.getFileB64().isEmpty()) {
			file2 = saveImageFromBase64(kycRequest.getFileB64());
		}

		else if(kycRequest.getUrl() != null && !kycRequest.getUrl().isEmpty()) {

			file2 = saveImageFromUrl(kycRequest.getUrl());
		}
		else {
			throw new KycException("Please fill atleast one resource.");
		}
		return file2;
	}

	private File saveImageFromFile(MultipartFile file) throws IOException{
		long timestamp = Instant.now().toEpochMilli();
		byte[] bytes = file.getBytes();
		String fileExtension = getFileExtension(bytes);
		log.info(fileExtension);
		if(fileExtension != null ) {
			return getFile(dir,timestamp,fileExtension,bytes);
		}
		else {
			throw new KycException("Invalid file resource.");
		}
	}

	private File saveImageFromBase64(String base64) throws IOException {

		long timestamp = Instant.now().toEpochMilli();
		byte[] decode = Base64.getDecoder().decode(base64);
		String fileExtension = getFileExtension(decode);
		log.info(fileExtension);

		if(fileExtension != null) {
			return getFile(dir,timestamp,fileExtension,decode);
		}
		else {
			throw new KycException("Invalid fileB64 resource.");
		}
	}

	private File saveImageFromUrl(String url) throws IOException {
		log.info(url);
		if(!url.subSequence(0, 5).equals(("https"))){
			log.info(url.subSequence(0, 5).toString());
			throw new KycException("Uri should be valid formate.");
		}
		URL url2 = new URL(url);

		byte[] readAllBytes = null;
		InputStream inputStream = null;
		try {
			inputStream = url2.openStream();
			readAllBytes = inputStream.readAllBytes();
			inputStream.close();
		} catch (Exception e) {
			throw new KycException("Uri should be valid.");
		}
		String fileExtension = getFileExtension(readAllBytes);
		log.info(fileExtension);

		long timestamp = Instant.now().toEpochMilli();

		if(fileExtension != null) {
			return getFile(dir,timestamp,fileExtension,readAllBytes);
		}
		else {
			throw new KycException("Invalid image url.");
		}
	}

	private File getFile(String dir2, long timestamp, String string, byte[] bytes) throws IOException {
		File file2 =new File(dir2 + timestamp+string);

		try (FileOutputStream fileOutputStream = new FileOutputStream(file2)){
			fileOutputStream.write(bytes);
			fileOutputStream.flush();
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(e.getMessage());
		}
		return file2;
	}

	private static final Map<String, String> mimeTypeMap;
	static {
		Map<String, String> map = new HashMap<>();
		map.put("application/pdf", ".pdf");
		map.put("application/msword", ".doc");
		map.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
		map.put("application/vnd.ms-excel", ".xls");
		map.put("application/x-tika-ooxml", ".xlsx");
		map.put("image/png", ".png");
		map.put("image/jpeg", ".jpg");
		map.put("application/zip", ".docx");
		mimeTypeMap = Collections.unmodifiableMap(map);
	}

	private static String getFileExtension(byte[] decode) throws IOException {
		Tika tika = new Tika();
		String fileMimeType = tika.detect(decode);
		log.info(fileMimeType);
		return mimeTypeMap.get(fileMimeType);
	}

	@Override
	public String getBankStatement(BankStatementRequest bankStatementRequest,HttpServletResponse httpServletResponse) throws IOException {

		File saveImageFromBase64 = saveImageFromBase64(bankStatementRequest.getFileB64());
		String response = "Success";
		if(bankStatementRequest.getReturnType().equalsIgnoreCase("file")) {
			byte[] bankStatementData = ocrResponse.getBankStatementDataInFile(saveImageFromBase64);
			String key = "Content-Disposition";
			String value = "attachment; filename=predict_"+".xlsx";
			httpServletResponse.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
			httpServletResponse.setHeader(key, value);

			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			arrayOutputStream.write(bankStatementData);
			arrayOutputStream.writeTo(httpServletResponse.getOutputStream());
		}
		else if(bankStatementRequest.getReturnType().equalsIgnoreCase("json")) {
			response = ocrResponse.getBankStatementDataInJson(saveImageFromBase64);
		}
		else {
			throw new KycException("Invalid return type.");
		}

		return response;
	}

	@Override
	public String getFaceDedupe(FaceDedupeRequest faceDedupeRequest) throws IOException {
		File saveImage1FromBase64 = saveImageFromBase64(faceDedupeRequest.getImage1B64());
		File saveImage2FromBase64 = saveImageFromBase64(faceDedupeRequest.getImage2B64());
		return ocrResponse.getFaceDedupeResult(saveImage1FromBase64, saveImage2FromBase64);
	}
}
