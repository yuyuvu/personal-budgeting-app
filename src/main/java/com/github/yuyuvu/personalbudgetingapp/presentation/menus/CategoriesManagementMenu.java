package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.println;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnPurple;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnYellow;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.domainservices.BudgetingService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Меню для управления бюджетами, а также категориями доходов и расходов. Позволяет по-разному
 * редактировать бюджеты и категории, удалять и объединять их.
 */
public class CategoriesManagementMenu extends Menu {

  /** Показ меню. */
  @Override
  public void showMenu() {
    super.showMenu();
    printlnYellow("Меню управления бюджетами и категориями доходов и расходов:");
    printlnYellow("Операции с категориями расходов:");
    println(
        """
                1. Добавить новый бюджет (лимит) для категории расходов.
                2. Изменить значение существующего лимита для категории расходов.
                3. Удалить бюджет для категории расходов.
                4. Изменить название существующей категории расходов.
                5. Объединить категории расходов и их лимиты.""");
    printlnYellow("Операции с категориями доходов:");
    println(
        """
                6. Изменить название существующей категории доходов.
                7. Объединить категории доходов.
                8. Возврат в главное меню.""");
    printYellow("Введите номер желаемого действия: ");
  }

  /** Направление на нужную функцию. */
  @Override
  public void handleUserInput() {
    requestUserInput(); // складывается в переменную super.currentInput
    try {
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
      switch (getCurrentUserInput()) {
        case "1" -> handleAddNewExpensesCategoryWithLimit(wallet);
        case "2" -> handleChangeExistingExpensesCategoryLimit(wallet);
        case "3" -> handleRemoveExistingExpensesCategoryLimit(wallet);
        case "4" -> handleChangeExistingExpensesCategoryName(wallet);
        case "5" -> handleMergeExistingExpensesCategoriesAndTheirLimits(wallet);
        case "6" -> handleChangeExistingIncomeCategoryName(wallet);
        case "7" -> handleMergeExistingIncomeCategories(wallet);
        case "8" -> PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
        default -> printlnYellow("Некорректный ввод, введите цифру от 1 до 8.");
      }
    } catch (CancellationRequestedException e) {
      printlnPurple(e.getMessage());
    }
  }

