package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class NotificationsService {

    public static String notificationsSectionWrap(String notificationsContents) {
        StringBuilder result = new StringBuilder();
        //result.append(paintYellow("------ Уведомления ------")).append("\n");
        //result.append(paintYellow("Обратите внимание:")).append("\n");
        result.append(paintYellow("------ Уведомления. Обратите внимание: ")).append("\n");
        result.append(notificationsContents);
        //result.append(paintYellow("-------------------------")).append("\n");
        return result.toString();
    }

    public static String checkAndPrepareNotifications(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        String resultWrapped = "";
        result.append(checkBalanceConsumption(wallet));
        result.append(checkCategoriesImportance(wallet));
        result.append(checkBudgetLimitsConsumption(wallet));
        if (!result.isEmpty()) {
            resultWrapped = notificationsSectionWrap(result.toString());
            resultWrapped = "\n" + resultWrapped;
        }
        return resultWrapped;
    }

    public static String checkBalanceConsumption(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        if (wallet.getTotalExpenses() >= (wallet.getTotalIncome() * 1.20)) {
            result.append(paintRed(String.format("- Ваши расходы значительно превышают ваши доходы: в %.2f раз.",
                    wallet.getTotalExpenses()/wallet.getTotalIncome()))).append("\n");
        } else if (wallet.getBalance() < 0.0) {
            result.append(paintRed("- Ваши расходы превысили ваши доходы. У вас отрицательный баланс.")).append("\n");
        } else if (Math.round(wallet.getBalance()) == 0) {
            result.append(paintRed("- Ваши расходы достигли величины ваших доходов. У вас нулевой баланс.")).append("\n");
        } else if (wallet.getTotalExpenses() >= (wallet.getTotalIncome() * 0.80)) {
            result.append(paintRed("- Ваши расходы превысили отметку в 80% от ваших доходов.")).append("\n");
        } else if (wallet.getTotalExpenses() >= (wallet.getTotalIncome() * 0.70)) {
            result.append(paintPurple("- Ваши расходы превысили отметку в 70% от ваших доходов.")).append("\n");
        }
        return result.toString();
    }

    private static String checkCategoriesImportance(Wallet wallet) {
        StringBuilder result = new StringBuilder();
        double totalIncome = wallet.getTotalIncome();
        double totalExpenses = wallet.getTotalExpenses();

        for (String category : wallet.getWalletOperationsExpensesCategories()) {
            double expensesByCategory = WalletOperationsService.getExpensesByCategory(wallet, category);
            if (expensesByCategory >= (totalExpenses * 0.35)) {
                result.append(paintPurple(String.format("- Категория расходов \"%s\" значительно влияет на общую сумму расходов: она составляет %.1f%% от них.",
                        category,
                        expensesByCategory/totalExpenses*100))).append("\n");
            }
        }

        for (String category : wallet.getWalletOperationsIncomeCategories()) {
            double incomeByCategory = WalletOperationsService.getIncomeByCategory(wallet, category);
            if (incomeByCategory >= (totalIncome * 0.35)) {
                result.append(paintCyan(String.format("- Категория доходов \"%s\" значительно влияет на общую сумму доходов: она составляет %.1f%% от них.",
                        category,
                        incomeByCategory/totalIncome*100))).append("\n");
            }
        }

        return result.toString();
    }

    public static String checkBudgetLimitsConsumption(Wallet wallet) {
        StringBuilder result = new StringBuilder();

        for (String category : wallet.getBudgetCategoriesAndLimits().keySet()) {
            double remainderByCategory = BudgetingService.getRemainderByCategory(wallet, category);
            double limitByCategory = BudgetingService.getLimitByCategory(wallet, category);

            if (remainderByCategory < 0) {
                result.append(paintRed(String.format("- Вы вышли за бюджет расходов на категорию \"%s\" (потрачено: %.1f, лимит: %.1f, перерасход: %.1f).",
                        category, limitByCategory - remainderByCategory, limitByCategory, Math.abs(remainderByCategory)))).append("\n");
            } else if (Math.round(remainderByCategory) == 0) {
                result.append(paintRed(String.format("- Вы израсходовали бюджет расходов на категорию \"%s\" (потрачено: %.1f, лимит: %.1f). У вас нулевой остаток бюджета по ней.",
                        category, limitByCategory - remainderByCategory, limitByCategory))).append("\n");
            } else if (remainderByCategory <= (limitByCategory * 0.2)) {
                result.append(paintPurple(String.format("- У вас заканчивается бюджет расходов на категорию \"%s\" (потрачено: %.1f, лимит: %.1f, потрачено %.1f%% от лимита).",
                        category,
                        limitByCategory - remainderByCategory,
                        limitByCategory,
                        (limitByCategory - remainderByCategory)/limitByCategory*100))
                ).append("\n");
            }
        }
        return result.toString();
    }
}
