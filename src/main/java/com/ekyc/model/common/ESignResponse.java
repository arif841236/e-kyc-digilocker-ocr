package com.ekyc.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ESignResponse {

	private Integer statusCode;
	private String signedPDF;
	private String initiatedBy;
}
