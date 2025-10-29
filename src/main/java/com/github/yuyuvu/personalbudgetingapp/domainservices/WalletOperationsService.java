package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.time.LocalDateTime;

public class WalletOperationsService {

    public static void addIncome(Wallet wallet, double amount, String category, LocalDateTime dateTime) {
        wallet.getWalletOperations().add(wallet.new WalletOperation(amount, true, category, dateTime));
    }

    public static void addExpense(Wallet wallet, double amount, String category, LocalDateTime dateTime) {
        wallet.getWalletOperations().add(wallet.new WalletOperation(amount, false, category, dateTime));
    }

    public static void sendMoneyToAnotherUser(Wallet from, String to, double amount) {
        User anotherUser = DataPersistenceService.loadUserdataFromFile(to);
        addExpense(from, amount, "Переводы другим пользователям", LocalDateTime.now());
        addIncome(anotherUser.getWallet(), amount, "Переводы другим пользователям", LocalDateTime.now());
    }
}
