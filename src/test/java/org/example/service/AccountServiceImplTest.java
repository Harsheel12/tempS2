package org.example.service;

import org.example.dao.*;
import org.example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    private AccountServiceImpl service;
    private AccountReaderDao readerDAO;
    private AccountWriterDao writerDAO;

    @BeforeEach
    void setUp() {
        readerDAO = mock(AccountReaderDao.class);
        writerDAO = mock(AccountWriterDao.class);

        service = new AccountServiceImpl(readerDAO, writerDAO);
    }

    @Test
    void testGetAccountsReturnsDaoData() {
        List<Account> fakeAccounts = List.of(new CheckingAccount(), new CheckingAccount());
        when(readerDAO.readAccounts()).thenReturn(fakeAccounts);

        List<Account> result = service.getAccounts();

        assertEquals(fakeAccounts, result);
        verify(readerDAO, times(1)).readAccounts();
    }

    @Test
    void testCreateAccountCallsWriteDaoCreate() {
        Account account = new CheckingAccount();

        service.createAccount(account);

        verify(writerDAO, times(1)).createAccount(account);
    }

    @Test
    void testRemoveAccountCallsWriterDaoDelete() {
        Account account = new CheckingAccount();

        service.removeAccount(account);

        verify(writerDAO, times(1)).deleteAccount(account);
    }
}
