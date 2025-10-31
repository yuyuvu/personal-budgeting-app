package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.util.Map;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AnalyticsService {

    // Методы печати отчётов в консоль

    public static void printTotalSummary(Wallet wallet) {
        printBalanceSummary(wallet);
        printExpensesSummary(wallet);
        printIncomeSummary(wallet);
        printBudgetCategoriesAndLimitsSummary(wallet);
    }

    public static void printBalanceSummary(Wallet wallet) {
        double balance = wallet.getBalance();
        if (balance < 0) {
            printGreen("Баланс доходов и расходов: ");
            printlnRed(String.format("%.2f", balance));
        } else {
            printlnGreen(String.format("Баланс доходов и расходов: %s%.2f", resetColor(), wallet.getBalance()));
        }
    }

    public static void printExpensesSummary(Wallet wallet) {
        printlnGreen(String.format("Общие расходы: %s%.2f", resetColor(), wallet.getTotalExpenses()));
        printlnCyan("Расходы по категориям:");
        if (wallet.getWalletOperationsExpensesCategories().isEmpty()) {
            printlnYellow("\tНет добавленных категорий расходов, добавьте новые операции в меню управления доходами и расходами.");
            return;
        }
        for (String category : wallet.getWalletOperationsExpensesCategories()) {
            printlnYellow(String.format("\t- %s: %.2f", capitalizeFirstLetter(category), WalletOperationsService.getExpensesByCategory(wallet, category)));
        }
    }

    public static void printIncomeSummary(Wallet wallet) {
        printlnGreen(String.format("Общие доходы: %s%.2f", resetColor(), wallet.getTotalIncome()));
        printlnCyan("Доходы по категориям:");
        if (wallet.getWalletOperationsIncomeCategories().isEmpty()) {
            printlnYellow("\tНет добавленных категорий доходов, добавьте новые операции в меню управления доходами и расходами.");
            return;
        }
        for (String category : wallet.getWalletOperationsIncomeCategories()) {
            printlnYellow(String.format("\t- %s: %.2f", capitalizeFirstLetter(category), WalletOperationsService.getIncomeByCategory(wallet, category)));
        }
    }

    public static void printBudgetCategoriesAndLimitsSummary(Wallet wallet) {
        printlnCyan("Бюджет по категориям:");
        if (wallet.getBudgetCategoriesAndLimits().isEmpty()) {
            printlnPurple("\tНе найдены добавленные категории бюджета с установленными лимитами.");
            printlnPurple("\tДобавьте их в меню управления категориями или при добавлении нового расхода.");
            return;
        }
        for (Map.Entry<String, Double> category : wallet.getBudgetCategoriesAndLimits().entrySet()) {
            printPurple(String.format("\t- %s: %.2f. ", capitalizeFirstLetter(category.getKey()), BudgetingService.getLimitByCategory(wallet, category.getKey())));
            printPurple("Оставшийся бюджет: ");
            double remainder = BudgetingService.getRemainderByCategory(wallet, category.getKey());
            if (remainder <= 0.0) {
                printlnRed(String.valueOf(remainder));
            } else {
                printlnGreen(String.valueOf(remainder));
            }
        }
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
