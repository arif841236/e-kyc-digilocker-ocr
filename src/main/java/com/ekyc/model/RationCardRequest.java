package com.ekyc.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RationCardRequest {

	@NotEmpty(message = "Consent should not be null or empty.")
	private String consent;

	@NotEmpty(message = "Ration number should not be null or empty.")
	@Pattern(regexp = "\\d+",message = "Ration number should be digit.")
	private String rationNumber;
	private String source;
}
