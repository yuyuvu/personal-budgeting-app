package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.println;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnPurple;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnYellow;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.AuthorizationService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Меню для добавления отдельных доходов и расходов (с указанием их категории, суммы и даты) или
 * удаления ранее добавленных операций.
 */
public class IncomeAndExpensesManagementMenu extends Menu {

  /** Показ меню. */
  @Override
  public void showMenu() {
    super.showMenu();
    printlnYellow("Меню управления доходами и расходами:");
    println(
        """
                1. Добавление дохода.
                2. Добавление расхода.
                3. Перевод средств другому пользователю.
                4. Удаление ранее добавленного дохода или расхода.
                5. Возврат в главное меню.""");
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
        case "1" -> handleAddWalletOperation(wallet, true);
        case "2" -> handleAddWalletOperation(wallet, false);
        case "3" -> handleTransferToAnotherUser(wallet);
        case "4" -> handleWalletOperationRemoval(wallet);
        case "5" -> PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
        default -> printlnYellow("Некорректный ввод, введите цифру от 1 до 5.");
      }
    } catch (CancellationRequestedException e) {
      printlnPurple(e.getMessage());
    }
  }

  /**
   * Запрос требуемых параметров операции и валидация ввода от пользователя перед добавлением
   * операции дохода или расхода. А затем обращение к WalletOperationsService для добавления
   * операции.
   */
  private void handleAddWalletOperation(Wallet wallet, boolean isIncome)
      throws CancellationRequestedException {

    // Получение категории операции
    printCyan(String.format("Введите название категории %s: ", (isIncome ? "дохода" : "расхода")));
    requestUserInput();
    Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());

    String category;
    double amount;

    category = getCurrentUserInput().toLowerCase();
    if (category.isBlank()) {
      category = "без категории";
      printlnRed(
          "Введено пустое название категории. Операция будет помечена как \"Без категории\".");
    }

    // Получение суммы
    do {
      try {
        printCyan(String.format("Введите сумму %s: ", (isIncome ? "дохода" : "расхода")));
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        amount = Double.parseDouble(getCurrentUserInput());
        if (amount <= 0) {
          throw new IllegalArgumentException();
        }
        break;
      } catch (NumberFormatException e) {
        printlnRed("Введено не число. Повторите ввод.");
      } catch (IllegalArgumentException e) {
        printlnRed("Сумма должна быть больше нуля. Повторите ввод.");
      }
    } while (true);

    // Получение даты операции
    boolean doesUserWantToSpecifyDate;
    continueToRequestVarDoesUserWantToSpecifyDate:
    while (true) {
      printCyan(
          "Желаете указать дату и время операции? (да/нет; в случае выбора \"нет\" будут указаны текущие дата и время): ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      switch (getCurrentUserInput().toLowerCase()) {
        case "да" -> {
          doesUserWantToSpecifyDate = true;
          break continueToRequestVarDoesUserWantToSpecifyDate;
        }
        case "нет" -> {
          doesUserWantToSpecifyDate = false;
          break continueToRequestVarDoesUserWantToSpecifyDate;
        }
        default -> printlnRed("Некорректный ввод. Введите \"да\" или \"нет\".");
      }
    }

    LocalDateTime dateTime = LocalDateTime.now();

    // Автоматическое или ручное указание даты операции
    if (doesUserWantToSpecifyDate) {
      dateTime = requestDateFromUser("");
    }

    // Обращение к сервису, добавление операции в кошелёк
    if (isIncome) {
      WalletOperationsService.addIncome(wallet, amount, category, dateTime);
    } else {
      WalletOperationsService.addExpense(wallet, amount, category, dateTime);
    }
    printlnGreen(String.format("%s успешно добавлен!", (isIncome ? "Доход" : "Расход")));
  }

  /**
   * Запрос ID операции для её удаления и валидация ввода от пользователя перед удалением. А затем
   * обращение к WalletOperationsService для удаления операции.
   */
  private void handleWalletOperationRemoval(Wallet wallet) throws CancellationRequestedException {
    do {
      try {
        // Получение ID операции
        printCyan(
            "Введите id операции, которую желаете удалить (можно посмотреть в меню вывода информации об операциях, \n--cancel для возврата в меню): ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        long id = Long.parseLong(getCurrentUserInput());
        // Обращение к сервису, удаление операции из кошелька
        boolean success = WalletOperationsService.removeWalletOperationById(wallet, id);
        if (success) {
          printGreen("Операция успешно удалена!");
          break;
        } else {
          printlnRed("Операции с данным id не существует.");
        }
      } catch (NumberFormatException e) {
        printlnRed("Id операции должен содержать только числа.");
      }
    } while (true);
  }

  /**
   * Запрос имени получателя и суммы перевода, валидация ввода перед осуществлением перевода другому
   * пользователю. А затем обращение к WalletOperationsService для перевода средств.
   */
  private void handleTransferToAnotherUser(Wallet wallet) throws CancellationRequestedException {
    double amount;
    String anotherUser;

    // Получение имени получателя перевода
    while (true) {
      try {
        if (wallet.getBalance() <= 0.0) {
          printlnPurple(
              "Обратите внимание: вы собираетесь зафиксировать перевод, уже имея отрицательный или нулевой баланс.");
        }

        printCyan(
            "Введите имя пользователя (без учёта регистра), для которого предназначается перевод: ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());

        if (PersonalBudgetingApp.getCurrentAppUser()
            .getUsername()
            .equalsIgnoreCase(getCurrentUserInput())) {
          throw new CheckedIllegalArgumentException("Нельзя переводить средства самому себе.");
        }
        if (!AuthorizationService.checkUserExistenceIrrespectiveOfCase(getCurrentUserInput())) {
          throw new CheckedIllegalArgumentException(
              "Пользователя с указанным именем не существует. Повторите ввод.");
        }

        anotherUser = getCurrentUserInput().toLowerCase();
        break;
      } catch (CheckedIllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    }

    // Получение суммы перевода
    while (true) {
      try {
        printCyan(String.format("Введите сумму перевода для пользователя \"%s\": ", anotherUser));
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        amount = Double.parseDouble(getCurrentUserInput());
        if (amount <= 0) {
          throw new CheckedIllegalArgumentException(
              "Сумма перевода должна быть больше нуля. Повторите ввод.");
        }
        break;
      } catch (NumberFormatException e) {
        printlnRed("Введено не число. Повторите ввод.");
      } catch (CheckedIllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    }

    // Обращение к сервису, перевод средств
    try {
      WalletOperationsService.transferMoneyToAnotherUser(
          PersonalBudgetingApp.getCurrentAppUser(), anotherUser, amount);
    } catch (IOException e) {
      printlnRed(e.getMessage());
      return;
    }
    printlnGreen(
        String.format(
            "Перевод пользователю \"%s\" на сумму %.2f успешно осуществлён!", anotherUser, amount));
  }
}
