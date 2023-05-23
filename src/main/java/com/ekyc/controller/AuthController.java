package com.ekyc.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.ekyc.exception.ExceptionHandlerGlobal;
import com.ekyc.exception.KycException;
import com.ekyc.model.AadhaarRequest;
import com.ekyc.model.DlRequest;
import com.ekyc.model.PanRequest;
import com.ekyc.model.RationCardRequest;
import com.ekyc.model.common.PanResponse;
import com.ekyc.service.IAuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Api(description = "Authentication API",tags = {"Authentication","Authentication"})
public class AuthController {

	@Autowired
	IAuthenticationService authService;

	String tokens = "token";

	@Value("${token.error.msg}")
	String tokenMsg ;

	@ApiResponses(value = {@ApiResponse(code = 200,response = PanResponse.class, message = "Successfully pan verified")})
	@PostMapping(value = "/pan", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Pan Authentication", notes = "Pan Authentication", response = PanResponse.class)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "panRequest", value = "Fill the pan request variables",required = true ,dataType = "PanRequest")
		,@ApiImplicitParam(name = "token", value = "Fill the token",required = false ,dataType = "Map<String,String>")
	})
	public ResponseEntity<PanResponse> panAuthentication(@RequestBody @Valid PanRequest panRequest,@RequestHeader Map<String,String> token) throws IOException{
		if(panRequest == null) {
			throw new KycException("Request body is null.");
		}

		log.info("PAN request initiated "+ panRequest.getSource());
		ExceptionHandlerGlobal.initSource(panRequest.getSource());

		String token1 = null;

		for(Entry<String, String> x:token.entrySet()) {
			if(x.getKey().equals(tokens)) {
				token1 = x.getValue();
			}
		}

		if(token1 == null || token1.isEmpty()) {
			throw new KycException(tokenMsg);
		}

		PanResponse panAuthentication = authService.panAuthentication(panRequest, token1);
		log.info("Response sent to source successfully for PAN");

		return ResponseEntity.ok(panAuthentication);
	}

	@ApiResponses(value = {@ApiResponse(code = 200,response = Object.class, message = "Successfully aadhaar verified")})
	@PostMapping(value = "/aadhaar", consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "aadhaarRequest", value = "Fill the aadhaar request variables",required = true ,dataType = "AadhaarRequest")
		,@ApiImplicitParam(name = "token", value = "Fill the token",required = false, dataType = "Map<String,String>")
	})
	@ApiOperation(value = "Aadhaar Authentication", notes = "Aadhaar Authentication",response = Object.class)
	public ResponseEntity<Object> aadhaarAuthentication(@RequestBody @Valid AadhaarRequest aadhaarRequest, @RequestHeader Map<String,String> token) throws IOException{
		log.info("EAadhaar authentication request initiated "+ aadhaarRequest.getSource());
		ExceptionHandlerGlobal.initSource(aadhaarRequest.getSource());

		String token1 = null;

		for(Entry<String, String> x:token.entrySet()) {
			if(x.getKey().equals(tokens)) {
				token1 = x.getValue();
			}
		}

		if(token1 == null || token1.isEmpty()) {
			throw new KycException(tokenMsg);
		}

		Object aadhaarAuthentication = authService.aadhaarAuthentication(aadhaarRequest,token1);
		log.info("Response sent to source successfully for EAadhaar.");

		return ResponseEntity.ok(aadhaarAuthentication);
	}

	@ApiResponses(value = {@ApiResponse(code = 200,response = Object.class, message = "Successfully dl verified")})
	@PostMapping(value = "/dl", consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "dlRequest", value = "Fill the DL request variables",required = true ,dataType = "DlRequest")
		,@ApiImplicitParam(name = "token", value = "Fill the token",required = false, dataType = "Map<String,String>")
	})
	@ApiOperation(value = "Driving License Authentication", notes = "DL Authentication", response = Object.class)
	public ResponseEntity<Object> dlAuthentication(@RequestBody @Valid DlRequest dlRequest, @RequestHeader Map<String,String> token) throws IOException{
		log.info("DL request initiated "+ dlRequest.getSource());
		ExceptionHandlerGlobal.initSource(dlRequest.getSource());

		String token1 = null;

		for(Entry<String, String> x:token.entrySet()) {
			if(x.getKey().equals(tokens)) {
				token1 = x.getValue();
			}
		}

		if(token1 == null || token1.isEmpty()) {
			throw new KycException(tokenMsg);
		}

		Object dlAuthenticationResponse = authService.dlAuthentication(dlRequest,token1);
		log.info("Response sent to source successfully for DL.");

		return ResponseEntity.ok(dlAuthenticationResponse);
	}

	@ApiResponses(value = {@ApiResponse(code = 200,response = Object.class, message = "Successfully ration card verified")})
	@PostMapping(value = "/ration", consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "cardRequest", value = "Fill the ration card request variables",required = true ,dataType = "RationCardRequest")
		,@ApiImplicitParam(name = "token", value = "Fill the token",required = false, dataType = "Map<String,String>")
	})
	@ApiOperation(value = "Ration Card Authentication", notes = "Ration Authentication", response = Object.class)
	public ResponseEntity<Object> rationAuthentication(@RequestBody @Valid RationCardRequest cardRequest, @RequestHeader Map<String,String> token) throws IOException, ParseException{
		log.info("Ration request initiated "+ cardRequest.getSource());
		ExceptionHandlerGlobal.initSource(cardRequest.getSource());

		String token1 = null;

		for(Entry<String, String> x:token.entrySet()) {
			if(x.getKey().equals(tokens)) {
				token1 = x.getValue();
			}
		}

		if(token1 == null || token1.isEmpty()) {
			throw new KycException(tokenMsg);
		}

		Object rationAuthenticationResponse = authService.rationCardAuthentication(cardRequest,token1);
		log.info("Response sent to source successfully for Ration.");

		return ResponseEntity.ok(rationAuthenticationResponse);
	}
}
