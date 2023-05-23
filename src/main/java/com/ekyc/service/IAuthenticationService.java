package com.ekyc.service;

import java.io.IOException;
import com.ekyc.model.AadhaarRequest;
import com.ekyc.model.DlRequest;
import com.ekyc.model.PanRequest;
import com.ekyc.model.RationCardRequest;
import com.ekyc.model.common.PanResponse;

public interface IAuthenticationService {

	public PanResponse panAuthentication(PanRequest panRequest,String token)throws IOException;

	public Object aadhaarAuthentication(AadhaarRequest aadhaarRequest, String token1);

	public Object dlAuthentication(DlRequest dlRequest, String token) throws IOException;

	public Object rationCardAuthentication(RationCardRequest cardRequest, String token) throws IOException;
}
