package com.github.yuyuvu.personalbudgetingapp.domainservices;

import static com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService.getExpensesByCategory;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

/**
 * Класс отвечает за логику приложения, связанную с управлением бюджетами по категориям расходов. В
 * нём содержатся методы для установления и изменения лимитов по категориям расходов. Также при
 * помощи его методов можно получить значение лимита, остаток лимита, проверить наличие лимита по
 * одной или нескольким категориям расходов.
 */
public class BudgetingService {

  /** Метод проверяет наличие уже установленного бюджета по определённой категории расходов. */
  public static boolean checkExpensesCategoryLimitExistence(Wallet wallet, String category) {
    return wallet.getBudgetCategoriesAndLimits().containsKey(category);
  }

  /**
   * Метод добавляет новый бюджет для определённой категории расходов. Лимит по бюджету должен быть
   * равен нулю или больше нуля.
   */
  public static void addNewExpensesCategoryLimit(Wallet wallet, String newCategory, double newLimit)
      throws IllegalArgumentException {
    if (newLimit >= 0) {
      wallet.getBudgetCategoriesAndLimits().put(newCategory, newLimit);
    } else {
      throw new IllegalArgumentException(
          "Лимит должен быть больше нуля или равен ему. Невозможно добавить лимит.");
    }
  }

  /**
   * Метод удаляет имеющийся бюджет для определённой категории расходов. Проверяет, что запрошенный
   * бюджет действительно существует.
   */
  public static void removeExpensesCategoryLimit(Wallet wallet, String category)
      throws IllegalArgumentException {
    if (checkExpensesCategoryLimitExistence(wallet, category)) {
      wallet.getBudgetCategoriesAndLimits().remove(category);
    } else {
      throw new IllegalArgumentException(
          "Установленного бюджета для данной категории расходов не существует. "
              + " Невозможно удалить.");
    }
  }

  /**
   * Метод изменяет лимит по имеющемуся бюджету для определённой категории расходов. Лимит по
   * бюджету должен быть равен нулю или больше нуля. Проверяет, что запрошенный бюджет действительно
   * существует.
   */
  public static void changeLimitForCategory(Wallet wallet, String category, double newLimit)
      throws IllegalArgumentException {
    if (checkExpensesCategoryLimitExistence(wallet, category)) {
      if (newLimit >= 0) {
        wallet.getBudgetCategoriesAndLimits().put(category, newLimit);
      } else {
        throw new IllegalArgumentException(
            "Новый лимит должен быть больше нуля или равен ему. Невозможно изменить лимит.");
      }
    } else {
      throw new IllegalArgumentException(
          "Установленного бюджета для данной категории расходов не существует. "
              + " Невозможно изменить лимит.");
    }
  }

  /** Метод возвращает значение лимита по определённой категории расходов. */
  public static double getLimitByCategory(Wallet wallet, String category)
      throws IllegalArgumentException {
    if (checkExpensesCategoryLimitExistence(wallet, category)) {
      return wallet.getBudgetCategoriesAndLimits().get(category);
    } else {
      throw new IllegalArgumentException(
          "Установленного бюджета для данной категории расходов не существует. "
              + "Невозможно совершить запрошенную операцию.");
    }
  }

  /** Метод возвращает значение суммарного лимита по нескольким категориям расходов. */
  public static double getLimitByCategories(
      Wallet wallet, boolean sensibleToErrors, String... categories)
      throws IllegalArgumentException {
    double result = 0.0;
    for (String category : categories) {
      try {
        result += getLimitByCategory(wallet, category);
      } catch (IllegalArgumentException e) {
        if (sensibleToErrors) {
          throw e;
        }
      }
    }
    return result;
  }

  /**
   * Метод возвращает значение остатка возможных трат до израсходования лимита по определённой
   * категории расходов.
   */
  public static double getRemainderByCategory(Wallet wallet, String category)
      throws IllegalArgumentException {
    double limit = getLimitByCategory(wallet, category);
    double alreadySpent = getExpensesByCategory(wallet, category);
    return limit - alreadySpent;
  }

  /**
   * Метод возвращает значение суммарного остатка возможных трат до израсходования лимитов по
   * нескольким категориям расходов.
   */
  public static double getRemainderByCategories(
      Wallet wallet, boolean sensibleToErrors, String... categories) {
    double result = 0.0;
    for (String category : categories) {
      try {
        result += getRemainderByCategory(wallet, category);
      } catch (IllegalArgumentException e) {
        if (sensibleToErrors) {
          throw e;
        }
      }
    }
    return result;
  }
}
