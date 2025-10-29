package com.github.yuyuvu.personalbudgetingapp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Wallet {
    private ArrayList<WalletOperation> walletOperations;
    private HashMap<String, Double> budgetCategoriesAndLimits;

    public Wallet() {
        walletOperations = new ArrayList<>();
        budgetCategoriesAndLimits = new HashMap<>();
    }

    public class WalletOperation {
        double amount;
        boolean isIncome;
        String category;
        LocalDateTime dateTime;

        public double getAmount() {
            return amount;
        }

        public boolean isIncome() {
            return isIncome;
        }

        public String getCategory() {
            return category;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }

        public WalletOperation(double amount, boolean isIncome, String category, LocalDateTime dateTime) {
            this.amount = amount;
            this.isIncome = isIncome;
            this.category = category;
            this.dateTime = dateTime;
        }
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    public double getTotalIncome() {
        return this.getWalletOperations()
                .stream()
                .filter(wo -> wo.isIncome)
                .map(wo -> wo.amount)
                .reduce(0.0, Double::sum);
    }

    public double getTotalExpenses() {
        return this.getWalletOperations()
                .stream()
                .filter(wo -> !wo.isIncome)
                .map(wo -> wo.amount)
                .reduce(0.0, Double::sum);
    }

    public ArrayList<WalletOperation> getWalletOperations() {
        return walletOperations;
    }

    public HashSet<String> getWalletOperationsExpensesCategories() {
        return getWalletOperations().stream()
                .filter(WalletOperation::isIncome) // проверка на доход
                .map(WalletOperation::getCategory)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public HashSet<String> getWalletOperationsIncomeCategories() {
        return getWalletOperations().stream()
                .filter(wo -> !wo.isIncome()) // проверка на расход
                .map(WalletOperation::getCategory)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public HashMap<String, Double> getBudgetCategoriesAndLimits() {
        return budgetCategoriesAndLimits;
    }
}
