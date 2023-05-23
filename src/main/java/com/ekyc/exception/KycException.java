package com.ekyc.exception;

/**
 * This class for exception and its extends to runtime exception
 */
public class KycException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public KycException() {
	}

	public KycException(String msg) {
		super(msg);
	}
}
