package com.dws.challenge.exception;

public class AccountNotFoundException extends RuntimeException{


	private static final long serialVersionUID = 87900957368096024L;

	public AccountNotFoundException(String message) {
		super(message);
	}


}
