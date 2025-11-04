package com.github.yuyuvu.personalbudgetingapp.unit.testdomainservices;

import com.github.yuyuvu.personalbudgetingapp.domainservices.BudgetingService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.time.LocalDateTime;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов из BudgetingService. */
public class BudgetingServiceTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк и нужную локаль перед каждым тестом. */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
    Locale.setDefault(Locale.US);
  }

  /** Проверяем метод для проверки наличия лимита по категории расходов. */
  @Test
  void checkExpensesCategoryLimitExistenceTest() {
    // Добавляем лимит
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    Assertions.assertTrue(BudgetingService.checkExpensesCategoryLimitExistence(wallet, "покупки"));

    // Удаляем лимит
    BudgetingService.removeExpensesCategoryLimit(wallet, "покупки");
    Assertions.assertFalse(BudgetingService.checkExpensesCategoryLimitExistence(wallet, "покупки"));
  }

  /** Проверяем метод для добавления лимита по категории расходов. */
  @Test
  void addNewExpensesCategoryLimitTest() {
    // Добавляем лимит
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    Assertions.assertEquals(1, wallet.getBudgetCategoriesAndLimits().size());
    BudgetingService.addNewExpensesCategoryLimit(wallet, "еда", 2000);
    Assertions.assertEquals(2, wallet.getBudgetCategoriesAndLimits().size());

    // Проверяем выброс ошибки при отрицательном лимите
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> BudgetingService.addNewExpensesCategoryLimit(wallet, "бензин", -5000));
    Assertions.assertDoesNotThrow(
        () -> BudgetingService.addNewExpensesCategoryLimit(wallet, "что-то", 0));
  }

  /** Проверяем метод для удаления лимита по категории расходов. */
  @Test
  void removeExpensesCategoryLimitTest() {
    // Добавляем лимит
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    Assertions.assertTrue(BudgetingService.checkExpensesCategoryLimitExistence(wallet, "покупки"));

    // Удаляем лимит
    BudgetingService.removeExpensesCategoryLimit(wallet, "покупки");
    Assertions.assertFalse(BudgetingService.checkExpensesCategoryLimitExistence(wallet, "покупки"));
    Assertions.assertNull(wallet.getBudgetCategoriesAndLimits().get("покупки"));

    // Проверяем выброс ошибки
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> BudgetingService.removeExpensesCategoryLimit(wallet, "еда"));
  }

  /** Проверяем метод для изменения лимита по категории расходов. */
  @Test
  void changeLimitForCategoryTest() {
    // Добавляем лимит
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    Assertions.assertTrue(BudgetingService.checkExpensesCategoryLimitExistence(wallet, "покупки"));

    // Изменяем лимит
    BudgetingService.changeLimitForCategory(wallet, "покупки", 5000);
    Assertions.assertTrue(BudgetingService.checkExpensesCategoryLimitExistence(wallet, "покупки"));
    Assertions.assertEquals(5000, wallet.getBudgetCategoriesAndLimits().get("покупки"));

    // Проверяем выброс ошибок
    // Не существует категория
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> BudgetingService.changeLimitForCategory(wallet, "еда", 5000));
    // Неправильная сумма
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> BudgetingService.changeLimitForCategory(wallet, "покупки", -2000));
  }

  /** Проверяем метод для получения лимита по категории расходов. */
  @Test
  void getLimitByCategoryTest() {
    // Добавляем лимит
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    Assertions.assertEquals(2000, BudgetingService.getLimitByCategory(wallet, "покупки"));

    // Изменяем лимит
    BudgetingService.changeLimitForCategory(wallet, "покупки", 5000);
    Assertions.assertEquals(5000, BudgetingService.getLimitByCategory(wallet, "покупки"));

    // Проверяем выброс ошибок
    // Не существует категория
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> BudgetingService.getLimitByCategory(wallet, "еда"));
  }

  /** Проверяем метод для получения лимита по нескольким категориям расходов. */
  @Test
  void getLimitByCategoriesTest() {
    // Добавляем лимиты
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    BudgetingService.addNewExpensesCategoryLimit(wallet, "еда", 2000);
    BudgetingService.addNewExpensesCategoryLimit(wallet, "бензин", 2000);
    Assertions.assertEquals(2000, BudgetingService.getLimitByCategories(wallet, true, "покупки"));
    Assertions.assertEquals(
        6000, BudgetingService.getLimitByCategories(wallet, true, "покупки", "еда", "бензин"));

    // Изменяем лимит
    BudgetingService.changeLimitForCategory(wallet, "покупки", 0);
    Assertions.assertEquals(0, BudgetingService.getLimitByCategories(wallet, true, "покупки"));
    Assertions.assertEquals(
        4000, BudgetingService.getLimitByCategories(wallet, true, "покупки", "еда", "бензин"));

    // Проверяем выброс ошибок
    // Не существует категория
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> BudgetingService.getLimitByCategories(wallet, true, "что-то"));
    // С false не выбрасывает
    Assertions.assertDoesNotThrow(
        () -> BudgetingService.getLimitByCategories(wallet, false, "что-то"));
  }

  /** Проверяем метод для получения остатка лимита по категории расходов. */
  @Test
  void getRemainderByCategoryTest() {
    // Добавляем лимит и траты
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    WalletOperationsService.addExpense(wallet, 500, "покупки", LocalDateTime.now());
    Assertions.assertEquals(1500, BudgetingService.getRemainderByCategory(wallet, "покупки"));

    // Изменяем лимит и траты
    BudgetingService.changeLimitForCategory(wallet, "покупки", 5000);
    WalletOperationsService.addExpense(wallet, 500, "покупки", LocalDateTime.now());
    Assertions.assertEquals(4000, BudgetingService.getRemainderByCategory(wallet, "покупки"));

    BudgetingService.changeLimitForCategory(wallet, "покупки", 0);
    Assertions.assertEquals(-1000, BudgetingService.getRemainderByCategory(wallet, "покупки"));

    // Проверяем выброс ошибок
    // Не существует категория
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> BudgetingService.getRemainderByCategory(wallet, "еда"));
  }

  /** Проверяем метод для получения лимита по нескольким категориям расходов. */
  @Test
  void getRemainderByCategoriesTest() {
    // Добавляем лимиты и траты
    BudgetingService.addNewExpensesCategoryLimit(wallet, "покупки", 2000);
    BudgetingService.addNewExpensesCategoryLimit(wallet, "еда", 2000);
    BudgetingService.addNewExpensesCategoryLimit(wallet, "бензин", 2000);
    WalletOperationsService.addExpense(wallet, 500, "покупки", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 700, "еда", LocalDateTime.now());
    Assertions.assertEquals(
        1500, BudgetingService.getRemainderByCategories(wallet, true, "покупки"));
    Assertions.assertEquals(
        4800, BudgetingService.getRemainderByCategories(wallet, true, "покупки", "еда", "бензин"));

    // Изменяем лимит
    BudgetingService.changeLimitForCategory(wallet, "покупки", 0);
    WalletOperationsService.addExpense(wallet, 5000, "покупки", LocalDateTime.now());
    Assertions.assertEquals(
        -5500, BudgetingService.getRemainderByCategories(wallet, true, "покупки"));
    Assertions.assertEquals(
        -2200, BudgetingService.getRemainderByCategories(wallet, true, "покупки", "еда", "бензин"));

    // Проверяем выброс ошибок
    // Не существует категория
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> BudgetingService.getRemainderByCategories(wallet, true, "что-то"));
    // С false не выбрасывает
    Assertions.assertDoesNotThrow(
        () -> BudgetingService.getRemainderByCategories(wallet, false, "что-то"));
  }
}
