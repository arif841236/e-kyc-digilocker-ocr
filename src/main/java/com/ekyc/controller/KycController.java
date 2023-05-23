package com.ekyc.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ekyc.exception.ExceptionHandlerGlobal;
import com.ekyc.model.BankStatementRequest;
import com.ekyc.model.FaceDedupeRequest;
import com.ekyc.model.KycRequest;
import com.ekyc.service.IKycService;
import com.ekyc.util.LoggingMessageUtil;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * This is controller layer
 * @author Md Arif
 *
 */
@RestController
@Api(description = "OCR API",tags = {"OCR service"})
@Slf4j
public class KycController {

	@Autowired
	private IKycService kycService;

	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	@Autowired
	Gson gson;

	@ApiResponses(value = {@ApiResponse(code = 200,response = String.class, message = "Fetched document data Successfully.")})
	@PostMapping(value="/ocr",consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Fetch document image data", notes = "Get image data", response = String.class)
	@ApiImplicitParam(name = "kycRequest", value = "Fill the kyc request variables",required = true,dataType = "KycRequest")
	public ResponseEntity<String> getKycData(@RequestBody @Valid KycRequest kycRequest) throws IOException{
		log.info(gson.toJson(kycRequest.getDocType() +" Request Initiated "+ kycRequest.getSource()));
		ExceptionHandlerGlobal.initSource(kycRequest.getSource());

		String aadhaardDetails = kycService.getKycDetails(kycRequest,null);

		log.info(gson.toJson("Response Sent to Source Successfully for " + kycRequest.getDocType()));

		return ResponseEntity.ok(aadhaardDetails);
	}

	@ApiResponses(value = {@ApiResponse(code = 200,response = String.class, message = "Get bank statement data successfully.")})
	@PostMapping(value="/statement",consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.MULTIPART_FORM_DATA_VALUE,MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Get bank statement data", notes = "Get statement", response = String.class)
	@ApiImplicitParam(name = "statementRequest", value = "Fill the bank statement request variables",required = true,dataType = "BankStatementRequest")
	public ResponseEntity<String> getFileDetail(@RequestBody @Valid BankStatementRequest statementRequest,HttpServletResponse httpServletResponse) throws IOException{
		log.info(gson.toJson("Bank statement Request Initiated "+ statementRequest.getSource()));
		ExceptionHandlerGlobal.initSource(statementRequest.getSource());
		String bankStatement = kycService.getBankStatement(statementRequest, httpServletResponse);
		log.info(gson.toJson("Response Sent to Source Successfully for bank statement."));

		return ResponseEntity.ok(bankStatement);
	}
	
	@ApiResponses(value = {@ApiResponse(code = 200,response = String.class, message = "Get face dedupe data successfully.")})
	@PostMapping(value="/faceDedupe",consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Get face dedupe data", notes = "Get face dedupe", response = String.class)
	@ApiImplicitParam(name = "dedupeRequest", value = "Fill the face dedupe request variables",required = true,dataType = "FaceDedupeRequest")
	public ResponseEntity<String> faceDedupeDetail(@RequestBody @Valid FaceDedupeRequest dedupeRequest) throws IOException{
		log.info(gson.toJson("Face dedupe Request Initiated "+ dedupeRequest.getSource()));
		ExceptionHandlerGlobal.initSource(dedupeRequest.getSource());
		String bankStatement = kycService.getFaceDedupe(dedupeRequest);
		log.info(gson.toJson("Response Sent to Source Successfully for face dedupe."));

		return ResponseEntity.ok(bankStatement);
	}
}