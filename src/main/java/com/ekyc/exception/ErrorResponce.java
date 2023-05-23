package com.ekyc.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class for showing otp error exception
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ErrorResponce {
	private Integer statusCode;
	private String message;
	private String path;
}
