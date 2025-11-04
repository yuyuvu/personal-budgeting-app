package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printPurple;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.println;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnPurple;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.skipLine;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.appservices.SnapshotsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.SnapshotException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/** Меню для экспорта и импорта снимков состояния всего кошелька или отдельных его частей. */
public class CheckpointsSaveAndLoadMenu extends Menu {

  /** Показ меню. */
  @Override
  public void showMenu() {
    // super.showMenu();
    skipLine();
    printlnYellow("Меню работы со снимками состояния:");
    println(
        """
                1. Экспорт / импорт всех данных кошелька.
                2. Экспорт / импорт всех доходов.
                3. Экспорт / импорт всех расходов.
                4. Экспорт / импорт всех бюджетов на категории расходов.
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
      User user = PersonalBudgetingApp.getCurrentAppUser();
      switch (getCurrentUserInput()) {
        case "1" -> handleTotalExportImport(wallet, user); // экспорт / импорт всего кошелька
        case "2" -> handleIncomeExportImport(wallet, user); // экспорт / импорт доходов
        case "3" -> handleExpensesExportImport(wallet, user); // экспорт / импорт расходов
        case "4" -> handleBudgetsExportImport(wallet, user); // экспорт / импорт бюджетов
        case "5" -> PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
        default -> printlnYellow("Некорректный ввод, введите цифру от 1 до 5.");
      }
    } catch (CancellationRequestedException e) {
      printlnPurple(e.getMessage());
    }
  }

  /** Запрос: экспорт или импорт нужен. */
  private boolean requestExportOrImport() throws CancellationRequestedException {
    return requestOptionFirstOrSecond(
        "Желаете ли вы экспортировать (1) или импортировать (2) "
            + "снимок состояния? (введите 1 или 2): ");
  }

  /**
   * Запрашиваем подтверждение импорта от пользователя, так как импорт перезаписывает
   * соответствующие данные кошелька.
   */
  private boolean requestImportCancellation(String categoryMessage)
      throws CancellationRequestedException {
    while (true) {
      printlnPurple(categoryMessage);
      printlnPurple(
          "Внимание: импорт данных из снимка состояния сотрёт соответствующие данные вашего кошелька.");
      printPurple("Точно продолжить? (введите да или нет): ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      switch (getCurrentUserInput().toLowerCase()) {
        case "да" -> {
          return false;
        }
        case "нет" -> {
          printlnGreen("Отмена импорта...");
          return true;
        }
        default -> printlnRed("Некорректный ввод. Введите \"да\" или \"нет\".");
      }
    }
  }

  /** Запрашиваем путь до файла со снимком состояния для импорта. */
  private String requestPathToSnapshotForImport() throws CancellationRequestedException {
    String pathToFile;
    while (true) {
      printCyan("Укажите абсолютный путь до файла снимка состояния: ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      try {
        if (getCurrentUserInput().isBlank()) {
          throw new CheckedIllegalArgumentException(
              "Вы не указали путь до снимка состояния. Повторите ввод.");
        }
        pathToFile = Path.of(getCurrentUserInput().replace("\"", "")).toAbsolutePath().toString();
        if (!Files.exists(Path.of(pathToFile))) {
          throw new CheckedIllegalArgumentException(
              "Файла по указанному пути не существует. Повторите ввод.");
        }
      } catch (CheckedIllegalArgumentException e) {
        printlnRed(e.getMessage());
        continue;
      } catch (InvalidPathException e) {
        printlnRed("Неверный формат пути до файла: " + e.getMessage());
        continue;
      }
      break;
    }
    return pathToFile;
  }

  /** Направляем на экспорт или импорт всего кошелька в зависимости от ввода пользователя. */
  private void handleTotalExportImport(Wallet wallet, User user)
      throws CancellationRequestedException {
    if (requestExportOrImport()) {
      handleTotalExport(wallet);
    } else {
      handleTotalImport(user);
    }
  }

  /**
   * Метод обращается к SnapshotsService для формирования снимка всего кошелька. Затем при помощи
   * DataPersistenceService сохраняет его в файл.
   */
  private void handleTotalExport(Wallet wallet) {
    printlnYellow("Экспорт снимка всего кошелька.");

    // Сериализация объекта из текущего кошелька
    String exportContents;
    try {
      exportContents = SnapshotsService.makeTotalExportContents(wallet);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Запись в файл
    String whereSaved;
    try {
      whereSaved =
          DataPersistenceService.saveSnapshotToFile(
              exportContents, makeFilenameForReportFile("wallet_total_data"));
    } catch (IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);
  }

  /**
   * Метод при помощи DataPersistenceService читает файл снимка всего кошелька. Затем обращается к
   * SnapshotsService для десериализации снимка всего кошелька в объект Wallet и перезаписывания
   * текущих данных.
   */
  private void handleTotalImport(User user) throws CancellationRequestedException {
    if (requestImportCancellation("Импорт снимка всего кошелька.")) {
      return;
    }
    String pathToFile = requestPathToSnapshotForImport();
    String loadedContents;

    // Чтение файла снимка
    try {
      loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
    } catch (CheckedIllegalArgumentException | IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Десериализация и перезапись
    try {
      SnapshotsService.importTotalSnapshot(user, loadedContents);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
  }

  /** Направляем на экспорт или импорт доходов в зависимости от ввода пользователя. */
  private void handleIncomeExportImport(Wallet wallet, User user)
      throws CancellationRequestedException {
    if (requestExportOrImport()) {
      handleIncomeExport(wallet);
    } else {
      handleIncomeImport(user);
    }
  }

  /**
   * Метод обращается к SnapshotsService для формирования снимка всех доходов. Затем при помощи
   * DataPersistenceService сохраняет его в файл.
   */
  private void handleIncomeExport(Wallet wallet) {
    printlnYellow("Экспорт снимка доходов.");

    // Сериализация объекта из текущего кошелька
    String exportContents;
    try {
      exportContents = SnapshotsService.makeOnlyIncomeExportContents(wallet);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Запись в файл
    String whereSaved;
    try {
      whereSaved =
          DataPersistenceService.saveSnapshotToFile(
              exportContents, makeFilenameForReportFile("wallet_only_income_data"));
    } catch (IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);
  }

  /**
   * Метод при помощи DataPersistenceService читает файл снимка всех доходов. Затем обращается к
   * SnapshotsService для десериализации снимка всех доходов в объект ArrayList и перезаписывания
   * текущих данных.
   */
  private void handleIncomeImport(User user) throws CancellationRequestedException {
    if (requestImportCancellation("Импорт снимка доходов.")) {
      return;
    }
    String pathToFile = requestPathToSnapshotForImport();
    String loadedContents;

    // Чтение файла снимка
    try {
      loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
    } catch (CheckedIllegalArgumentException | IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Десериализация и перезапись
    try {
      SnapshotsService.importOnlyIncomeSnapshot(user, loadedContents);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
  }

  /** Направляем на экспорт или импорт расходов в зависимости от ввода пользователя. */
  private void handleExpensesExportImport(Wallet wallet, User user)
      throws CancellationRequestedException {
    if (requestExportOrImport()) {
      handleExpensesExport(wallet);
    } else {
      handleExpensesImport(user);
    }
  }

  /**
   * Метод обращается к SnapshotsService для формирования снимка всех расходов. Затем при помощи
   * DataPersistenceService сохраняет его в файл.
   */
  private void handleExpensesExport(Wallet wallet) {
    printlnYellow("Экспорт снимка расходов.");

    // Сериализация объекта из текущего кошелька
    String exportContents;
    try {
      exportContents = SnapshotsService.makeOnlyExpensesExportContents(wallet);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Запись в файл
    String whereSaved;
    try {
      whereSaved =
          DataPersistenceService.saveSnapshotToFile(
              exportContents, makeFilenameForReportFile("wallet_only_expenses_data"));
    } catch (IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);
  }

  /**
   * Метод при помощи DataPersistenceService читает файл снимка всех расходов. Затем обращается к
   * SnapshotsService для десериализации снимка всех расходов в объект ArrayList и перезаписывания
   * текущих данных.
   */
  private void handleExpensesImport(User user) throws CancellationRequestedException {
    if (requestImportCancellation("Импорт снимка расходов.")) {
      return;
    }
    String pathToFile = requestPathToSnapshotForImport();
    String loadedContents;

    // Чтение файла снимка
    try {
      loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
    } catch (CheckedIllegalArgumentException | IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Десериализация и перезапись
    try {
      SnapshotsService.importOnlyExpensesSnapshot(user, loadedContents);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
  }

  /** Направляем на экспорт или импорт бюджетов в зависимости от ввода пользователя. */
  private void handleBudgetsExportImport(Wallet wallet, User user)
      throws CancellationRequestedException {
    if (requestExportOrImport()) {
      handleBudgetsExport(wallet);
    } else {
      handleBudgetsImport(user);
    }
  }

  /**
   * Метод обращается к SnapshotsService для формирования снимка всех бюджетов. Затем при помощи
   * DataPersistenceService сохраняет его в файл.
   */
  private void handleBudgetsExport(Wallet wallet) {
    printlnYellow("Экспорт снимка бюджетов.");

    // Сериализация объекта из текущего кошелька
    String exportContents;
    try {
      exportContents = SnapshotsService.makeOnlyBudgetsExportContents(wallet);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Запись в файл
    String whereSaved;
    try {
      whereSaved =
          DataPersistenceService.saveSnapshotToFile(
              exportContents, makeFilenameForReportFile("wallet_only_budgets_data"));
    } catch (IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);
  }

  /**
   * Метод при помощи DataPersistenceService читает файл снимка всех бюджетов. Затем обращается к
   * SnapshotsService для десериализации снимка всех бюджетов в объект HashMap и перезаписывания
   * текущих данных.
   */
  private void handleBudgetsImport(User user) throws CancellationRequestedException {
    if (requestImportCancellation("Импорт снимка бюджетов.")) {
      return;
    }
    String pathToFile = requestPathToSnapshotForImport();
    String loadedContents;

    // Чтение файла снимка
    try {
      loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
    } catch (CheckedIllegalArgumentException | IOException e) {
      printlnRed(e.getMessage());
      return;
    }

    // Десериализация и перезапись
    try {
      SnapshotsService.importOnlyBudgetsSnapshot(user, loadedContents);
    } catch (SnapshotException e) {
      printlnRed(e.getMessage());
      return;
    }

    printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
  }
}
