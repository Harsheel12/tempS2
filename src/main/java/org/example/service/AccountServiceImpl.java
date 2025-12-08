package org.example.service;

import org.example.dao.AccountReaderDao;
import org.example.dao.AccountWriterDao;
import org.example.model.Account;

import java.util.List;

public class AccountServiceImpl implements AccountService {

    private final AccountReaderDao accReaderDao;
    private final AccountWriterDao accWriterDao;

    public AccountServiceImpl(AccountReaderDao accReaderDao, AccountWriterDao accWriterDao) {
        this.accReaderDao = accReaderDao;
        this.accWriterDao = accWriterDao;
    }

    @Override
    public List<Account> getAccounts() {
        return accReaderDao.readAccounts();
    }

    @Override
    public Account createAccount(Account account) {
        return accWriterDao.createAccount(account);
    }

    @Override
    public void removeAccount(Account account) {
        accWriterDao.deleteAccount(account);
    }
}
