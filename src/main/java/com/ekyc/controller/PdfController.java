package com.ekyc.controller;

import java.util.Map;
import java.util.Map.Entry;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.ekyc.exception.ExceptionHandlerGlobal;
import com.ekyc.exception.KycException;
import com.ekyc.model.ESignRequest;
import com.ekyc.model.common.ESignResponse;
import com.ekyc.service.IPdfService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Api(description = "Digital Signature",tags = {"Signature Service"})
public class PdfController {

	@Autowired
	IPdfService iPdfService;

	@ApiResponses(value = {@ApiResponse(code = 200,response = ESignResponse.class, message = "Successfully digitally signed")})
	@ApiOperation(value = "Sign PDF document", notes = "Get digitally signed base64", response = ESignResponse.class)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "pdfRequest", value = "Fill the sign request variables",required = true,dataType = "ESignRequest")
		,@ApiImplicitParam(name = "token", value = "Fill the token",required = false, dataType = "Map<String,String>")
	})
	@PostMapping(value = "/e-sign", consumes = {MediaType.APPLICATION_JSON_VALUE},produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ESignResponse> svePdf(@RequestBody @Valid ESignRequest pdfRequest,@RequestHeader Map<String,String> token) throws Exception{

		log.info("PDF digital signature request initiated "+ pdfRequest.getSource());
		ExceptionHandlerGlobal.initSource(pdfRequest.getSource());

		String token1 = null;

		for(Entry<String, String> x:token.entrySet()) {
			if(x.getKey().equals("token")) {
				token1 = x.getValue();
			}
		}

		if(token1 == null || token1.isEmpty()) {
			throw new KycException("Token is null or empty.");
		}

		ESignResponse response = iPdfService.savePdfFile(pdfRequest,token1);
		log.info("Response sent to source successfully for PDF digital signature.");

		return  ResponseEntity.ok(response);
	}
}
