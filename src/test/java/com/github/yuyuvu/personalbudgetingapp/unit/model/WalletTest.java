package com.github.yuyuvu.personalbudgetingapp.unit.model;

import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов из Wallet. */
public class WalletTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк перед каждым тестом. */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
  }

  /** Проверяем метод для получения суммы доходов. */
  @Test
  void getTotalIncomeTest() {
    for (int i = 1; i <= 50; i++) {
      WalletOperationsService.addIncome(wallet, 50, "доход", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 30, "расход", LocalDateTime.now());
    }
    double totalIncome = 0;
    for (Wallet.WalletOperation wo : wallet.getWalletOperations()) {
      if (wo.isIncome()) {
        totalIncome += wo.getAmount();
      }
    }
    Assertions.assertEquals(2500, wallet.getTotalIncome());
    Assertions.assertEquals(totalIncome, wallet.getTotalIncome());
  }

  /** Проверяем метод для получения суммы расходов. */
  @Test
  void getTotalExpensesTest() {
    for (int i = 1; i <= 50; i++) {
      WalletOperationsService.addIncome(wallet, 50, "доход", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 30, "расход", LocalDateTime.now());
    }
    double totalExpenses = 0;
    for (Wallet.WalletOperation wo : wallet.getWalletOperations()) {
      if (!wo.isIncome()) {
        totalExpenses += wo.getAmount();
      }
    }
    Assertions.assertEquals(1500, wallet.getTotalExpenses());
    Assertions.assertEquals(totalExpenses, wallet.getTotalExpenses());
  }

  /** Проверяем метод для получения всех операций дохода. */
  @Test
  void getIncomeWalletOperationsTest() {
    for (int i = 1; i <= 50; i++) {
      WalletOperationsService.addIncome(wallet, 50, "доход", LocalDateTime.now());
      WalletOperationsService.addIncome(wallet, 20, "доход2", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 30, "расход", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 10, "расход2", LocalDateTime.now());
    }
    boolean gotExpenses = false;
    for (Wallet.WalletOperation wo : wallet.getIncomeWalletOperations()) {
      if (!wo.isIncome()) {
        gotExpenses = true;
        break;
      }
    }
    Assertions.assertFalse(gotExpenses);
  }

  /** Проверяем метод для получения всех операций расхода. */
  @Test
  void getExpensesWalletOperationsTest() {
    for (int i = 1; i <= 50; i++) {
      WalletOperationsService.addIncome(wallet, 50, "доход", LocalDateTime.now());
      WalletOperationsService.addIncome(wallet, 20, "доход2", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 30, "расход", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 10, "расход2", LocalDateTime.now());
    }
    boolean gotIncome = false;
    for (Wallet.WalletOperation wo : wallet.getExpensesWalletOperations()) {
      if (wo.isIncome()) {
        gotIncome = true;
        break;
      }
    }
    Assertions.assertFalse(gotIncome);
  }

  /** Проверяем метод для получения всех категорий расхода. */
  @Test
  void getWalletOperationsExpensesCategoriesTest() {
    for (int i = 1; i <= 50; i++) {
      WalletOperationsService.addIncome(wallet, 50, "доход", LocalDateTime.now());
      WalletOperationsService.addIncome(wallet, 20, "доход2", LocalDateTime.now());

      WalletOperationsService.addExpense(wallet, 30, "расход", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 10, "расход2", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 10, "расход3", LocalDateTime.now());
    }
    Assertions.assertEquals(3, wallet.getWalletOperationsExpensesCategories().size());
    Assertions.assertTrue(wallet.getWalletOperationsExpensesCategories().contains("расход"));
    Assertions.assertTrue(wallet.getWalletOperationsExpensesCategories().contains("расход2"));
    Assertions.assertTrue(wallet.getWalletOperationsExpensesCategories().contains("расход3"));
  }

  /** Проверяем метод для получения всех категорий расхода. */
  @Test
  void getWalletOperationsIncomeCategoriesTest() {
    for (int i = 1; i <= 50; i++) {
      WalletOperationsService.addIncome(wallet, 50, "доход", LocalDateTime.now());
      WalletOperationsService.addIncome(wallet, 20, "доход2", LocalDateTime.now());

      WalletOperationsService.addExpense(wallet, 30, "расход", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 10, "расход2", LocalDateTime.now());
      WalletOperationsService.addExpense(wallet, 10, "расход3", LocalDateTime.now());
    }
    Assertions.assertEquals(2, wallet.getWalletOperationsIncomeCategories().size());
    Assertions.assertTrue(wallet.getWalletOperationsIncomeCategories().contains("доход"));
    Assertions.assertTrue(wallet.getWalletOperationsIncomeCategories().contains("доход2"));
  }

  /** Упрощённо без Random и сида проверяем метод для генерации ID. */
  @Test
  void generateNewWalletOperationIdTest() {
    for (int i = 1; i <= 400; i++) {
      WalletOperationsService.addExpense(wallet, 50, "доход", LocalDateTime.now());
    }
    // получаем множество операций, у всех разные ID
    long result =
        wallet.getWalletOperations().stream().map(Wallet.WalletOperation::getId).distinct().count();
    Assertions.assertEquals(400, result);
  }
}
