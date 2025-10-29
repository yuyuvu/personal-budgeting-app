package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.model.User;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AnalyticsService {

    public static void getBalanceSummary() {
        printlnCyan(String.format("Баланс доходов и расходов: %.3f",
                PersonalBudgetingApp.getCurrentAppUser().getWallet().getBalance()));
    }

    public static void getExpensesSummary() {
        printlnCyan(String.format("Общий доход: %.3f", PersonalBudgetingApp.getCurrentAppUser().getWallet().getTotalExpenses()));
        println("Доходы по категориям:");
        for (String category : PersonalBudgetingApp.getCurrentAppUser().getWallet().getWalletOperationsExpensesCategories()) {
            printlnYellow(String.format("\t%s", category.to));
        }
    }

    public static void getIncomeSummary() {
        printlnCyan(String.format("Общий доход: %.3f", PersonalBudgetingApp.getCurrentAppUser().getWallet().getTotalIncome()));
    }

    public static void getBudgetCategoriesAndLimitsSummary() {

    }
}
