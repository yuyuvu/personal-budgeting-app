package com.github.yuyuvu.personalbudgetingapp.testdomainservices;

import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BudgetingServiceTest {
  Wallet wallet;

//  /** Подготавливаем пустой кошелёк перед каждым тестом */
//  @BeforeEach
//  void prepareNewWallet() {
//    wallet = new Wallet(false);
//  }
//
//  /**
//   * Проверяем, что добавление дохода корректно влияет на баланс, сумму доходов, расходов и
//   * количество категорий
//   */
//  @Test
//  void addIncomeTest() {
//    WalletOperationsService.addIncome(wallet, 500, "зарплата", LocalDateTime.now());
//    Assertions.assertEquals(500, wallet.getBalance());
//
//    Assertions.assertEquals(500, wallet.getTotalIncome());
//    Assertions.assertEquals(0, wallet.getTotalExpenses());
//
//    Assertions.assertEquals(1, wallet.getWalletOperationsIncomeCategories().size());
//    Assertions.assertEquals(0, wallet.getWalletOperationsExpensesCategories().size());
//  }
}
