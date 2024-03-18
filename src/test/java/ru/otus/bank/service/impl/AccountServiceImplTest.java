package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    @Test
    public void testCharge() {
        Account account = new Account();
        account.setAmount(new BigDecimal(100));

        when(accountDao.findById(anyLong())).thenReturn(Optional.of(account));
        when(accountDao.save(account)).thenReturn(account);

        accountServiceImpl.charge(account.getId(), new BigDecimal(10));

        assertEquals(new BigDecimal(90), account.getAmount());
    }

    @Test
    public void testChargeSourceNotFound() {
        Account account = new Account();

        when(accountDao.findById(account.getId())).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, () ->
                accountServiceImpl.charge(account.getId(), new BigDecimal(10)));
        assertEquals("No source account", result.getLocalizedMessage());
    }

    @Test
    public void testAddAccount() {
        var agreement = new Agreement();
        agreement.setId(111L);
        var amount = new BigDecimal("100");
        var accountNumber = "123";
        var type = 1;

        var expectedAccount = new Account();
        expectedAccount.setAgreementId(agreement.getId());
        expectedAccount.setAmount(amount);
        expectedAccount.setNumber(accountNumber);
        expectedAccount.setType(type);

        var accountCaptor = ArgumentCaptor.forClass(Account.class);
        when(accountDao.save(accountCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountServiceImpl.addAccount(agreement, accountNumber, type, amount);
        assertEquals(expectedAccount, result);

        verify(accountDao).save(argThat(new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Account account) {
                return account.getAgreementId().equals(agreement.getId()) &&
                        account.getAmount().compareTo(amount) == 0 &&
                        account.getNumber().equals(accountNumber) &&
                        account.getType().equals(type);
            }
        }));
    }

    @Test
    public void testGetAccounts() {
        Account account1 = new Account();
        account1.setId(1L);
        account1.setAmount(new BigDecimal("100"));
        account1.setType(1);
        account1.setNumber("123");
        account1.setAgreementId(1L);

        Account account2 = new Account();
        account2.setId(2L);
        account2.setAmount(new BigDecimal("200"));
        account2.setType(2);
        account2.setNumber("456");
        account2.setAgreementId(2L);

        var expectedAccounts = Arrays.asList(account1, account2);
        when(accountDao.findAll()).thenReturn(expectedAccounts);

        List<Account> resultAccounts = accountServiceImpl.getAccounts();

        assertNotNull(resultAccounts);
        assertEquals(2, resultAccounts.size());
        assertTrue(resultAccounts.containsAll(expectedAccounts));

        verify(accountDao).findAll();
    }

    @Test
    public void testTransfer() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        assertEquals(new BigDecimal(90), sourceAccount.getAmount());
        assertEquals(new BigDecimal(20), destinationAccount.getAmount());
    }

    @Test
    public void testSourceNotFound() {
        when(accountDao.findById(any())).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, () ->
                accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10)));
        assertEquals("No source account", result.getLocalizedMessage());
    }

    @Test
    public void testDestinationNotFound() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));
            }
        });
        assertEquals("No destination account", result.getLocalizedMessage());
    }

    @Test
    public void testTransferWithVerify() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        ArgumentMatcher<Account> sourceMatcher =
                argument -> argument.getId().equals(1L) && argument.getAmount().equals(new BigDecimal(90));

        ArgumentMatcher<Account> destinationMatcher =
                argument -> argument.getId().equals(2L) && argument.getAmount().equals(new BigDecimal(20));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        verify(accountDao).save(argThat(sourceMatcher));
        verify(accountDao).save(argThat(destinationMatcher));
        }
}
