package com.dws.challenge.repository;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);
  
  boolean performBalanceTransfer(Account fromAccount, Account toAccount, BigDecimal transferAmount);
  
  boolean save(Account account);

  void clearAccounts();
}
