package com.dws.challenge.exception;

public class InsufficientBalanceException extends RuntimeException {

	private static final long serialVersionUID = -4232551650292177499L;

	public InsufficientBalanceException(String message) {
		super(message);
	}
}
