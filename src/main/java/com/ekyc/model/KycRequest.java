package com.ekyc.model;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class KycRequest {

	private boolean maskAadhaar;
	private boolean hideAadhaar;
	private String fileB64;
	
	@NotEmpty(message = "Doc type should not be null or empty.")
	private String docType;
	private String url;
	private String source;
}
