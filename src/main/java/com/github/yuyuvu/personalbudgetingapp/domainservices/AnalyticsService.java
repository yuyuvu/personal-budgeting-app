package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public static String makeSummaryByCategories(Wallet wallet, boolean isIncome, ArrayList<String> requestedCategories) {
        StringBuilder result = new StringBuilder();
        //ArrayList<String> warnings = new ArrayList<>();
        ArrayList<String> incorrectCategories = new ArrayList<>();
        result.append("\n");
        for (String category : requestedCategories) {
            if (isIncome) {
                if (!wallet.getWalletOperationsIncomeCategories().contains(category)) incorrectCategories.add(category);
            } else {
                if (!wallet.getWalletOperationsExpensesCategories().contains(category)) incorrectCategories.add(category);
            }
        }
        if (!incorrectCategories.isEmpty()) {
            result.append(paintPurple(
                    String.format("Обратите внимание, были запрошены категории %s, по которым ещё не было учтено ни одного %1$s: %s.%n",
                            (isIncome ? "дохода" : "расхода"), incorrectCategories.toString().replaceAll("[\\[\\]]", ""))));
            //incorrectCategories.forEach(category -> {requestedCategories.remove(category);});
        }

        result.append(paintGreen(String.format("Суммарные %s по категориям \"%s\": %s%.2f",
                (isIncome ? "доходы" : "расходы"),
                requestedCategories.toString().replaceAll("[\\[\\]]", ""),
                resetColor(),
                (isIncome ?
                        WalletOperationsService.getIncomeByCategories(wallet, requestedCategories.toArray(new String[0])) :
                        WalletOperationsService.getExpensesByCategories(wallet, requestedCategories.toArray(new String[0]))))))
                .append("\n");

        result.append(paintCyan(String.format("%s по каждой из существующих запрошенных категорий:", (isIncome ? "Доходы" : "Расходы")))).append("\n");

        if (WalletOperationsService.getWalletOperationsByCategories(wallet, isIncome, requestedCategories.toArray(new String[0])).isEmpty()) {
            result.append(paintYellow("\tНет учтённых операций по запрошенным категориям, добавьте новые операции в меню управления доходами и расходами.")).append("\n");
            return result.toString();
        }

        if (isIncome) {
            for (String category : wallet.getWalletOperationsIncomeCategories()) {
                if (requestedCategories.contains(category)) {
                    result.append(paintYellow(String.format("\t- %s: %.2f", capitalizeFirstLetter(category), WalletOperationsService.getIncomeByCategory(wallet, category)))).append("\n");
                }
            }
        } else {
            for (String category : wallet.getWalletOperationsExpensesCategories()) {
                if (requestedCategories.contains(category)) {
                    result.append(paintYellow(String.format("\t- %s: %.2f", capitalizeFirstLetter(category), WalletOperationsService.getExpensesByCategory(wallet, category)))).append("\n");
                }
            }
            // Также выводим лимиты по запрошенным категориям расхода, если такие есть.
            result.append(paintCyan("Бюджет по запрошенным категориям:")).append("\n");
            // Суммарный бюджет по запрошенным категориям
            result.append(paintGreen(String.format("Суммарный бюджет по запрошенным категориям: %s%.2f", resetColor(), BudgetingService.getLimitByCategories(wallet, false, requestedCategories.toArray(new String[0]))))).append("\n");
            result.append(paintGreen("Оставшийся суммарный бюджет по запрошенным категориям: "));
            double remainderTotal = BudgetingService.getRemainderByCategories(wallet, false, requestedCategories.toArray(new String[0]));
            if (remainderTotal <= 0.0) {
                result.append(paintRed(String.valueOf(remainderTotal))).append("\n");
            } else {
                result.append(paintGreen(String.valueOf(remainderTotal))).append("\n");
            }
            // Бюджет по отдельным запрошенным категориям
            boolean doRequestedCategoriesHaveLimits = requestedCategories.stream().anyMatch(c -> wallet.getBudgetCategoriesAndLimits().containsKey(c));
            if (!doRequestedCategoriesHaveLimits) {
                result.append(paintPurple("\tНе найдены добавленные бюджеты по запрошенным категориям.")).append("\n");
                result.append(paintPurple("\tДобавьте их в меню управления категориями или при добавлении нового расхода.")).append("\n");
                return result.toString();
            }
            result.append(paintGreen("Бюджеты по отдельным запрошенным категориям:")).append("\n");
            for (String category : requestedCategories) {
                if (wallet.getBudgetCategoriesAndLimits().containsKey(category)) {
                    result.append(paintPurple(String.format("\t- %s: %.2f. ", capitalizeFirstLetter(category), BudgetingService.getLimitByCategory(wallet, category))));
                    result.append(paintPurple("Оставшийся бюджет: "));
                    double remainder = BudgetingService.getRemainderByCategory(wallet, category);
                    if (remainder <= 0.0) {
                        result.append(paintRed(String.valueOf(remainder))).append("\n");
                    } else {
                        result.append(paintGreen(String.valueOf(remainder))).append("\n");
                    }
                } else {
                    result.append(paintPurple(String.format("\t- %s: %s. ", capitalizeFirstLetter(category), "бюджет не задан"))).append("\n");
                }
            }

//            for (Map.Entry<String, Double> category : wallet.getBudgetCategoriesAndLimits().entrySet()) {
//                if (requestedCategories.contains(category.getKey())) {
//                    result.append(paintPurple(String.format("\t- %s: %.2f. ", capitalizeFirstLetter(category.getKey()), BudgetingService.getLimitByCategory(wallet, category.getKey()))));
//                    result.append(paintPurple("Оставшийся бюджет: "));
//                    double remainder = BudgetingService.getRemainderByCategory(wallet, category.getKey());
//                    if (remainder <= 0.0) {
//                        result.append(paintRed(String.valueOf(remainder))).append("\n");
//                    } else {
//                        result.append(paintGreen(String.valueOf(remainder))).append("\n");
//                    }
//                }
//            }
        }
        return result.toString();
    }

    public static String makeSummaryByPeriod(Wallet wallet, boolean isIncome, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return "";
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
