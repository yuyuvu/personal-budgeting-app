package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.time.LocalDateTime;

public class WalletOperationsService {

    public static void addIncome(Wallet wallet, double amount, String category, LocalDateTime dateTime) {
        wallet.getWalletOperations().add(new Wallet.WalletOperation(wallet, amount, true, category, dateTime));
    }

    public static void addExpense(Wallet wallet, double amount, String category, LocalDateTime dateTime) {
        wallet.getWalletOperations().add(new Wallet.WalletOperation(wallet, amount, false, category, dateTime));
    }

    public static boolean removeWalletOperationById(Wallet wallet, long id) {
        return wallet.getWalletOperations().removeIf(wo -> wo.getId() == id);
    }

    public static void sendMoneyToAnotherUser(Wallet from, String to, double amount) {
        User anotherUser = DataPersistenceService.loadUserdataFromFile(to);
        addExpense(from, amount, "Переводы другим пользователям", LocalDateTime.now());
        addIncome(anotherUser.getWallet(), amount, "Переводы другим пользователям", LocalDateTime.now());
    }


    public static double getExpensesByCategory(Wallet wallet, String category) {
        return wallet.getWalletOperations()
                .stream()
                .filter(w -> !w.isIncome())
                .filter(wo -> wo.getCategory().equals(category))
                .map(Wallet.WalletOperation::getAmount)
                .reduce(0.0, Double::sum);
    }

    public static double getExpensesByCategories(Wallet wallet, String... categories) {
        double result = 0.0;
        for (String category : categories){
            result += getExpensesByCategory(wallet, category);
        }
        return result;
    }

    public static double getIncomeByCategory(Wallet wallet, String category) {
        return wallet.getWalletOperations()
                .stream()
                .filter(Wallet.WalletOperation::isIncome)
                .filter(wo -> wo.getCategory().equals(category))
                .map(Wallet.WalletOperation::getAmount)
                .reduce(0.0, Double::sum);
    }

    public static double getIncomeByCategories(Wallet wallet, String... categories) {
        double result = 0.0;
        for (String category : categories){
            result += getIncomeByCategory(wallet, category);
        }
        return result;
    }
}
