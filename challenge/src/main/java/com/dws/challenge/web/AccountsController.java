package com.dws.challenge.web;

import java.math.BigDecimal;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.domain.BalanceTransfer;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.AccountsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;

	@Autowired
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	@PostMapping(path = "/balanceTransfer",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BalanceTransfer> balanceTransfer(@RequestBody @Valid AccountTransfer accountTransfer) {
		log.info("Balance Transfer service {}", accountTransfer);


		Account fromAccount = Optional.ofNullable(getAccount(accountTransfer.getAccountFrom())).orElseThrow(() -> new AccountNotFoundException("Sender Account Not Found"));

		Account accountTo = Optional.ofNullable(getAccount(accountTransfer.getAccountTo())).orElseThrow(() -> new AccountNotFoundException("Receiver Account Not Found"));

		BigDecimal currentBalance = accountsService.getCurrentBalance(fromAccount);
		
		log.info("sender account current state {}",fromAccount);
		
		log.info("receiver account current state {}",accountTo);

		if(currentBalance.doubleValue() > 0 
				&& (currentBalance.subtract(accountTransfer.getTransferAmount()).doubleValue()) >= 0) {

			boolean flag=accountsService.performBalanceTransfer(fromAccount, accountTo, accountTransfer.getTransferAmount()); 

			BalanceTransfer balanceTransferObj = new BalanceTransfer();
			
			if(flag) {
				balanceTransferObj.setStatus("balance transfer processed successfully");
			}else {
				balanceTransferObj.setStatus("balance transfer process failed");
			}
			return new ResponseEntity<BalanceTransfer>(balanceTransferObj, HttpStatus.OK);
		}else {
			// throw insufficient balance
			throw new InsufficientBalanceException("Sender Account low balance");
		}
	}

}
 