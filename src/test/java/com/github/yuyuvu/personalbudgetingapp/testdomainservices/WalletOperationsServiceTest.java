package com.github.yuyuvu.personalbudgetingapp.testdomainservices;

import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов из WalletOperationsService */
public class WalletOperationsServiceTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк перед каждым тестом */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
  }

  /**
   * Проверяем, что добавление дохода корректно влияет на баланс, сумму доходов, расходов и
   * количество категорий
   */
  @Test
  void addIncomeTest() {
    WalletOperationsService.addIncome(wallet, 500, "зарплата", LocalDateTime.now());
    Assertions.assertEquals(500, wallet.getBalance());

    Assertions.assertEquals(500, wallet.getTotalIncome());
    Assertions.assertEquals(0, wallet.getTotalExpenses());

    Assertions.assertEquals(1, wallet.getWalletOperationsIncomeCategories().size());
    Assertions.assertEquals(0, wallet.getWalletOperationsExpensesCategories().size());
  }

  /**
   * Проверяем, что добавление расхода корректно влияет на баланс, сумму доходов, расходов и
   * количество категорий
   */
  @Test
  void addExpenseTest() {
    WalletOperationsService.addExpense(wallet, 3200, "еда", LocalDateTime.now());
    Assertions.assertEquals(-3200, wallet.getBalance());

    Assertions.assertEquals(0, wallet.getTotalIncome());
    Assertions.assertEquals(3200, wallet.getTotalExpenses());

    Assertions.assertEquals(0, wallet.getWalletOperationsIncomeCategories().size());
    Assertions.assertEquals(1, wallet.getWalletOperationsExpensesCategories().size());
  }

  /**
   * Проверяем, что id корректно присваивается добавленной операции. И что удаление по id корректно
   * влияет на количество операций в кошельке.
   */
  @Test
  void removeWalletOperationByIdTest() {
    WalletOperationsService.addExpense(wallet, 1500, "поездки", LocalDateTime.now());
    Assertions.assertEquals(1, wallet.getExpensesWalletOperations().size());
    Assertions.assertEquals(1, wallet.getWalletOperations().size());

    long id = wallet.getExpensesWalletOperations().get(0).getId();
    WalletOperationsService.removeWalletOperationById(wallet, id);

    Assertions.assertEquals(0, wallet.getExpensesWalletOperations().size());
    Assertions.assertEquals(0, wallet.getWalletOperations().size());
  }

  /**
   * Проверяем, что метод создаёт расход у одного пользователя и доход на аналогичную сумму у
   * другого пользователя. Метод должен работать в том числе после сохранений данных кошелька
   * другого пользователя в файловую систему и загрузок данных из неё.
   */
  @Test
  void transferMoneyToAnotherUserTest() {
    // Потенциальный текущий пользователь приложения.
    User sender = new User("username1", new String[] {"hash", "salt"});

    // Потенциальный уже имеющийся пользователь приложения, у которого есть файл кошелька.
    // Имитируем наличие файла кошелька получателя и после этого тестируем нужный метод.
    User recipient = new User("username2", new String[] {"hash", "salt"});

    // Никаких операций до теста нет
    Assertions.assertEquals(0, sender.getWallet().getWalletOperations().size());
    Assertions.assertEquals(0, recipient.getWallet().getWalletOperations().size());

    User recipientAfterTransfer = null;
    String pathWhereSaved;
    try {
      pathWhereSaved = DataPersistenceService.makeNewUserWalletFile(recipient.getUsername());
      DataPersistenceService.saveUserdataToFile(recipient);

      // Метод, который тестируется
      WalletOperationsService.transferMoneyToAnotherUser(sender, recipient.getUsername(), 15000);
      recipientAfterTransfer = DataPersistenceService.loadUserdataFromFile(recipient.getUsername());

      // Удаляем ненужный временный файл после теста
      Files.deleteIfExists(Path.of(pathWhereSaved));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }

    // Проверяем списание у текущего пользователя приложения.
    ArrayList<Wallet.WalletOperation> senderOperations =
        sender.getWallet().getExpensesWalletOperations();
    Assertions.assertEquals(1, senderOperations.size());
    Assertions.assertEquals(-15000, sender.getWallet().getBalance());
    Assertions.assertFalse(senderOperations.get(0).isIncome());

    // Проверяем получение перевода у другого пользователя приложения
    Assertions.assertNotNull(recipientAfterTransfer);
    ArrayList<Wallet.WalletOperation> recipientOperations =
        recipientAfterTransfer.getWallet().getIncomeWalletOperations();
    Assertions.assertEquals(1, recipientOperations.size());
    Assertions.assertEquals(15000, recipientAfterTransfer.getWallet().getBalance());
    Assertions.assertTrue(recipientOperations.get(0).isIncome());
  }

  /**
   * Проверяем, что сумма расходов по категории корректно считается. Вне зависимости от количества
   * добавленных операций расхода или наличия разных категорий расходов. И вне зависимости от
   * доходов (в том числе с аналогичными названиями категорий).
   */
  @Test
  void getExpensesByCategoryTest() {
    WalletOperationsService.addExpense(wallet, 3200, "еда", LocalDateTime.now());

    double factExpenses = WalletOperationsService.getExpensesByCategory(wallet, "еда");
    Assertions.assertEquals(3200, factExpenses);

    // повторное добавление
    WalletOperationsService.addExpense(wallet, 3200, "еда", LocalDateTime.now());

    // результат должен быть в 2 раза больше
    factExpenses = WalletOperationsService.getExpensesByCategory(wallet, "еда");
    Assertions.assertEquals(6400, factExpenses);

    // добавили такой же доход
    WalletOperationsService.addIncome(wallet, 3200, "еда", LocalDateTime.now());

    // результат должен быть тем же
    factExpenses = WalletOperationsService.getExpensesByCategory(wallet, "еда");
    Assertions.assertEquals(6400, factExpenses);

    // добавили расход другого типа
    WalletOperationsService.addExpense(wallet, 1000, "поездки", LocalDateTime.now());

    // результат должен быть тем же
    factExpenses = WalletOperationsService.getExpensesByCategory(wallet, "еда");
    Assertions.assertEquals(6400, factExpenses);
  }

  /**
   * Проверяем, что сумма доходов по категории корректно считается. Вне зависимости от количества
   * добавленных операций дохода или наличия разных категорий доходов. И вне зависимости от расходов
   * (в том числе с аналогичными названиями категорий).
   */
  @Test
  void getIncomeByCategoryTest() {
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());

    double factIncome = WalletOperationsService.getIncomeByCategory(wallet, "зарплата");
    Assertions.assertEquals(2500, factIncome);

    // повторное добавление
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());

    // результат должен быть в 2 раза больше
    factIncome = WalletOperationsService.getIncomeByCategory(wallet, "зарплата");
    Assertions.assertEquals(5000, factIncome);

    // добавили такой же расход
    WalletOperationsService.addExpense(wallet, 2500, "зарплата", LocalDateTime.now());

    // результат должен быть тем же
    factIncome = WalletOperationsService.getIncomeByCategory(wallet, "зарплата");
    Assertions.assertEquals(5000, factIncome);

    // добавили доход другого типа
    WalletOperationsService.addIncome(wallet, 500, "бонус", LocalDateTime.now());

    // результат должен быть тем же
    factIncome = WalletOperationsService.getIncomeByCategory(wallet, "зарплата");
    Assertions.assertEquals(5000, factIncome);
  }

  /**
   * Проверяем, что сумма расходов по нескольким категориям корректно считается. Должна работать
   * аналогично функции getExpensesByCategory с 1 категорией. А также корректно считать сумму с
   * несколькими категориями.
   */
  @Test
  void getExpensesByCategoriesTest() {
    WalletOperationsService.addExpense(wallet, 3200, "еда", LocalDateTime.now());

    double factExpenses = WalletOperationsService.getExpensesByCategories(wallet, "еда");
    Assertions.assertEquals(3200, factExpenses);

    // повторное добавление
    WalletOperationsService.addExpense(wallet, 3200, "еда", LocalDateTime.now());

    // результат должен быть в 2 раза больше
    factExpenses = WalletOperationsService.getExpensesByCategories(wallet, "еда");
    Assertions.assertEquals(6400, factExpenses);

    // добавили такой же доход
    WalletOperationsService.addIncome(wallet, 3200, "еда", LocalDateTime.now());

    // результат должен быть тем же
    factExpenses = WalletOperationsService.getExpensesByCategories(wallet, "еда");
    Assertions.assertEquals(6400, factExpenses);

    // добавили расход другого типа
    WalletOperationsService.addExpense(wallet, 1000, "поездки", LocalDateTime.now());

    // результат должен быть тем же
    factExpenses = WalletOperationsService.getExpensesByCategories(wallet, "еда");
    Assertions.assertEquals(6400, factExpenses);

    // добавили ещё 2 расхода других категорий
    WalletOperationsService.addExpense(wallet, 600, "подарки", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 700, "транспорт", LocalDateTime.now());

    // проверка по 4 категориям
    factExpenses =
        WalletOperationsService.getExpensesByCategories(
            wallet, "еда", "поездки", "подарки", "транспорт");
    Assertions.assertEquals(8700, factExpenses);
  }

  /**
   * Проверяем, что сумма доходов по категории корректно считается. Вне зависимости от количества
   * добавленных операций дохода или наличия разных категорий доходов. И вне зависимости от расходов
   * (в том числе с аналогичными названиями категорий).
   */
  @Test
  void getIncomeByCategoriesTest() {
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());

    double factIncome = WalletOperationsService.getIncomeByCategories(wallet, "зарплата");
    Assertions.assertEquals(2500, factIncome);

    // повторное добавление
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());

    // результат должен быть в 2 раза больше
    factIncome = WalletOperationsService.getIncomeByCategories(wallet, "зарплата");
    Assertions.assertEquals(5000, factIncome);

    // добавили такой же расход
    WalletOperationsService.addExpense(wallet, 2500, "зарплата", LocalDateTime.now());

    // результат должен быть тем же
    factIncome = WalletOperationsService.getIncomeByCategories(wallet, "зарплата");
    Assertions.assertEquals(5000, factIncome);

    // добавили доход другого типа
    WalletOperationsService.addIncome(wallet, 500, "бонус", LocalDateTime.now());

    // результат должен быть тем же
    factIncome = WalletOperationsService.getIncomeByCategories(wallet, "зарплата");
    Assertions.assertEquals(5000, factIncome);

    // добавили ещё 2 дохода других категорий
    WalletOperationsService.addIncome(wallet, 200, "субсидия", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 650, "льгота", LocalDateTime.now());

    // проверка по 4 категориям
    factIncome =
        WalletOperationsService.getIncomeByCategories(
            wallet, "зарплата", "бонус", "субсидия", "льгота");
    Assertions.assertEquals(6350, factIncome);
  }

  /**
   * Проверяем, что в конечный список попадает нужное количество операций и все нужного типа. И что
   * операции других типов или категорий никак не влияют на результат.
   */
  @Test
  void getWalletOperationsByCategoriesTest() {
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());

    // потенциальные ошибки
    WalletOperationsService.addIncome(wallet, 920, "ошибка", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 2500, "зарплата", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 3000, "траты", LocalDateTime.now());

    // проверка на 1 категории доходов
    ArrayList<Wallet.WalletOperation> result =
        WalletOperationsService.getWalletOperationsByCategories(wallet, true, "зарплата");

    // в результате нужное количество операций
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(3, result.size());

    // в результате нужный тип, категория и сумма операций
    Assertions.assertTrue(result.stream().allMatch(Wallet.WalletOperation::isIncome));
    Assertions.assertTrue(result.stream().allMatch(wo -> wo.getCategory().equals("зарплата")));
    Assertions.assertEquals(
        7500,
        result.stream().map(Wallet.WalletOperation::getAmount).mapToDouble(amount -> amount).sum());

    // проверка на нескольких категориях расходов
    result =
        WalletOperationsService.getWalletOperationsByCategories(wallet, false, "зарплата", "траты");

    // в результате нужное количество операций
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(2, result.size());

    // в результате нужный тип, категория и сумма операций
    Assertions.assertTrue(result.stream().noneMatch(Wallet.WalletOperation::isIncome));
    Assertions.assertEquals(
        2,
        result.stream()
            .filter(wo -> wo.getCategory().equals("зарплата") || wo.getCategory().equals("траты"))
            .toList()
            .size());
    Assertions.assertEquals(
        5500,
        result.stream().map(Wallet.WalletOperation::getAmount).mapToDouble(amount -> amount).sum());
  }

  /**
   * Проверяем, что в конечный список попадает нужное количество операций. И что туда попадают любые
   * типы операций любых категорий.
   */
  @Test
  void getWalletOperationsByPeriodTest() {
    // В нужном периоде должны быть 4 операции из 6
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2020, 5, 6, 15, 15));

    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2021, 6, 11, 19, 15));
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2022, 7, 10, 18, 15));
    WalletOperationsService.addIncome(wallet, 920, "ошибка", LocalDateTime.of(2023, 8, 12, 15, 15));
    WalletOperationsService.addExpense(
        wallet, 2500, "зарплата", LocalDateTime.of(2024, 9, 15, 22, 30));

    WalletOperationsService.addExpense(wallet, 3000, "траты", LocalDateTime.of(2026, 5, 6, 15, 15));

    // проверка на границах за 1 минуту до и после операций
    ArrayList<Wallet.WalletOperation> result =
        WalletOperationsService.getWalletOperationsByPeriod(
            wallet, LocalDateTime.of(2021, 6, 11, 19, 14), LocalDateTime.of(2024, 9, 15, 22, 31));

    // в результате нужное количество операций (4)
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(4, result.size());

    // в результате любые типы и нужная сумма операций (8420)
    Assertions.assertTrue(result.stream().anyMatch(Wallet.WalletOperation::isIncome));
    Assertions.assertTrue(result.stream().anyMatch(wo -> !wo.isIncome()));
    Assertions.assertEquals(
        8420,
        result.stream().map(Wallet.WalletOperation::getAmount).mapToDouble(amount -> amount).sum());
  }

  /**
   * Проверяем, что в конечный список попадает нужное количество операций. И что туда попадают
   * только доходы или только расходы любых категорий.
   */
  @Test
  void getWalletOperationsByTypeAndPeriodTest() {
    // В нужном периоде должны быть 2 последние операции из 6
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2020, 5, 6, 15, 15));
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2021, 6, 11, 19, 15));
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2022, 7, 10, 18, 15));

    WalletOperationsService.addIncome(wallet, 920, "ошибка", LocalDateTime.of(2023, 8, 12, 15, 15));
    WalletOperationsService.addExpense(
        wallet, 2500, "зарплата", LocalDateTime.of(2024, 9, 15, 22, 30));
    WalletOperationsService.addExpense(wallet, 3000, "траты", LocalDateTime.of(2026, 5, 6, 15, 15));

    // проверка на разных границах до и после операций
    ArrayList<Wallet.WalletOperation> result =
        WalletOperationsService.getWalletOperationsByTypeAndPeriod(
            wallet,
            false,
            LocalDateTime.of(2023, 1, 1, 1, 14),
            LocalDateTime.of(2026, 5, 6, 15, 16));

    // в результате нужное количество операций (2)
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(2, result.size());

    // в результате только расходы и нужная сумма операций (5500)
    Assertions.assertTrue(result.stream().noneMatch(Wallet.WalletOperation::isIncome));
    Assertions.assertEquals(
        5500,
        result.stream().map(Wallet.WalletOperation::getAmount).mapToDouble(amount -> amount).sum());
  }

  /**
   * Проверяем, что в конечный список попадает нужное количество операций. И что туда попадают
   * только доходы или только расходы только нужной категории.
   */
  @Test
  void getWalletOperationsByTypeAndPeriodAndCategoryTest() {
    // В нужном периоде должна быть 1 последняя операция из 6
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2020, 5, 6, 15, 15));
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2021, 6, 11, 19, 15));
    WalletOperationsService.addIncome(
        wallet, 2500, "зарплата", LocalDateTime.of(2022, 7, 10, 18, 15));

    WalletOperationsService.addIncome(wallet, 920, "ошибка", LocalDateTime.of(2023, 8, 12, 15, 15));
    WalletOperationsService.addExpense(
        wallet, 2500, "зарплата", LocalDateTime.of(2024, 9, 15, 22, 30));
    WalletOperationsService.addExpense(wallet, 3000, "траты", LocalDateTime.of(2026, 5, 6, 15, 15));

    // проверка на разных границах до и после операций
    ArrayList<Wallet.WalletOperation> result =
        WalletOperationsService.getWalletOperationsByTypeAndPeriodAndCategory(
            wallet,
            false,
            "траты",
            LocalDateTime.of(2023, 1, 1, 1, 14),
            LocalDateTime.of(2026, 5, 6, 15, 16));

    // в результате нужное количество операций (1)
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(1, result.size());

    // в результате только расходы и нужная сумма операций (3000)
    Assertions.assertTrue(result.stream().noneMatch(Wallet.WalletOperation::isIncome));
    Assertions.assertTrue(result.stream().allMatch(wo -> wo.getCategory().equals("траты")));
    Assertions.assertEquals(
        3000,
        result.stream().map(Wallet.WalletOperation::getAmount).mapToDouble(amount -> amount).sum());
  }

  /** Проверяем, что сумма по операциям разных типов корректно считается. */
  @Test
  void getWalletOperationsAmountsSumTest() {
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 2500, "зарплата", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 920, "ошибка", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 2500, "зарплата", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 3000, "траты", LocalDateTime.now());

    // баланс по доходам и расходам сходится
    Assertions.assertEquals(
        wallet.getBalance(),
        WalletOperationsService.getWalletOperationsAmountsSum(wallet.getWalletOperations(), true)
            - WalletOperationsService.getWalletOperationsAmountsSum(
                wallet.getWalletOperations(), false));

    // итог по доходам сходится
    Assertions.assertEquals(
        wallet.getTotalIncome(),
        WalletOperationsService.getWalletOperationsAmountsSum(wallet.getWalletOperations(), true));

    // итог по расходам сходится
    Assertions.assertEquals(
        wallet.getTotalExpenses(),
        WalletOperationsService.getWalletOperationsAmountsSum(wallet.getWalletOperations(), false));
  }
}
