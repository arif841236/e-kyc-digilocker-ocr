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
public class FaceDedupeRequest {
	
	@NotEmpty(message = "Image1 fileB64 should not be null or empty")
	private String image1B64;
	
	@NotEmpty(message = "Image2 fileB64 should not be null or empty")
	private String image2B64;
	
	private String source;

}
