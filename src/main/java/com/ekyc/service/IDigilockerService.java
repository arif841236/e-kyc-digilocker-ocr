package com.ekyc.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import com.ekyc.model.Request;

public interface IDigilockerService {

	public Object getToken(Request request);

	public Object getUserDetails(String token);

	public Object getIssuedDocument(String token);

	public Object fetchIssuedDocumentInxml(String token,String uri)throws IOException;

	public ByteArrayInputStream fetchDocumentByUri(String token,String uri);

	public Object getAdharDetails(String token);
}
