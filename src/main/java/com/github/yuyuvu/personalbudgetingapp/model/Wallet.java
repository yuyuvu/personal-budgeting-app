package com.github.yuyuvu.personalbudgetingapp.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Объекты класса Wallet хранят: <br>
 * <br>
 * 1) список из отдельных операций доходов и расходов -. ArrayList<WalletOperation>
 * walletOperations; <br>
 * <br>
 * 2) хэш-таблицу с названиями категорий расходов, на которые установлен бюджет, с соответствующими
 * заданными бюджетами - HashMap < String, Double > budgetCategoriesAndLimits. <br>
 * <br>
 * Объект Wallet создаётся в конструкторе пользователя и привязывается к нему. Может быть получен
 * через getWallet класса User. Также в класс Wallet вложен статический класс WalletOperation,
 * представляющий отдельную операцию дохода или расхода. <br>
 * <br>
 * Помимо этого, у объекта Wallet можно вызвать методы, описывающие ключевые характеристики
 * кошелька: баланс, сумму всех доходов, сумму всех расходов, категории расходов и доходов, все
 * операции в кошельке, операции определённого типа (дохода или расхода), а также бюджеты по
 * категориям расходов.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Wallet {
  // Все операции кошелька
  private ArrayList<WalletOperation> walletOperations;
  // Все бюджеты по категориям расходов
  private HashMap<String, Double> budgetCategoriesAndLimits;

  /** Данный конструктор должен использоваться только библиотекой Jackson для десериализации. */
  @JsonCreator
  private Wallet() {}

  /**
   * Основной конструктор, используемый при создании нового пользователя или при загрузке данных из
   * файла имеющегося.
   */
  public Wallet(boolean usedForDeserialization) {
    walletOperations = new ArrayList<>();
    budgetCategoriesAndLimits = new HashMap<>();
  }

  /**
   * Используется для отладки. <br>
   * Метод для перевода значений полей, хранимых в объекте кошелька, в строку.
   */
  @Override
  public String toString() {
    return "Wallet{"
        + "walletOperations="
        + walletOperations
        + ", budgetCategoriesAndLimits="
        + budgetCategoriesAndLimits
        + '}';
  }

  // Хотя, по логике, классу WalletOperation лучше быть нестатическим,
  // нет способа добавить пустой конструктор без неявного параметра родителя для вложенного
  // нестатического класса.
  // Такой конструктор нужен для корректной работы десериализации JSON.
  /**
   * Объекты класса WalletOperation представляют отдельный добавленный доход или расход и хранят
   * свои: <br>
   * <br>
   * 1) уникальные автогенерируемые ID; <br>
   * 2) сумму операции (и для расходов, и для доходов она должна быть положительной!); <br>
   * 3) тип операции (доход или расход); <br>
   * 4) категорию операции; <br>
   * 5) дату и время, с которыми данная операции должна быть учтена.
   */
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class WalletOperation {
    private long id;
    private double amount;
    private boolean isIncome;
    private String category;
    private LocalDateTime dateTime;

    // Геттеры и сеттеры
    public long getId() {
      return id;
    }

    public double getAmount() {
      return amount;
    }

    public boolean isIncome() {
      return isIncome;
    }

    public String getCategory() {
      return category;
    }

    public LocalDateTime getDateTime() {
      return dateTime;
    }

    public void setCategory(String category) {
      this.category = category;
    }

    /** Данный конструктор должен использоваться только библиотекой Jackson для десериализации. */
    @JsonCreator
    private WalletOperation() {}

    /** Основной конструктор, используемый при добавлении нового дохода или расхода. */
    public WalletOperation(
        Wallet wallet, double amount, boolean isIncome, String category, LocalDateTime dateTime) {
      this.id = generateNewWalletOperationId(wallet);
      this.amount = amount;
      this.isIncome = isIncome;
      this.category = category;
      this.dateTime = dateTime;
    }

    /** Метод для генерации нового уникального ID операции. */
    private long generateNewWalletOperationId(Wallet wallet) {
      long id;
      boolean idAlreadyExists = false;
      while (true) {
        id = (long) (Math.random() * Long.MAX_VALUE);
        for (WalletOperation wo : wallet.getWalletOperations()) {
          if (wo.getId() == id) {
            idAlreadyExists = true;
            break;
          }
        }
        if (!idAlreadyExists) {
          return id;
        }
      }
    }
  }

  /** Получение баланса доходов и расходов кошелька. */
  public double getBalance() {
    return getTotalIncome() - getTotalExpenses();
  }

  /** Получение суммы всех доходов кошелька (всегда положительное значение). */
  public double getTotalIncome() {
    return this.getWalletOperations().stream()
        .filter(WalletOperation::isIncome)
        .map(WalletOperation::getAmount)
        .reduce(0.0, Double::sum);
  }

  /** Получение суммы всех расходов кошелька (всегда положительное значение). */
  public double getTotalExpenses() {
    return this.getWalletOperations().stream()
        .filter(wo -> !wo.isIncome())
        .map(WalletOperation::getAmount)
        .reduce(0.0, Double::sum);
  }

  /** Получение всех добавленных операций кошелька (доходов и расходов). */
  public ArrayList<WalletOperation> getWalletOperations() {
    return walletOperations;
  }

  /** Получение всех добавленных доходных операций кошелька. */
  public ArrayList<WalletOperation> getIncomeWalletOperations() {
    return walletOperations.stream()
        .filter(WalletOperation::isIncome)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** Получение всех добавленных расходных операций кошелька. */
  public ArrayList<WalletOperation> getExpensesWalletOperations() {
    return walletOperations.stream()
        .filter(wo -> !wo.isIncome())
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** Получение всех категорий добавленных расходных операций кошелька. */
  public HashSet<String> getWalletOperationsExpensesCategories() {
    return getWalletOperations().stream()
        .filter(wo -> !wo.isIncome()) // проверка на расход
        .map(WalletOperation::getCategory)
        .collect(Collectors.toCollection(HashSet::new));
  }

  /** Получение всех категорий добавленных доходных операций кошелька. */
  public HashSet<String> getWalletOperationsIncomeCategories() {
    return getWalletOperations().stream()
        .filter(WalletOperation::isIncome) // проверка на доход
        .map(WalletOperation::getCategory)
        .collect(Collectors.toCollection(HashSet::new));
  }

  /**
   * Получение всех категорий расходов, по которым установлены бюджеты, и лимитов по ним. <br>
   * Лимиты можно устанавливать и на те категории, по которым ещё не было учтённых расходов.
   */
  public HashMap<String, Double> getBudgetCategoriesAndLimits() {
    return budgetCategoriesAndLimits;
  }
}
