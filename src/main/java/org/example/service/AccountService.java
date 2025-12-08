package org.example.service;

import org.example.model.Account;

import java.util.List;

public interface AccountService {
    public List<Account> getAccounts();
    public Account createAccount(Account account);
    public void removeAccount(Account account);
}
