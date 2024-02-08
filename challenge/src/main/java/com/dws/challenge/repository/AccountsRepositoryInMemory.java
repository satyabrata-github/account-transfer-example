package com.dws.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException(
					"Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public boolean performBalanceTransfer(Account fromAccount, Account toAccount, BigDecimal transferAmount) {
		BigDecimal fromAccountPreviousBalance = fromAccount.getBalance(); 
		BigDecimal toAccountPreviousBalance = toAccount.getBalance(); 

		synchronized (fromAccount.getAccountId()) {
			try {
				
				fromAccount.setBalance(fromAccountPreviousBalance.subtract(transferAmount));
				save(fromAccount);

				toAccount.setBalance(toAccountPreviousBalance.add(transferAmount));
				save(toAccount);

				return true;
			}catch(Exception e) {
				log.error("Exception in balancetranfer reason",e);
				log.info("initiating rollback");
				fromAccount.setBalance(fromAccountPreviousBalance);
				toAccount.setBalance(toAccountPreviousBalance);
				doRollBack(fromAccount, toAccount);
			}finally {
				log.info("post transfer sender account details {}",fromAccount);
				log.info("post transfer receiver account details {}",toAccount);
			}
		}
		return false;
	}

	@Override
	public boolean save(Account account) {
		try {
			accounts.put(account.getAccountId(), account);
			return true;
		}catch(Exception e) {
			log.error("Exception in saving Account {} reason {}",account,e);
		}
		return false;
	}

	private void doRollBack(Account fromAccount, Account toAccount) {
		save(fromAccount);
		save(toAccount);
	}
}
