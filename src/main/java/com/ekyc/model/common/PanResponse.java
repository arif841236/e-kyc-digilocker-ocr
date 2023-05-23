package com.ekyc.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PanResponse {

	private String statusCode;
	private String	requestId;
	private Result result;

}
