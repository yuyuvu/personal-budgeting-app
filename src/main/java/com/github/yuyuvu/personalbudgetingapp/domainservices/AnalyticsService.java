package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.util.Map;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AnalyticsService {

    // Методы печати отчётов в консоль

    public static String makeTotalSummary(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        result.append(makeBalanceSummary(wallet));
        result.append(makeExpensesSummary(wallet));
        result.append(makeIncomeSummary(wallet));
        result.append(makeBudgetCategoriesAndLimitsSummary(wallet));
        return result.toString();
    }

    public static String makeBalanceSummary(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        double balance = wallet.getBalance();
        if (balance < 0) {
            result.append(paintGreen("Баланс доходов и расходов: "));
            result.append(paintRed(String.format("%.2f", balance))).append("\n");
        } else {
            result.append(paintGreen(String.format("Баланс доходов и расходов: %s%.2f", resetColor(), wallet.getBalance()))).append("\n");
        }
        return result.toString();
    }

    public static String makeExpensesSummary(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        result.append(paintGreen(String.format("Общие расходы: %s%.2f", resetColor(), wallet.getTotalExpenses()))).append("\n");
        result.append(paintCyan("Расходы по категориям:")).append("\n");
        if (wallet.getWalletOperationsExpensesCategories().isEmpty()) {
            result.append(paintYellow("\tНет добавленных категорий расходов, добавьте новые операции в меню управления доходами и расходами.")).append("\n");
            return result.toString();
        }
        for (String category : wallet.getWalletOperationsExpensesCategories()) {
            result.append(paintYellow(String.format("\t- %s: %.2f", capitalizeFirstLetter(category), WalletOperationsService.getExpensesByCategory(wallet, category)))).append("\n");
        }
        return result.toString();
    }

    public static String makeIncomeSummary(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        result.append(paintGreen(String.format("Общие доходы: %s%.2f", resetColor(), wallet.getTotalIncome()))).append("\n");
        result.append(paintCyan("Доходы по категориям:")).append("\n");
        if (wallet.getWalletOperationsIncomeCategories().isEmpty()) {
            result.append(paintYellow("\tНет добавленных категорий доходов, добавьте новые операции в меню управления доходами и расходами.")).append("\n");
            return result.toString();
        }
        for (String category : wallet.getWalletOperationsIncomeCategories()) {
            result.append(paintYellow(String.format("\t- %s: %.2f", capitalizeFirstLetter(category), WalletOperationsService.getIncomeByCategory(wallet, category)))).append("\n");
        }
        return result.toString();
    }

    public static String makeBudgetCategoriesAndLimitsSummary(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        result.append(paintCyan("Бюджет по категориям:")).append("\n");
        if (wallet.getBudgetCategoriesAndLimits().isEmpty()) {
            result.append(paintPurple("\tНе найдены добавленные категории бюджета с установленными лимитами.")).append("\n");
            result.append(paintPurple("\tДобавьте их в меню управления категориями или при добавлении нового расхода.")).append("\n");
            return result.toString();
        }
        for (Map.Entry<String, Double> category : wallet.getBudgetCategoriesAndLimits().entrySet()) {
            result.append(paintPurple(String.format("\t- %s: %.2f. ", capitalizeFirstLetter(category.getKey()), BudgetingService.getLimitByCategory(wallet, category.getKey()))));
            result.append(paintPurple("Оставшийся бюджет: "));
            double remainder = BudgetingService.getRemainderByCategory(wallet, category.getKey());
            if (remainder <= 0.0) {
                result.append(paintRed(String.valueOf(remainder))).append("\n");
            } else {
                result.append(paintGreen(String.valueOf(remainder))).append("\n");
            }
        }
        return result.toString();
    }

    public static String makeIncomeWalletOperationsList(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        result.append(paintGreen(String.format("Общие доходы: %s%.2f", resetColor(), wallet.getTotalIncome()))).append("\n");
        if (wallet.getIncomeWalletOperations().isEmpty()) {
            result.append(paintYellow(" - Нет добавленных операций дохода, добавьте новые операции в меню управления доходами и расходами.")).append("\n");
            return result.toString();
        }
        result.append(paintCyan("Список всех учтённых операций дохода:")).append("\n");
        for (Wallet.WalletOperation wo : wallet.getIncomeWalletOperations()) {
            result.append(paintYellow(String.format("- ID: %d, Категория: \"%s\", Сумма: %.2f, Дата: %s.", wo.getId(), capitalizeFirstLetter(wo.getCategory()), wo.getAmount(), wo.getDateTime()))).append("\n");
        }
        return result.toString();
    }

    public static String makeExpensesWalletOperationsList(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        result.append(paintGreen(String.format("Общие расходы: %s%.2f", resetColor(), wallet.getTotalExpenses()))).append("\n");
        if (wallet.getExpensesWalletOperations().isEmpty()) {
            result.append(paintYellow(" - Нет добавленных операций расхода, добавьте новые операции в меню управления доходами и расходами.")).append("\n");
            return result.toString();
        }
        result.append(paintCyan("Список всех учтённых операций расхода:")).append("\n");
        for (Wallet.WalletOperation wo : wallet.getExpensesWalletOperations()) {
            result.append(paintYellow(String.format("- ID: %d, Категория: \"%s\", Сумма: %.2f, Дата: %s.", wo.getId(), capitalizeFirstLetter(wo.getCategory()), wo.getAmount(), wo.getDateTime()))).append("\n");
        }
        return result.toString();
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
