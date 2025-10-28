package dev.yuriymordashev.financemanagement.domainlogic;

import dev.yuriymordashev.financemanagement.applogic.DataPersistenceSystem;
import dev.yuriymordashev.financemanagement.userdata.User;
import dev.yuriymordashev.financemanagement.userdata.Wallet;

public class WalletOperationsService {

    void addIncome(Wallet wallet, double amount) {

    }

    void addExpense(Wallet wallet, double amount) {

    }

    void sendMoneyToAnotherUser(Wallet from, String to, double amount) {
        User anotherUser = DataPersistenceSystem.loadWalletDataFromFile(to);
        this.addExpense(from, amount);
        this.addIncome(anotherUserWallet, amount);
    }
}
