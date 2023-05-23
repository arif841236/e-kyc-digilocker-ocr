package com.ekyc.service;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.ekyc.model.BankStatementRequest;
import com.ekyc.model.FaceDedupeRequest;
import com.ekyc.model.KycRequest;

public interface IKycService {

	public String getKycDetails(KycRequest adharRequest, MultipartFile file)throws IOException;

	public String getBankStatement(BankStatementRequest bankStatementRequest,HttpServletResponse httpServletResponse) throws IOException;
	
	public String getFaceDedupe(FaceDedupeRequest faceDedupeRequest) throws IOException;
}
