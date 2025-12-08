package org.example.dao;

import org.example.model.Account;

import java.util.List;

public interface AccountReaderDao {
    public List<Account> readAccounts();
}