  /**
   * Запрос требуемых параметров для добавления нового бюджета и валидация ввода от пользователя
   * перед его добавлением. А затем обращение к BudgetingService для фактического добавления данного
   * бюджета. Позволяет добавлять бюджеты даже по тем категориям, по которым ещё не было расходов.
   */
  private void handleAddNewExpensesCategoryWithLimit(Wallet wallet)
      throws CancellationRequestedException {
    String category;
    double amount;
    do {
      printCyan("Введите название категории расходов для добавления лимита: ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      category = getCurrentUserInput().toLowerCase();
      if (BudgetingService.checkExpensesCategoryLimitExistence(wallet, category)) {
        printlnRed(
            "Бюджет для данной категории уже существует. Используйте опцию изменения лимита или введите другое название категории.");
        continue;
      }
      break;
    } while (true);
    do {
      try {
        printCyan("Введите бюджет для данной категории расходов: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        amount = Double.parseDouble(getCurrentUserInput());
        // Обращение к BudgetingService
        BudgetingService.addNewExpensesCategoryLimit(wallet, category, amount);
        break;
      } catch (NumberFormatException e) {
        printlnRed("Введено не число. Повторите ввод.");
      } catch (IllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    printlnGreen(
        String.format("Бюджет в %.2f для категории \"%s\" успешно добавлен!", amount, category));
  }

  /**
   * Запрос требуемых параметров для изменения лимита по имеющемуся бюджету и валидация ввода от
   * пользователя. А затем обращение к BudgetingService для фактического изменения лимита по
   * имеющемуся бюджету.
   */
  private void handleChangeExistingExpensesCategoryLimit(Wallet wallet)
      throws CancellationRequestedException {
    String category;
    double amount;
    do {
      printCyan("Введите название категории расходов для изменения лимита: ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      category = getCurrentUserInput().toLowerCase();
      if (!BudgetingService.checkExpensesCategoryLimitExistence(wallet, category)) {
        printlnRed(
            "Бюджета для данной категории не существует. Введите другое название категории.");
        continue;
      }
      break;
    } while (true);
    do {
      try {
        printCyan("Введите новый бюджет для данной категории расходов: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        amount = Double.parseDouble(getCurrentUserInput());
        // Обращение к BudgetingService
        BudgetingService.changeLimitForCategory(wallet, category, amount);
        break;
      } catch (NumberFormatException e) {
        printlnRed("Введено не число. Повторите ввод.");
      } catch (IllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    printlnGreen(
        String.format(
            "Новый бюджет в %.2f для категории \"%s\" успешно установлен!", amount, category));
  }

  /**
   * Запрос требуемых параметров для удаления бюджета по определённой категории расходов и валидация
   * ввода от пользователя. А затем обращение к BudgetingService для фактического удаления бюджета
   * по определённой категории расходов.
   */
  private void handleRemoveExistingExpensesCategoryLimit(Wallet wallet)
      throws CancellationRequestedException {
    String category;
    double amount;
    do {
      try {
        printCyan("Введите название категории расходов для удаления бюджета: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        category = getCurrentUserInput().toLowerCase();
        amount = BudgetingService.getLimitByCategory(wallet, category);
        // Обращение к BudgetingService
        BudgetingService.removeExpensesCategoryLimit(wallet, category);
        break;
      } catch (IllegalArgumentException e) {
        printlnRed(e.getMessage() + " Повторите ввод.");
      }
    } while (true);
    printlnGreen(
        String.format("Бюджет в %.2f для категории \"%s\" успешно удалён!", amount, category));
  }

  /**
   * Запрос требуемых параметров для изменения названия имеющейся категории расходов и валидация
   * ввода от пользователя. А затем обращение к WalletOperationsService для фактического изменения
   * названия имеющейся категории расходов.
   */
  private void handleChangeExistingExpensesCategoryName(Wallet wallet)
      throws CancellationRequestedException {
    String category;
    String newCategoryName;
    printlnYellow(
        "Обратите внимание, данная опция также изменяет название категории для соответствующего бюджета!");
    do {
      printCyan(
          "Введите название категории расходов, для которой требуется установить новое название: ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      category = getCurrentUserInput().toLowerCase();
      if (!wallet.getWalletOperationsExpensesCategories().contains(category)) {
        printlnRed(
            "По данной категории ещё не было учтено ни одного расхода. Введите другое название категории.");
        continue;
      }
      break;
    } while (true);
    do {
      try {
        printCyan("Введите новое название для данной категории расходов: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        newCategoryName = getCurrentUserInput().toLowerCase();
        if (wallet.getWalletOperationsExpensesCategories().contains(newCategoryName)) {
          printlnRed(
              "Категория расходов с выбранным названием уже существует. Введите другое название для категории.");
          continue;
        }
        if (BudgetingService.checkExpensesCategoryLimitExistence(wallet, newCategoryName)) {
          printlnRed(
              "Бюджет для категории с выбранным названием уже существует. \nВведите другое название для категории или сначала отредактируйте лимиты.");
          continue;
        }
        // Обращение к WalletOperationsService
        WalletOperationsService.changeNameForCategory(wallet, category, newCategoryName, false);
        break;
      } catch (IllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    printlnGreen(
        String.format(
            "Название для категории расходов и бюджета \"%s\" успешно изменено на \"%s\"!",
            category, newCategoryName));
  }

  /**
   * Запрос требуемых параметров для объединения нескольких имеющихся категорий расходов и валидация
   * ввода от пользователя. А затем обращение к WalletOperationsService для фактического объединения
   * нескольких имеющихся категорий расходов.
   */
  private void handleMergeExistingExpensesCategoriesAndTheirLimits(Wallet wallet)
      throws CancellationRequestedException {
    String[] oldCategories;
    String newCategoryName;
    printlnYellow(
        "Обратите внимание, данная опция также объединяет категории для соответствующих бюджетов в одну суммарную!");
    do {
      try {
        printCyan(
            "Введите названия категорий расходов, которые требуется объединить. Указывайте их в кавычках и через пробел: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        if (getCurrentUserInput()
            .toLowerCase()
            .matches(
                "^(\\s*\"(([^\\s\"']+)|((\\s*)([^\\s\"']+)(\\s*))+)\"\\s+)+(\\s*\"(([^\\s\"']+)|((\\s*)([^\\s\"']+)(\\s*))+)\"\\s*)$")) {
          oldCategories = getCurrentUserInput().toLowerCase().split("\"\\s+\"");
          oldCategories =
              Arrays.stream(oldCategories)
                  .map(s -> s.replaceAll("\"", ""))
                  .map(String::strip)
                  .distinct()
                  .toArray(String[]::new);
          printlnCyan(
              "Введены категории: "
                  + Arrays.toString(oldCategories).replaceAll("[\\[\\]]", "")
                  + ".");
          if (oldCategories.length == 1) {
            throw new CheckedIllegalArgumentException(
                "Введена только одна повторяющаяся категория расходов. Укажите как минимум две разных.");
          }
          ArrayList<String> incorrectCategories = new ArrayList<>();
          for (String category : oldCategories) {
            if (!wallet.getWalletOperationsExpensesCategories().contains(category)) {
              incorrectCategories.add(category);
            }
          }
          if (!incorrectCategories.isEmpty()) {
            throw new CheckedIllegalArgumentException(
                String.format(
                    "Указаны категории, по которым ещё не было учтено ни одного расхода: %s. Введите названия категорий, по которым учтены расходы.",
                    incorrectCategories.toString().replaceAll("[\\[\\]]", "")));
          }
        } else {
          throw new CheckedIllegalArgumentException(
              "Неверный формат ввода категорий. Повторите ввод в формате: \"категория\" \"категория2\" \"категория3\" и т.д.");
        }
        break;
      } catch (CheckedIllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    do {
      try {
        printCyan("Введите новое название для объединённой категории расходов: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        newCategoryName = getCurrentUserInput().toLowerCase();
        if (wallet.getWalletOperationsExpensesCategories().contains(newCategoryName)) {
          if (!Arrays.asList(oldCategories).contains(newCategoryName)) {
            throw new CheckedIllegalArgumentException(
                "Категория расходов с выбранным названием уже существует. Введите другое название для категории.");
          }
        }
        if (BudgetingService.checkExpensesCategoryLimitExistence(wallet, newCategoryName)) {
          if (!Arrays.asList(oldCategories).contains(newCategoryName)) {
            throw new CheckedIllegalArgumentException(
                "Бюджет для категории с выбранным названием уже существует. \nВведите другое название для категории или сначала отредактируйте лимиты.");
          }
        }
        // Обращение к WalletOperationsService
        WalletOperationsService.mergeExpensesCategories(wallet, newCategoryName, oldCategories);
        break;
      } catch (CheckedIllegalArgumentException | IllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    printlnGreen(
        String.format(
            "Категории расходов и бюджета \"%s\" успешно объединены в категорию \"%s\"!",
            Arrays.toString(oldCategories).replaceAll("[\\[\\]]", ""), newCategoryName));
  }

  /**
   * Запрос требуемых параметров для изменения названия имеющейся категории доходов и валидация
   * ввода от пользователя. А затем обращение к WalletOperationsService для фактического изменения
   * названия имеющейся категории доходов.
   */
  private void handleChangeExistingIncomeCategoryName(Wallet wallet)
      throws CancellationRequestedException {
    String category;
    String newCategoryName;
    do {
      printCyan(
          "Введите название категории доходов, для которой требуется установить новое название: ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      category = getCurrentUserInput().toLowerCase();
      if (!wallet.getWalletOperationsIncomeCategories().contains(category)) {
        printlnRed(
            "По данной категории ещё не было учтено ни одного дохода. Введите другое название категории.");
        continue;
      }
      break;
    } while (true);
    do {
      try {
        printCyan("Введите новое название для данной категории доходов: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        newCategoryName = getCurrentUserInput().toLowerCase();
        if (wallet.getWalletOperationsIncomeCategories().contains(newCategoryName)) {
          printlnRed(
              "Категория расходов с выбранным названием уже существует. Введите другое название для категории.");
          continue;
        }
        // Обращение к WalletOperationsService
        WalletOperationsService.changeNameForCategory(wallet, category, newCategoryName, true);
        break;
      } catch (IllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    printlnGreen(
        String.format(
            "Название для категории доходов \"%s\" успешно изменено на \"%s\"!",
            category, newCategoryName));
  }

  /**
   * Запрос требуемых параметров для объединения нескольких имеющихся категорий доходов и валидация
   * ввода от пользователя. А затем обращение к WalletOperationsService для фактического объединения
   * нескольких имеющихся категорий доходов.
   */
  private void handleMergeExistingIncomeCategories(Wallet wallet)
      throws CancellationRequestedException {
    String[] oldCategories;
    String newCategoryName;
    do {
      try {
        printCyan(
            "Введите названия категорий доходов, которые требуется объединить. Указывайте их в кавычках и через пробел: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        if (getCurrentUserInput()
            .toLowerCase()
            .matches(
                "^(\\s*\"(([^\\s\"']+)|((\\s*)([^\\s\"']+)(\\s*))+)\"\\s+)+(\\s*\"(([^\\s\"']+)|((\\s*)([^\\s\"']+)(\\s*))+)\"\\s*)$")) {
          oldCategories = getCurrentUserInput().toLowerCase().split("\"\\s+\"");
          oldCategories =
              Arrays.stream(oldCategories)
                  .map(s -> s.replaceAll("\"", ""))
                  .map(String::strip)
                  .distinct()
                  .toArray(String[]::new);
          printlnCyan(
              "Введены категории: "
                  + Arrays.toString(oldCategories).replaceAll("[\\[\\]]", "")
                  + ".");
          if (oldCategories.length == 1) {
            throw new CheckedIllegalArgumentException(
                "Введена только одна повторяющаяся категория доходов. Укажите как минимум две разных.");
          }
          ArrayList<String> incorrectCategories = new ArrayList<>();
          for (String category : oldCategories) {
            if (!wallet.getWalletOperationsIncomeCategories().contains(category)) {
              incorrectCategories.add(category);
            }
          }
          if (!incorrectCategories.isEmpty()) {
            throw new CheckedIllegalArgumentException(
                String.format(
                    "Указаны категории, по которым ещё не было учтено ни одного дохода: %s. Введите названия категорий, по которым учтены доходы.",
                    incorrectCategories.toString().replaceAll("[\\[\\]]", "")));
          }
        } else {
          throw new CheckedIllegalArgumentException(
              "Неверный формат ввода категорий. Повторите ввод в формате: \"категория\" \"категория2\" \"категория3\" и т.д.");
        }
        break;
      } catch (CheckedIllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    do {
      try {
        printCyan("Введите новое название для объединённой категории доходов: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        newCategoryName = getCurrentUserInput().toLowerCase();
        if (wallet.getWalletOperationsIncomeCategories().contains(newCategoryName)) {
          if (!Arrays.asList(oldCategories).contains(newCategoryName)) {
            throw new CheckedIllegalArgumentException(
                "Категория доходов с выбранным названием уже существует. Введите другое название для категории.");
          }
        }
        // Обращение к WalletOperationsService
        WalletOperationsService.mergeIncomeCategories(wallet, newCategoryName, oldCategories);
        break;
      } catch (CheckedIllegalArgumentException | IllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    } while (true);
    printlnGreen(
        String.format(
            "Категории доходов \"%s\" успешно объединены в категорию \"%s\"!",
            Arrays.toString(oldCategories).replaceAll("[\\[\\]]", ""), newCategoryName));
  }
}
