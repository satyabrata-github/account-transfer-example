package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public boolean performBalanceTransfer(Account fromAccount, Account toAccount, BigDecimal Amount) {
		return this.accountsRepository.performBalanceTransfer(fromAccount, toAccount, Amount);
	}

	public BigDecimal getCurrentBalance(Account account) {
		
		synchronized (account.getAccountId()) {
			
			return account.getBalance();
		}
		
	}
}
