package com.github.yuyuvu.personalbudgetingapp.domainservices;

import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Класс отвечает за основную логику приложения, связанную с доходами и расходами. В нём содержатся
 * методы для добавления и удаления доходов или расходов. А также различные методы для совершения
 * расчётов на основе добавленных операций. Помимо этого, методы класса позволяют по-разному
 * фильтровать операции кошелька, получать срезы операций или итоговые суммы по срезам операций.
 */
public class WalletOperationsService {

  /**
   * Метод добавляет доход определённой категории и суммы в основной список всех операций кошелька.
   * Каждой операции также присваивается дата, время и ID.
   */
  public static void addIncome(
      Wallet wallet, double amount, String category, LocalDateTime dateTime) {
    wallet
        .getWalletOperations()
        .add(new Wallet.WalletOperation(wallet, amount, true, category, dateTime));
  }

  /**
   * Метод добавляет расход определённой категории и суммы в основной список всех операций кошелька.
   * Каждой операции также присваивается дата, время и ID.
   */
  public static void addExpense(
      Wallet wallet, double amount, String category, LocalDateTime dateTime) {
    wallet
        .getWalletOperations()
        .add(new Wallet.WalletOperation(wallet, amount, false, category, dateTime));
  }

  /**
   * Метод удаляет отдельный добавленный расход или доход (не категорию) по уникальному ID этой
   * операции из основного списка всех операций кошелька.
   */
  public static boolean removeWalletOperationById(Wallet wallet, long id) {
    return wallet.getWalletOperations().removeIf(wo -> wo.getId() == id);
  }

  /**
   * Метод отвечает за операцию перевода между кошельками. Создаёт расход у одного экземпляра
   * кошелька и доход на аналогичную сумму у другого экземпляра кошелька. <br>
   * Для упрощения тестирования он же отвечает за загрузку данных кошелька другого пользователя и
   * сохранение их после операции перевода.
   */
  public static void transferMoneyToAnotherUser(User from, String to, double amount)
      throws IOException {
    User anotherUser = DataPersistenceService.loadUserdataFromFile(to);
    addExpense(
        from.getWallet(),
        amount,
        ("переводы пользователю " + anotherUser.getUsername()).toLowerCase(),
        LocalDateTime.now());
    addIncome(
        anotherUser.getWallet(),
        amount,
        ("переводы от пользователя " + from.getUsername()).toLowerCase(),
        LocalDateTime.now());
    DataPersistenceService.saveUserdataToFile(anotherUser);
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) нужного типа (расход) и одной
   * определённой категории, возвращая суммарный расход. <br>
   * Используется для базовой аналитики по кошельку и уведомлений.
   */
  public static double getExpensesByCategory(Wallet wallet, String category) {
    return wallet.getWalletOperations().stream()
        .filter(w -> !w.isIncome())
        .filter(wo -> wo.getCategory().equals(category))
        .map(Wallet.WalletOperation::getAmount)
        .reduce(0.0, Double::sum);
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) нужного типа (расход) и категорий,
   * возвращая суммарный расход. <br>
   * Используется для расширенной фильтрации по категориям и аналитики по кошельку.
   */
  public static double getExpensesByCategories(Wallet wallet, String... categories) {
    double result = 0.0;
    for (String category : categories) {
      result += getExpensesByCategory(wallet, category);
    }
    return result;
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) нужного типа (доход) и одной
   * определённой категории, возвращая суммарный доход. <br>
   * Используется для базовой аналитики по кошельку и уведомлений.
   */
  public static double getIncomeByCategory(Wallet wallet, String category) {
    return wallet.getWalletOperations().stream()
        .filter(Wallet.WalletOperation::isIncome)
        .filter(wo -> wo.getCategory().equals(category))
        .map(Wallet.WalletOperation::getAmount)
        .reduce(0.0, Double::sum);
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) нужного типа (доход) и категорий,
   * возвращая суммарный доход. <br>
   * Используется для расширенной фильтрации по категориям и аналитики по кошельку.
   */
  public static double getIncomeByCategories(Wallet wallet, String... categories) {
    double result = 0.0;
    for (String category : categories) {
      result += getIncomeByCategory(wallet, category);
    }
    return result;
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) нужного типа (доход или расход) и
   * категорий, возвращая результирующий список. <br>
   * Используется для расширенной фильтрации по категориям и аналитики по кошельку.
   */
  public static ArrayList<Wallet.WalletOperation> getWalletOperationsByCategories(
      Wallet wallet, boolean isIncome, String... categories) {
    ArrayList<Wallet.WalletOperation> result = new ArrayList<>();
    for (String category : categories) {
      result.addAll(
          wallet.getWalletOperations().stream()
              .filter(wo -> wo.isIncome() == isIncome)
              .filter(wo -> wo.getCategory().equals(category))
              .collect(Collectors.toCollection(ArrayList::new)));
    }
    return result;
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) за нужный период, возвращая
   * результирующий список. <br>
   * Используется для расширенной фильтрации по периодам и аналитики по кошельку.
   */
  public static ArrayList<Wallet.WalletOperation> getWalletOperationsByPeriod(
      Wallet wallet, LocalDateTime periodStart, LocalDateTime periodEnd) {
    return wallet.getWalletOperations().stream()
        .filter(wo -> wo.getDateTime().isAfter(periodStart) && wo.getDateTime().isBefore(periodEnd))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) нужного типа (доход или расход), и
   * периода, возвращая результирующий список. <br>
   * Используется для расширенной фильтрации по периодам и аналитики по кошельку.
   */
  public static ArrayList<Wallet.WalletOperation> getWalletOperationsByTypeAndPeriod(
      Wallet wallet, boolean isIncome, LocalDateTime periodStart, LocalDateTime periodEnd) {
    return getWalletOperationsByPeriod(wallet, periodStart, periodEnd).stream()
        .filter(wo -> wo.isIncome() == isIncome)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Метод находит в кошельке операции (Wallet.WalletOperation) нужного типа (доход или расход),
   * категории и периода, возвращая результирующий список. <br>
   * Используется для расширенной фильтрации по периодам и аналитики по кошельку.
   */
  public static ArrayList<Wallet.WalletOperation> getWalletOperationsByTypeAndPeriodAndCategory(
      Wallet wallet,
      boolean isIncome,
      String category,
      LocalDateTime periodStart,
      LocalDateTime periodEnd) {
    return getWalletOperationsByTypeAndPeriod(wallet, isIncome, periodStart, periodEnd).stream()
        .filter(wo -> wo.getCategory().equals(category))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Метод принимает список операций дохода или список операций расхода (Wallet.WalletOperation) и
   * считает их сумму. <br>
   * Используется для расширенной фильтрации по периодам и аналитики по кошельку.
   */
  public static double getWalletOperationsAmountsSum(
      ArrayList<Wallet.WalletOperation> woList, boolean isIncome) {
    return woList.stream()
        .filter(wo -> wo.isIncome() == isIncome)
        .mapToDouble(Wallet.WalletOperation::getAmount)
        .sum();
  }
}
