package com.ekyc.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ESignRequest {

	@NotEmpty(message = "Please enter PDF base64.")
	private String pdfBase64;
	
	private String reason;
	
	@ApiModelProperty(required = false, hidden = true)
	private String issueTo;
	
	@NotEmpty(message = "Consent should not be null or empty.")
	private String consent;
	
	@Size(min = 12,max = 12, message = "Aadhaar number should be 12 digit.")
	@Pattern(regexp = "\\d+",message = "Aadhaar number should be in digit.")
	private String aadhaarNo;
	
	private String source;
}
