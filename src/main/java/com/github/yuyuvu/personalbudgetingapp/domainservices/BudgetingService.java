package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

public class BudgetingService {

    public boolean checkExpensesCategoryExistence(Wallet wallet, String category) {
        return wallet.getBudgetCategoriesAndLimits().containsKey(category);
    }

    public void addNewExpensesCategory(Wallet wallet, String newCategory, double newLimit) {
        wallet.getBudgetCategoriesAndLimits().put(newCategory, newLimit);
    }

    public void removeExpensesCategory(Wallet wallet, String category) throws Exception {
        if (checkExpensesCategoryExistence(wallet, category)) {
            if (getExpensesByCategory(wallet, category) != 0.0) {
                wallet.getBudgetCategoriesAndLimits().remove(category);
            } else {
                throw new Exception("Указанная категория уже содержит учтённые расходы. Вместо удаления используйте объединение с другой категорией.");
            }
        } else {
            throw new Exception("Указанной категории расходов не существует. Невозможно удалить.");
        }
    }

    public void changeLimitForCategory(Wallet wallet, String category, double newLimit) throws Exception {
        if (checkExpensesCategoryExistence(wallet, category)) {
            wallet.getBudgetCategoriesAndLimits().put(category, newLimit);
        } else {
            throw new Exception("Указанной категории расходов не существует. Невозможно изменить лимит.");
        }
    }

    public void changeNameForCategory(Wallet wallet, String category, String newName) {
        // Смена названия в хэш-таблице с лимитами (в случае, если это расход с лимитом)
        if (checkExpensesCategoryExistence(wallet, category)) {
            double limit = wallet.getBudgetCategoriesAndLimits().get(category);
            wallet.getBudgetCategoriesAndLimits().remove(category);
            wallet.getBudgetCategoriesAndLimits().put(newName, limit);
        }

        // Смена названия в массиве операций пользователя
        for (Wallet.WalletOperation wo : wallet.getWalletOperations()) {
            if (wo.getCategory().equals(category)) {
                wo.setCategory(newName);
            }
        }
    }

    public void mergeExpensesCategories(Wallet wallet, String newCategoryName, String... oldCategories) {
        // Замена множества старых лимитов на один единый
        double newLimit = getLimitByCategories(wallet, oldCategories);
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

    public void mergeIncomeCategories(Wallet wallet, String newCategoryName, String... oldCategories) {
        // Так как в случае доходов не нужно работать с лимитами, можем применить метод changeNameForCategory
        for (String category : oldCategories){
            changeNameForCategory(wallet, category, newCategoryName);
        }
    }

    public double getLimitByCategory(Wallet wallet, String category) {
        return wallet.getBudgetCategoriesAndLimits().get(category);
    }

    public double getLimitByCategories(Wallet wallet, String... categories) {
        double result = 0.0;
        for (String category : categories){
            result += getLimitByCategory(wallet, category);
        }
        return result;
    }

    public double getRemainderByCategory(Wallet wallet, String category) {
        double limit = getLimitByCategory(wallet, category);
        double alreadySpent = getExpensesByCategory(wallet, category);
        return alreadySpent - limit;
    }

    public double getRemainderByCategories(Wallet wallet, String... categories) {
        double result = 0.0;
        for (String category : categories){
            result += getRemainderByCategory(wallet, category);
        }
        return result;
    }

    public double getExpensesByCategory(Wallet wallet, String category) {
        return wallet.getWalletOperations()
                .stream()
                .filter(w -> !w.isIncome())
                .filter(wo -> wo.getCategory().equals(category))
                .map(Wallet.WalletOperation::getAmount)
                .reduce(0.0, Double::sum);
    }

    public double getExpensesByCategories(Wallet wallet, String... categories) {
        double result = 0.0;
        for (String category : categories){
            result += getExpensesByCategory(wallet, category);
        }
        return result;
    }

    public double getIncomeByCategory(Wallet wallet, String category) {
        return wallet.getWalletOperations()
                .stream()
                .filter(Wallet.WalletOperation::isIncome)
                .filter(wo -> wo.getCategory().equals(category))
                .map(Wallet.WalletOperation::getAmount)
                .reduce(0.0, Double::sum);
    }

    public double getIncomeByCategories(Wallet wallet, String... categories) {
        double result = 0.0;
        for (String category : categories){
            result += getIncomeByCategory(wallet, category);
        }
        return result;
    }
}
