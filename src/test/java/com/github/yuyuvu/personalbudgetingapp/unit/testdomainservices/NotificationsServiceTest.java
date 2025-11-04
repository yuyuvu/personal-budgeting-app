package com.github.yuyuvu.personalbudgetingapp.unit.testdomainservices;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.deleteColorsFromString;

import com.github.yuyuvu.personalbudgetingapp.domainservices.BudgetingService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.NotificationsService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.time.LocalDateTime;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов из NotificationsService. */
public class NotificationsServiceTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк и нужную локаль перед каждым тестом. */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
    Locale.setDefault(Locale.US);
  }

  /** Проверяем метод, формирующий сообщение с проверкой отношения доходов к расходам. */
  @Test
  void checkBalanceConsumptionTest() {
    // нет операций
    String[] result =
        deleteColorsFromString(NotificationsService.checkBalanceConsumption(wallet)).split("\n");
    Assertions.assertEquals(
        "- У вас пока нет добавленных доходов или расходов. У вас нулевой баланс.", result[0]);

    // отрицательный баланс
    WalletOperationsService.addExpense(wallet, 1000, "расход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkBalanceConsumption(wallet)).split("\n");
    Assertions.assertEquals(
        "- Ваши расходы превысили ваши доходы. У вас отрицательный баланс.", result[0]);

    // нулевой баланс
    wallet.getWalletOperations().clear();
    WalletOperationsService.addExpense(wallet, 100, "расход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 100, "доход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkBalanceConsumption(wallet)).split("\n");
    Assertions.assertEquals(
        "- Ваши расходы достигли величины ваших доходов. У вас нулевой баланс.", result[0]);

    // 80%
    wallet.getWalletOperations().clear();
    WalletOperationsService.addExpense(wallet, 81, "расход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 100, "доход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkBalanceConsumption(wallet)).split("\n");
    Assertions.assertEquals("- Ваши расходы превысили отметку в 80% от ваших доходов.", result[0]);

    // 70%
    wallet.getWalletOperations().clear();
    WalletOperationsService.addExpense(wallet, 71, "расход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 100, "доход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkBalanceConsumption(wallet)).split("\n");
    Assertions.assertEquals("- Ваши расходы превысили отметку в 70% от ваших доходов.", result[0]);

    // много доходов
    wallet.getWalletOperations().clear();
    WalletOperationsService.addExpense(wallet, 1, "расход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 1000, "доход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkBalanceConsumption(wallet)).split("\n");
    Assertions.assertEquals(
        "- Ваши доходы значительно превышают ваши расходы: в 1000.0 раз. Так держать!", result[0]);

    // много расходов
    wallet.getWalletOperations().clear();
    WalletOperationsService.addExpense(wallet, 1000, "расход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 1, "доход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkBalanceConsumption(wallet)).split("\n");
    Assertions.assertEquals(
        "- Ваши расходы значительно превышают ваши доходы: в 1000.0 раз. "
            + "У вас отрицательный баланс.",
        result[0]);
  }

  /**
   * Проверяем метод, формирующий сообщение о влиянии отдельных категорий на итоговую сумму доходов
   * или расходов.
   */
  @Test
  void checkCategoriesImportanceTest() {
    // 100% доход
    WalletOperationsService.addIncome(wallet, 510, "доход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 490, "доход", LocalDateTime.now());
    String[] result =
        deleteColorsFromString(NotificationsService.checkCategoriesImportance(wallet)).split("\n");
    Assertions.assertEquals(
        "- Категория доходов \"доход\" значительно влияет на общую сумму доходов: "
            + "она составляет 100.0% от них.",
        result[0]);

    // 66% и 34% доходов
    wallet.getWalletOperations().clear();
    WalletOperationsService.addIncome(wallet, 660, "доход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 340, "недоход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkCategoriesImportance(wallet)).split("\n");
    Assertions.assertEquals(
        "- Категория доходов \"доход\" значительно влияет на общую сумму доходов: "
            + "она составляет 66.0% от них.",
        result[0]);
    String[] finalResult = result;
    Assertions.assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> {
          String s = finalResult[1];
        });

    // 100% расход
    wallet.getWalletOperations().clear();
    WalletOperationsService.addExpense(wallet, 510, "расход", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 490, "расход", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkCategoriesImportance(wallet)).split("\n");
    Assertions.assertEquals(
        "- Категория расходов \"расход\" значительно влияет на общую сумму расходов: "
            + "она составляет 100.0% от них.",
        result[0]);

    // 42% и 42% расходов
    wallet.getWalletOperations().clear();
    WalletOperationsService.addExpense(wallet, 420, "расход", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 420, "расход2", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 160, "расход3", LocalDateTime.now());
    result =
        deleteColorsFromString(NotificationsService.checkCategoriesImportance(wallet)).split("\n");
    Assertions.assertEquals(
        "- Категория расходов \"расход\" значительно влияет на общую сумму расходов: "
            + "она составляет 42.0% от них.",
        result[0]);
    Assertions.assertEquals(
        "- Категория расходов \"расход2\" значительно влияет на общую сумму расходов: "
            + "она составляет 42.0% от них.",
        result[1]);
    String[] finalResult2 = result;
    Assertions.assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> {
          String s = finalResult2[2];
        });
  }

  /** Проверяем метод, формирующий сообщение об израсходовании лимитов по категориям расходов. */
  @Test
  void checkBudgetLimitsConsumptionTest() {
    // перерасход и запрет
    WalletOperationsService.addExpense(wallet, 100, "расход", LocalDateTime.now());
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход", 0);
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход2", 0);
    String[] result =
        deleteColorsFromString(NotificationsService.checkBudgetLimitsConsumption(wallet))
            .split("\n");
    Assertions.assertEquals(
        "- Вы вышли за бюджет расходов на категорию \"расход\" "
            + "(потрачено: 100.0, лимит: 0.0, перерасход: 100.0).",
        result[0]);
    Assertions.assertEquals(
        "- Вы запретили любые расходы на категории \"расход, расход2\" "
            + "(по ним установлен лимит: 0.0). Помните об этом!",
        result[1]);

    // нулевой остаток бюджета
    wallet.getWalletOperations().clear();
    wallet.getBudgetCategoriesAndLimits().clear();
    WalletOperationsService.addExpense(wallet, 100, "расход", LocalDateTime.now());
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход", 100);
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход2", 0);
    result =
        deleteColorsFromString(NotificationsService.checkBudgetLimitsConsumption(wallet))
            .split("\n");
    Assertions.assertEquals(
        "- Вы израсходовали бюджет расходов на категорию \"расход\" "
            + "(потрачено: 100.0, лимит: 100.0). "
            + "У вас нулевой остаток бюджета по ней.",
        result[0]);
    Assertions.assertEquals(
        "- Вы запретили любые расходы на категории \"расход2\" "
            + "(по ним установлен лимит: 0.0). Помните об этом!",
        result[1]);

    // приближение к израсходованию бюджета
    wallet.getWalletOperations().clear();
    wallet.getBudgetCategoriesAndLimits().clear();
    WalletOperationsService.addExpense(wallet, 81.5, "расход", LocalDateTime.now());
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход", 100);
    WalletOperationsService.addExpense(wallet, 83.5, "расход2", LocalDateTime.now());
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход2", 100);
    result =
        deleteColorsFromString(NotificationsService.checkBudgetLimitsConsumption(wallet))
            .split("\n");
    Assertions.assertEquals(
        "- У вас заканчивается бюджет расходов на категорию \"расход\" "
            + "(потрачено: 81.5, лимит: 100.0, потрачено 81.5% от лимита).",
        result[0]);
    Assertions.assertEquals(
        "- У вас заканчивается бюджет расходов на категорию \"расход2\" "
            + "(потрачено: 83.5, лимит: 100.0, потрачено 83.5% от лимита).",
        result[1]);
    String[] finalResult = result;
    Assertions.assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> {
          String s = finalResult[2];
        });
  }

  /** Проверяем метод, формирующий сообщение со всеми уведомлениями. */
  @Test
  void checkAndPrepareNotificationsTest() {
    // Сервис не должен всегда выводить какие-то уведомления
    // Например, в данном случае
    WalletOperationsService.addIncome(wallet, 500, "доход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 500, "доход2", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 500, "доход3", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 333, "расход", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 340, "расход2", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 340, "расход3", LocalDateTime.now());

    Assertions.assertEquals("", NotificationsService.checkAndPrepareNotifications(wallet));
  }
}
