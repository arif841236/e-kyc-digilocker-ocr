package com.ekyc.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AadhaarRequest {

	@NotEmpty(message = "Consent should not be null or empty.")
	private String consent;

	@NotEmpty(message = "Aadhaar number should not be null or empty.")
	@Size(min = 12,max = 12, message = "Aadhaar number should be 12 digit.")
	@Pattern(regexp = "\\d+",message = "Aadhaar number should be in digit.")
	private String aadhaarNo;
	
	private String source;
}
