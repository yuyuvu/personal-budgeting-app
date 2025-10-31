package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import static com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService.getExpensesByCategory;

public class BudgetingService {

    public static boolean checkExpensesCategoryLimitExistence(Wallet wallet, String category) {
        return wallet.getBudgetCategoriesAndLimits().containsKey(category);
    }

    public static void addNewExpensesCategoryLimit(Wallet wallet, String newCategory, double newLimit) throws IllegalArgumentException {
        if (newLimit >= 0) {
            wallet.getBudgetCategoriesAndLimits().put(newCategory, newLimit);
        } else {
            throw new IllegalArgumentException("Лимит должен быть больше нуля или равен ему. Невозможно добавить лимит.");
        }
    }

    public static void removeExpensesCategoryLimit(Wallet wallet, String category) throws IllegalArgumentException {
        if (checkExpensesCategoryLimitExistence(wallet, category)) {
            wallet.getBudgetCategoriesAndLimits().remove(category);
        } else {
            throw new IllegalArgumentException("Установленного бюджета для данной категории расходов не существует. Невозможно удалить.");
        }
    }

    public static void changeLimitForCategory(Wallet wallet, String category, double newLimit) throws IllegalArgumentException {
        if (checkExpensesCategoryLimitExistence(wallet, category)) {
            if (newLimit >= 0) {
                wallet.getBudgetCategoriesAndLimits().put(category, newLimit);
            } else {
                throw new IllegalArgumentException("Новый лимит должен быть больше нуля или равен ему. Невозможно изменить лимит.");
            }
        } else {
            throw new IllegalArgumentException("Установленного бюджета для данной категории расходов не существует. Невозможно изменить лимит.");
        }
    }

    public static void changeNameForCategory(Wallet wallet, String category, String newName, boolean isIncome) {
        // Смена названия в хэш-таблице с лимитами (в случае, если это расход с лимитом)
        if ((!isIncome) && checkExpensesCategoryLimitExistence(wallet, category)) {
            double limit = wallet.getBudgetCategoriesAndLimits().get(category);
            wallet.getBudgetCategoriesAndLimits().remove(category);
            wallet.getBudgetCategoriesAndLimits().put(newName, limit);
        }

        // Смена названия в массиве операций пользователя
        for (Wallet.WalletOperation wo : wallet.getWalletOperations()) {
            if (wo.getCategory().equals(category) && wo.isIncome() == isIncome) {
                wo.setCategory(newName);
            }
        }
    }

    public static void mergeExpensesCategories(Wallet wallet, String newCategoryName, String... oldCategories) {
        // Замена множества старых лимитов на один единый
        double newLimit = getLimitByCategories(wallet, false, oldCategories);
        for (String category : oldCategories){
            wallet.getBudgetCategoriesAndLimits().remove(category);
        }
        wallet.getBudgetCategoriesAndLimits().put(newCategoryName, newLimit);

        // Смена названий старых категорий на одно новое в массиве операций пользователя
        for (Wallet.WalletOperation wo : wallet.getWalletOperations()) {
            for (String category : oldCategories) {
                if (wo.getCategory().equals(category)) {
                    wo.setCategory(newCategoryName);
                }
            }
        }
    }

    public static void mergeIncomeCategories(Wallet wallet, String newCategoryName, String... oldCategories) {
        // Так как в случае доходов не нужно работать с лимитами, можем применить метод changeNameForCategory
        for (String category : oldCategories){
            changeNameForCategory(wallet, category, newCategoryName, true);
        }
    }

    public static double getLimitByCategory(Wallet wallet, String category) throws IllegalArgumentException {
        if (checkExpensesCategoryLimitExistence(wallet, category)) {
            return wallet.getBudgetCategoriesAndLimits().get(category);
        } else {
            throw new IllegalArgumentException("Установленного бюджета для данной категории расходов не существует. Невозможно совершить запрошенную операцию.");
        }
    }

    public static double getLimitByCategories(Wallet wallet, boolean sensibleToErrors, String... categories) throws IllegalArgumentException {
        double result = 0.0;
        for (String category : categories){
            try {
                result += getLimitByCategory(wallet, category);
            } catch (IllegalArgumentException e) {
                if (sensibleToErrors){
                    throw e;
                }
            }
        }
        return result;
    }

    public static double getRemainderByCategory(Wallet wallet, String category) throws IllegalArgumentException {
        double limit = getLimitByCategory(wallet, category);
        double alreadySpent = getExpensesByCategory(wallet, category);
        return limit - alreadySpent;
    }

    public static double getRemainderByCategories(Wallet wallet, String... categories) {
        double result = 0.0;
        for (String category : categories){
            result += getRemainderByCategory(wallet, category);
        }
        return result;
    }
}
