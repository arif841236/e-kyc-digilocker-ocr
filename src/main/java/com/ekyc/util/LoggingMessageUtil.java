package com.ekyc.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.ekyc.common.LoggingResponseMessage;
import com.ekyc.common.MessageTypeConst;
import com.ekyc.model.KycRequest;
import com.google.gson.Gson;

@Component
public class LoggingMessageUtil {

	@Autowired
	Gson gson;

	public String getLogMessageSuccess(String msg, Object obj) {
		LoggingResponseMessage msgStart = LoggingResponseMessage.builder()
				.message(msg)
				.statusCode(HttpStatus.OK.value())
				.messageTypeId(MessageTypeConst.SUCCESS)
				.data(obj)
				.build();

		return gson.toJson(msgStart);
	}

	public String getLogMessageError(String msg, Object obj) {
		LoggingResponseMessage msgStart = LoggingResponseMessage.builder()
				.message(msg)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.messageTypeId(MessageTypeConst.ERROR)
				.data(obj)
				.build();
		return gson.toJson(msgStart);
	}

	public String getRequestLog(KycRequest kycRequest) {
		if(kycRequest == null) {
			return null;
		}
		String fileB64 = kycRequest.getFileB64();
		fileB64 = fileB64.substring(0, 40).concat(fileB64.substring(fileB64.length()-50, fileB64.length()));

		KycRequest fileB642 = KycRequest.builder()
				.docType(kycRequest.getDocType())
				.hideAadhaar(kycRequest.isHideAadhaar())
				.maskAadhaar(kycRequest.isMaskAadhaar())
				.fileB64(fileB64)
				.build();

		Object fromJson = gson.fromJson(gson.toJson(fileB642), Object.class);
		return fromJson.toString();
	}
}
