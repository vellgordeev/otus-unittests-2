package ru.otus.bank.service.impl;

import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.AccountService;
import ru.otus.bank.service.PaymentProcessor;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;

public class PaymentProcessorImpl implements PaymentProcessor {
    private AccountService accountService;

    public PaymentProcessorImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    public boolean makeTransfer(Agreement source, Agreement destination, int sourceType,
                                int destinationType, BigDecimal amount) {

        Account sourceAccount = accountService.getAccounts(source).stream()
                .filter(account -> account.getType() == sourceType)
                .findAny()
                .orElseThrow(() -> new AccountException("Account not found"));

        Account destinationAccount = accountService.getAccounts(destination).stream()
                .filter(account -> account.getType() == destinationType)
                .findAny()
                .orElseThrow(() -> new AccountException("Account not found"));

        return accountService.makeTransfer(sourceAccount.getId(), destinationAccount.getId(), amount);
    }

    @Override
    public boolean makeTransferWithComission(Agreement source, Agreement destination,
                                             int sourceType, int destinationType,
                                             BigDecimal amount,
                                             BigDecimal commissionPercent) {

        Account sourceAccount = accountService.getAccounts(source).stream()
                .filter(account -> account.getType() == sourceType)
                .findAny()
                .orElseThrow(() -> new AccountException("Account not found"));

        Account destinationAccount = accountService.getAccounts(destination).stream()
                .filter(account -> account.getType() == destinationType)
                .findAny()
                .orElseThrow(() -> new AccountException("Account not found"));

        //а здесь точно комиссия должна быть отрицательной? В реализации charge мы же просто вычитаем сумму из баланса
        accountService.charge(sourceAccount.getId(), amount.negate().multiply(commissionPercent));

        return accountService.makeTransfer(sourceAccount.getId(), destinationAccount.getId(), amount);
    }
}
