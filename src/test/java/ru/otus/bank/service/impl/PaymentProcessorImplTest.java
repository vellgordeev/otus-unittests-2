package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessorImplTest {

    @Mock
    AccountService accountService;

    @InjectMocks
    PaymentProcessorImpl paymentProcessor;

    //кажется в этом тесте нет ассерта, я поправил
    @Test
    public void testTransfer() {
        Agreement sourceAgreement = new Agreement();
        sourceAgreement.setId(1L);

        Agreement destinationAgreement = new Agreement();
        destinationAgreement.setId(2L);

        Account sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAmount(BigDecimal.TEN);
        sourceAccount.setType(0);
        sourceAccount.setAgreementId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAmount(BigDecimal.ZERO);
        destinationAccount.setType(0);
        destinationAccount.setAgreementId(2L);

        when(accountService.getAccounts(argThat(agreement -> agreement != null && agreement.getId() == 1L)))
                .thenReturn(List.of(sourceAccount));
        when(accountService.getAccounts(argThat(agreement -> agreement != null && agreement.getId() == 2L)))
                .thenReturn(List.of(destinationAccount));

        paymentProcessor.makeTransfer(sourceAgreement, destinationAgreement, 0, 0, BigDecimal.ONE);

        verify(accountService).makeTransfer(eq(1L), eq(2L), eq(BigDecimal.ONE));
    }

    @Test
    public void testTransferWithCommission() {
        Agreement sourceAgreement = new Agreement();
        sourceAgreement.setId(1L);

        Agreement destinationAgreement = new Agreement();
        destinationAgreement.setId(2L);

        Account sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAmount(BigDecimal.TEN);
        sourceAccount.setType(0);
        sourceAccount.setAgreementId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAmount(BigDecimal.ZERO);
        destinationAccount.setType(0);
        destinationAccount.setAgreementId(2L);

        when(accountService.getAccounts(argThat(agreement -> agreement != null && agreement.getId() == 1L)))
                .thenReturn(List.of(sourceAccount));
        when(accountService.getAccounts(argThat(agreement -> agreement != null && agreement.getId() == 2L)))
                .thenReturn(List.of(destinationAccount));

        paymentProcessor.makeTransferWithComission(sourceAgreement,
                destinationAgreement, 0, 0, new BigDecimal(5), new BigDecimal("0.5"));

        verify(accountService).charge(sourceAccount.getId(), new BigDecimal("-2.5"));
    }

}
