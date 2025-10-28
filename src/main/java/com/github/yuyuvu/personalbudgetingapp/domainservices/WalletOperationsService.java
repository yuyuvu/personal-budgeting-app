package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

public class WalletOperationsService {

    void addIncome(Wallet wallet, double amount) {

    }

    void addExpense(Wallet wallet, double amount) {

    }

    void sendMoneyToAnotherUser(Wallet from, String to, double amount) {
        User anotherUser = DataPersistenceService.loadUserdataFromFile(to);
        this.addExpense(from, amount);
        this.addIncome(anotherUser.getWallet(), amount);
    }
}
