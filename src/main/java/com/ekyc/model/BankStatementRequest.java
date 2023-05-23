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
public class BankStatementRequest {
	
	@NotEmpty(message = "FileB64 should not be null or empty.")
    private String fileB64;
	
	@NotEmpty(message = "Return type should not be null or empty.")
	private String returnType;
	
	private String source;
}
