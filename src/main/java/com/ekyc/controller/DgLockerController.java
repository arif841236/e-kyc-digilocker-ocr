package com.ekyc.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ekyc.exception.ExceptionHandlerGlobal;
import com.ekyc.exception.KycException;
import com.ekyc.model.Request;
import com.ekyc.service.IDigilockerService;
import com.ekyc.util.LoggingMessageUtil;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * This is controller layer here all written all 
 * endpoint method
 * @author Md Arif
 *
 */
@Api(description = "Token API", tags = {"Token Service"})
@RestController
@Slf4j
public class DgLockerController {

	@Autowired
	Gson gson;

	@Autowired
	LoggingMessageUtil logger;	

	@Autowired
	IDigilockerService digilockerService;

	@Value("${header.name}")
	String headerName;

	/**
	 * This endpoint for  get token
	 * @param request
	 * @return response entity of json form object
	 */
	@ApiResponses(value = {@ApiResponse(code = 200,response = String.class, message = "Token create Successfully.")})
	@PostMapping(value="/client-auth-token",consumes = {MediaType.APPLICATION_JSON_VALUE},produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Create token", notes = "Return a created token details", response = Object.class)
	@ApiImplicitParam(name = "request", value = "Fill the token variables",required = true,dataType = "Request")
	public ResponseEntity<Object> getToken(@RequestBody @Valid Request request){
		log.info(logger.getLogMessageSuccess("Get token method start.", request));
		ExceptionHandlerGlobal.initSource(request.getSource());

		if(request.getCode()==null || request.getCode().isEmpty()) {
			throw new KycException("Code is null or invalid.");
		}
		Object token = digilockerService.getToken(request);
		log.info(logger.getLogMessageSuccess("Get token method end.", token));

		return ResponseEntity.ok(token);
	}
}
