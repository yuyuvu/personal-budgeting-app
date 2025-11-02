package com.github.yuyuvu.personalbudgetingapp.domainservices;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.paintCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.paintGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.paintPurple;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.paintRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.paintYellow;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.util.ArrayList;

public class NotificationsService {

  public static String notificationsSectionWrap(String notificationsContents) {
    StringBuilder result = new StringBuilder();
    result.append(paintYellow("------ Уведомления. Обратите внимание: ")).append("\n");
    result.append(notificationsContents);
    return result.toString();
  }

  public static String checkAndPrepareNotifications(Wallet wallet) {
    StringBuilder result = new StringBuilder();
    result.append(checkBalanceConsumption(wallet));
    result.append(checkCategoriesImportance(wallet));
    result.append(checkBudgetLimitsConsumption(wallet));
    String resultWrapped = "";
    if (!result.isEmpty()) {
      resultWrapped = notificationsSectionWrap(result.toString());
      resultWrapped = "\n" + resultWrapped;
    }
    return resultWrapped;
  }

  public static String checkBalanceConsumption(Wallet wallet) {
    StringBuilder result = new StringBuilder();
    boolean noOperations =
        wallet.getIncomeWalletOperations().isEmpty()
            && wallet.getExpensesWalletOperations().isEmpty();
    boolean allTypesOfOperationsPresent =
        !wallet.getIncomeWalletOperations().isEmpty()
            && !wallet.getExpensesWalletOperations().isEmpty();
    if (noOperations) {
      result
          .append(
              paintPurple(
                  "- У вас пока нет добавленных доходов или расходов. У вас нулевой баланс."))
          .append("\n");
      return result.toString();
    }
    if (allTypesOfOperationsPresent
        && (wallet.getTotalExpenses() >= (wallet.getTotalIncome() * 1.20))) {
      result
          .append(
              paintRed(
                  String.format(
                      "- Ваши расходы значительно превышают ваши доходы: в %.1f раз. "
                          + "У вас отрицательный баланс.",
                      wallet.getTotalExpenses() / wallet.getTotalIncome())))
          .append("\n");
    } else if (wallet.getBalance() < 0.0) {
      result
          .append(paintRed("- Ваши расходы превысили ваши доходы. У вас отрицательный баланс."))
          .append("\n");
    } else if (Math.round(wallet.getBalance()) == 0) {
      result
          .append(paintRed("- Ваши расходы достигли величины ваших доходов. У вас нулевой баланс."))
          .append("\n");
    } else if (wallet.getTotalExpenses() >= (wallet.getTotalIncome() * 0.80)) {
      result
          .append(paintRed("- Ваши расходы превысили отметку в 80% от ваших доходов."))
          .append("\n");
    } else if (wallet.getTotalExpenses() >= (wallet.getTotalIncome() * 0.70)) {
      result
          .append(paintPurple("- Ваши расходы превысили отметку в 70% от ваших доходов."))
          .append("\n");
    } else if (allTypesOfOperationsPresent
        && (wallet.getTotalIncome() >= (wallet.getTotalExpenses() * 1.50))) {
      result
          .append(
              paintGreen(
                  String.format(
                      "- Ваши доходы значительно превышают ваши расходы: в %.1f раз. Так держать!",
                      wallet.getTotalIncome() / wallet.getTotalExpenses())))
          .append("\n");
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
        result
            .append(
                paintPurple(
                    String.format(
                        "- Категория расходов \"%s\" значительно влияет на общую сумму расходов: "
                            + "она составляет %.1f%% от них.",
                        category, expensesByCategory / totalExpenses * 100)))
            .append("\n");
      }
    }

    for (String category : wallet.getWalletOperationsIncomeCategories()) {
      double incomeByCategory = WalletOperationsService.getIncomeByCategory(wallet, category);
      if (incomeByCategory >= (totalIncome * 0.4)) {
        result
            .append(
                paintCyan(
                    String.format(
                        "- Категория доходов \"%s\" значительно влияет на общую сумму доходов: "
                            + "она составляет %.1f%% от них.",
                        category, incomeByCategory / totalIncome * 100)))
            .append("\n");
      }
    }

    return result.toString();
  }

  public static String checkBudgetLimitsConsumption(Wallet wallet) {
    StringBuilder result = new StringBuilder();
    ArrayList<String> restrictedCategories = new ArrayList<>();
    for (String category : wallet.getBudgetCategoriesAndLimits().keySet()) {
      double remainderByCategory = BudgetingService.getRemainderByCategory(wallet, category);
      double limitByCategory = BudgetingService.getLimitByCategory(wallet, category);
      double expensesByCategory = WalletOperationsService.getExpensesByCategory(wallet, category);
      if (Math.round(limitByCategory) == 0) {
        restrictedCategories.add(category);
      }
      if (remainderByCategory < 0) {
        result
            .append(
                paintRed(
                    String.format(
                        "- Вы вышли за бюджет расходов на категорию \"%s\" "
                            + "(потрачено: %.1f, лимит: %.1f, перерасход: %.1f).",
                        category,
                        expensesByCategory,
                        limitByCategory,
                        Math.abs(remainderByCategory))))
            .append("\n");
      } else if (Math.round(remainderByCategory) == 0 && Math.round(limitByCategory) != 0) {
        result
            .append(
                paintRed(
                    String.format(
                        "- Вы израсходовали бюджет расходов на категорию \"%s\" "
                            + "(потрачено: %.1f, лимит: %.1f). "
                            + "У вас нулевой остаток бюджета по ней.",
                        category, expensesByCategory, limitByCategory)))
            .append("\n");
      } else if (expensesByCategory > 0 && (remainderByCategory <= (limitByCategory * 0.2))) {
        result
            .append(
                paintPurple(
                    String.format(
                        "- У вас заканчивается бюджет расходов на категорию \"%s\" "
                            + "(потрачено: %.1f, лимит: %.1f, потрачено %.1f%% от лимита).",
                        category,
                        expensesByCategory,
                        limitByCategory,
                        (expensesByCategory) / limitByCategory * 100)))
            .append("\n");
      }
    }
    if (!restrictedCategories.isEmpty()) {
      result
          .append(
              paintRed(
                  String.format(
                      "- Вы запретили любые расходы на категории \"%s\" "
                          + "(по ним установлен лимит: 0.0). Помните об этом!",
                      restrictedCategories.toString().replaceAll("[\\[\\]]", ""))))
          .append("\n");
    }
    return result.toString();
  }
}
