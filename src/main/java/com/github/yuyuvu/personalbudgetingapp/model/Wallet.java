package com.github.yuyuvu.personalbudgetingapp.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Wallet {
    private ArrayList<WalletOperation> walletOperations;
    private HashMap<String, Double> budgetCategoriesAndLimits;

    /** Данный конструктор должен использоваться только библиотекой Jackson для десериализации */
    @JsonCreator
    private Wallet() {}

    public Wallet(boolean usedForDeserialization) {
        walletOperations = new ArrayList<>();
        budgetCategoriesAndLimits = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "walletOperations=" + walletOperations +
                ", budgetCategoriesAndLimits=" + budgetCategoriesAndLimits +
                '}';
    }

    // Хотя, по логике, классу WalletOperation лучше быть нестатическим,
    // нет способа добавить пустой конструктор без неявного параметра родителя для вложенного нестатического класса.
    // Такой конструктор нужен для корректной работы десериализации JSON.
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class WalletOperation {
        private long id;
        private double amount;
        private boolean isIncome;
        private String category;
        private LocalDateTime dateTime;

        public double getId() {
            return id;
        }

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

        /** Данный конструктор должен использоваться только библиотекой Jackson для десериализации */
        @JsonCreator
        private WalletOperation(){}

        public WalletOperation(Wallet wallet, double amount, boolean isIncome, String category, LocalDateTime dateTime) {
            this.id = generateNewWalletOperationId(wallet);
            this.amount = amount;
            this.isIncome = isIncome;
            this.category = category;
            this.dateTime = dateTime;
        }

        private long generateNewWalletOperationId(Wallet wallet) {
            long id;
            boolean idAlreadyExists = false;
            while (true) {
                id = (long) (Math.random()*Long.MAX_VALUE);
                for (WalletOperation wo : wallet.getWalletOperations()) {
                    if (wo.getId() == id) idAlreadyExists = true;
                }
                if (!idAlreadyExists) {
                    return id;
                } else continue;
            }
        }
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    public double getTotalIncome() {
        return this.getWalletOperations()
                .stream()
                .filter(WalletOperation::isIncome)
                .map(WalletOperation::getAmount)
                .reduce(0.0, Double::sum);
    }

    public double getTotalExpenses() {
        return this.getWalletOperations()
                .stream()
                .filter(wo -> !wo.isIncome())
                .map(WalletOperation::getAmount)
                .reduce(0.0, Double::sum);
    }

    public ArrayList<WalletOperation> getWalletOperations() {
        return walletOperations;
    }

    public HashSet<String> getWalletOperationsExpensesCategories() {
        return getWalletOperations().stream()
                .filter(wo -> !wo.isIncome()) // проверка на расход
                .map(WalletOperation::getCategory)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public HashSet<String> getWalletOperationsIncomeCategories() {
        return getWalletOperations().stream()
                .filter(WalletOperation::isIncome) // проверка на доход
                .map(WalletOperation::getCategory)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public HashMap<String, Double> getBudgetCategoriesAndLimits() {
        return budgetCategoriesAndLimits;
    }
}
