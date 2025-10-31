package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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
        addExpense(from, amount, "переводы другим пользователям", LocalDateTime.now());
        addIncome(anotherUser.getWallet(), amount, "переводы от других пользователей", LocalDateTime.now());
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

    public static ArrayList<Wallet.WalletOperation> getWalletOperationsByCategories(Wallet wallet, boolean isIncome, String... categories) {
        ArrayList<Wallet.WalletOperation> result = new ArrayList<>();
        for (String category : categories) {
            result.addAll(wallet.getWalletOperations().stream()
                    .filter(wo -> wo.isIncome() == isIncome)
                    .filter(wo -> wo.getCategory().equals(category))
                    .collect(Collectors.toCollection(ArrayList::new)));
        }
        return result;
    }

    public static ArrayList<Wallet.WalletOperation> getWalletOperationsByPeriod(Wallet wallet, LocalDateTime periodStart, LocalDateTime periodEnd) {
        ArrayList<Wallet.WalletOperation> result = new ArrayList<>();
        result.addAll(wallet.getWalletOperations().stream()
                .filter(wo -> wo.getDateTime().isAfter(periodStart) && wo.getDateTime().isBefore(periodEnd))
                .collect(Collectors.toCollection(ArrayList::new)));
        return result;
    }

    public static ArrayList<Wallet.WalletOperation> getWalletOperationsByTypeAndPeriod(Wallet wallet, boolean isIncome, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return getWalletOperationsByPeriod(wallet, periodStart, periodEnd).stream()
                .filter(wo -> wo.isIncome() == isIncome)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<Wallet.WalletOperation> getWalletOperationsByTypeAndPeriodAndCategory(Wallet wallet, boolean isIncome, String category, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return getWalletOperationsByTypeAndPeriod(wallet, isIncome, periodStart, periodEnd).stream()
                .filter(wo -> wo.getCategory().equals(category))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static double getWalletOperationsAmountsSum(ArrayList<Wallet.WalletOperation> woList) {
        return woList.stream().mapToDouble(Wallet.WalletOperation::getAmount).sum();
    }
}
