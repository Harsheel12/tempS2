package org.example.dao;

import org.example.model.Account;

public interface AccountWriterDao {
    public Account createAccount(Account account);
    public void deleteAccount(Account account);
}
