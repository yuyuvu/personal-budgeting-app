package com.github.yuyuvu.personalbudgetingapp.unit.testdomainservices;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.deleteColorsFromString;

import com.github.yuyuvu.personalbudgetingapp.domainservices.AnalyticsService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.BudgetingService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов из AnalyticsService. */
public class AnalyticsServiceTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк перед каждым тестом. */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
  }

  /** Проверяем метод собирающий основной отчёт-сводку по всем данным кошелька. */
  @Test
  void makeTotalSummaryTest() {
    // нет операций
    String[] result = deleteColorsFromString(AnalyticsService.makeTotalSummary(wallet)).split("\n");
    Assertions.assertEquals("Баланс доходов и расходов: 0.00", result[0]);
    Assertions.assertEquals("Общие доходы: 0.00", result[1]);
    Assertions.assertEquals("Доходы по категориям:", result[2]);
    Assertions.assertEquals(
        "\tНет добавленных категорий доходов, "
            + "добавьте новые операции в меню управления доходами и расходами.",
        result[3]);
    Assertions.assertEquals("Общие расходы: 0.00", result[4]);
    Assertions.assertEquals("Расходы по категориям:", result[5]);
    Assertions.assertEquals(
        "\tНет добавленных категорий расходов, "
            + "добавьте новые операции в меню управления доходами и расходами.",
        result[6]);
    Assertions.assertEquals("Бюджет по категориям:", result[7]);
    Assertions.assertEquals(
        "\tНе найдены добавленные категории бюджета с установленными лимитами.", result[8]);
    Assertions.assertEquals("\tДобавьте их в меню управления категориями.", result[9]);

    String[] finalResult1 = result;
    Assertions.assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> {
          String s = finalResult1[10];
        });

    // 5 операций
    wallet.getWalletOperations().clear();
    WalletOperationsService.addIncome(wallet, 510, "доход1", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 490, "доход1", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 420, "расход", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 430, "расход2", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 260, "расход3", LocalDateTime.now());
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход2", 100);

    result = deleteColorsFromString(AnalyticsService.makeTotalSummary(wallet)).split("\n");
    Assertions.assertEquals("Баланс доходов и расходов: -110.00", result[0]);
    Assertions.assertEquals("Общие доходы: 1000.00", result[1]);
    Assertions.assertEquals("Доходы по категориям:", result[2]);
    Assertions.assertEquals("\t- Доход1: 1000.00", result[3]);
    Assertions.assertEquals("Общие расходы: 1110.00", result[4]);
    Assertions.assertEquals("Расходы по категориям:", result[5]);
    Assertions.assertEquals("\t- Расход: 420.00", result[6]);
    Assertions.assertEquals("\t- Расход2: 430.00", result[7]);
    Assertions.assertEquals("\t- Расход3: 260.00", result[8]);
    Assertions.assertEquals("Бюджет по категориям:", result[9]);
    Assertions.assertEquals("\t- Расход2: 100.00. Оставшийся бюджет: -330.0", result[10]);

    String[] finalResult = result;
    Assertions.assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> {
          String s = finalResult[11];
        });
  }

  /** Проверяем метод собирающий отчёт с фильтрацией операций по периоду. */
  @Test
  void makeSummaryByPeriodTest() {
    // 5 операций и неправильный период
    WalletOperationsService.addIncome(wallet, 510, "доход1", LocalDateTime.of(2020, 5, 5, 11, 11));
    WalletOperationsService.addIncome(wallet, 490, "доход1", LocalDateTime.of(2021, 5, 5, 11, 11));
    WalletOperationsService.addIncome(wallet, 420, "расход", LocalDateTime.of(2022, 5, 5, 11, 11));
    WalletOperationsService.addExpense(
        wallet, 430, "расход2", LocalDateTime.of(2023, 5, 5, 11, 11));
    WalletOperationsService.addExpense(
        wallet, 260, "расход3", LocalDateTime.of(2024, 5, 5, 11, 11));
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход2", 100);

    String[] result =
        deleteColorsFromString(
                AnalyticsService.makeSummaryByPeriod(
                    wallet,
                    LocalDateTime.of(2018, 5, 5, 11, 11),
                    LocalDateTime.of(2019, 5, 5, 11, 11)))
            .split("\n");
    Assertions.assertEquals(
        "Нет добавленных операций дохода или расхода за указанный период.", result[1]);
    Assertions.assertEquals(
        "Добавьте новые операции в меню управления доходами и расходами.", result[2]);

    String[] finalResult1 = result;
    Assertions.assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> {
          String s = finalResult1[3];
        });

    // правильный период и другой результат
    result =
        deleteColorsFromString(
                AnalyticsService.makeSummaryByPeriod(
                    wallet,
                    LocalDateTime.of(2020, 5, 5, 11, 11),
                    LocalDateTime.of(2024, 5, 5, 11, 11)))
            .split("\n");

    // правильная сумма доходов
    Assertions.assertEquals("Общие доходы за указанный период: 910.00", result[1]);
    Assertions.assertNotEquals(
        "Нет добавленных операций дохода или расхода за указанный период.", result[1]);
    Assertions.assertNotEquals(
        "Добавьте новые операции в меню управления доходами и расходами.", result[2]);

    String[] finalResult = result;
    Assertions.assertDoesNotThrow(
        () -> {
          String s = finalResult[3];
        });
  }
}
