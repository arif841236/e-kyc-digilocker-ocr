package com.ekyc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoggingResponseMessage {

	private Integer statusCode;
	private String message;
	private Object data;
	private MessageTypeConst messageTypeId;
}
