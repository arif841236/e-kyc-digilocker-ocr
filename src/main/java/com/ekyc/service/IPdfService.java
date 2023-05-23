package com.ekyc.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import com.ekyc.model.ESignRequest;
import com.ekyc.model.common.ESignResponse;

public interface IPdfService {

	public ESignResponse savePdfFile(ESignRequest pdfRequest, String token1) throws IOException, GeneralSecurityException, Exception;
}
