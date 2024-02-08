package com.dws.challenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.dws.challenge.domain.BalanceTransfer;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class AccountServiceControllerAdvice {
	

	@ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<BalanceTransfer> insufficientBalance(InsufficientBalanceException ex,final WebRequest request) {
		
		log.info("insufficient balance found in source account");
		
		BalanceTransfer balanceTransfer = new BalanceTransfer();
		balanceTransfer.setStatus(ex.getMessage());
		
		return new ResponseEntity<BalanceTransfer>(balanceTransfer,HttpStatus.PRECONDITION_FAILED);
    }
	
	@ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<BalanceTransfer> accountNotFound(AccountNotFoundException ex) {
		
		log.info(ex.getMessage());
		
		BalanceTransfer balanceTransfer = new BalanceTransfer();
		balanceTransfer.setStatus(ex.getMessage());
		
		return new ResponseEntity<BalanceTransfer>(balanceTransfer,HttpStatus.NOT_FOUND);
    }
}
