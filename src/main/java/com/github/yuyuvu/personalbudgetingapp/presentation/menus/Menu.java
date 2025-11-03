package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.print;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.skipLine;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.NotificationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Класс, от которого наследуются остальные классы Menu. Содержит 2 основных метода showMenu и
 * handleUserInput, которые вызываются в основном цикле приложения и которые должны переопределяться
 * наследниками. Также содержит методы для единообразного запроса и считывания ввода пользователя из
 * остальных меню, для выхода из аккаунта, выключения приложения, обработки служебных команд. Также
 * для сокращения дублирования кода содержит методы для единообразного запроса даты, формата отчёта,
 * опционального поведения, формирования имени файла отчёта, которые используются в
 * меню-наследниках.
 */
public abstract class Menu {

  private String currentInput;

  /** Метод, который должен отображать меню. Также отвечает за вывод уведомлений перед меню. */
  public void showMenu() {
    print(
        NotificationsService.checkAndPrepareNotifications(
            PersonalBudgetingApp.getCurrentAppUser().getWallet()));
    skipLine();
  }

  /**
   * Метод, который должен запрашивать и обрабатывать ввод от пользователя, направляя его на нужный
   * функционал.
   */
  public abstract void handleUserInput();

  /** Метод, выключающий приложение. */
  protected static void turnOffApplication(boolean printMessages) {
    if (printMessages) {
      printlnGreen("Выключаем приложение...");
    }
    System.exit(0);
  }

  /**
   * Метод, осуществляющий выход из текущего аккаунта пользователя и возврат в меню авторизации.
   * Также сохраняет данные кошелька пользователя в файл при помощи методов DataPersistenceService.
   */
  protected static void logOutOfCurrentUser(boolean printMessages) {
    if (PersonalBudgetingApp.getCurrentAppUser() != null) {
      if (printMessages) {
        printlnGreen(
            String.format(
                "Осуществляется выход из аккаунта пользователя %s...",
                PersonalBudgetingApp.getCurrentAppUser().getUsername()));
      }
      try {
        DataPersistenceService.saveUserdataToFile(PersonalBudgetingApp.getCurrentAppUser());
      } catch (IOException e) {
        printlnRed(e.getMessage());
        printlnRed(
            "К сожалению, данные пользователя не были сохранены. "
                + "Проверьте права доступа на директории.");
      }
      PersonalBudgetingApp.setCurrentAppUser(null);
      PersonalBudgetingApp.setCurrentMenu(new AuthorizationMenu());
    } else {
      if (printMessages) {
        printlnRed("Невозможно выйти из аккаунта без предварительной авторизации.");
      }
    }
  }

  /** Метод для запроса ввода от пользователя. */
  protected void requestUserInput() {
    currentInput = PersonalBudgetingApp.getUserInput().nextLine().strip();
  }

  /** Метод для получения ввода от пользователя. */
  protected String getCurrentUserInput() {
    return currentInput;
  }

  /**
   * Метод для обработки служебных команд на любом этапе работы приложения. Выводит справку,
   * позволяет отменить текущее действие и вернуться в меню и т.д.
   */
  public static void checkUserInputForAppGeneralCommands(String userInput)
      throws CancellationRequestedException {
    switch (userInput.toLowerCase()) {
      case "--cancel" -> throw new CancellationRequestedException();
      case "--help" -> {
        printlnCyan(
            """
                        Помощь по приложению:
                        --cancel - отмена текущего выбора и возврат в предыдущее меню;
                        --help - вывод помощи по приложению;
                        --logout - выход из текущего аккаунта пользователя;
                        --off - выключение приложения.""");
        throw new CancellationRequestedException();
      }
      case "--logout" -> {
        logOutOfCurrentUser(true);
        throw new CancellationRequestedException();
      }
      case "--off" -> {
        logOutOfCurrentUser(false);
        turnOffApplication(true);
      }
      default -> {
        return;
      }
    }
  }

  /**
   * Метод для запроса формата, в котором должен создаваться отчёт по данным кошелька пользователя.
   */
  protected String requestReportFormat() throws CancellationRequestedException {
    do {
      printCyan("Вывести результат в консоль (1) или сохранить в файл (2)? (введите 1 или 2): ");
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      switch (getCurrentUserInput().toLowerCase()) {
        case "1" -> {
          return "";
        }
        case "2" -> {
          return ".txt";
        }
        default -> printlnRed("Некорректный ввод. Введите \"1\" или \"2\".");
      }
    } while (true);
  }

  /**
   * Метод для создания названия файла отчёта или снимка состояния кошелька пользователя. Включает в
   * название файла имя пользователя и текущие дату и время.
   */
  protected String makeFilenameForReportFile(String reportVariant) {
    StringBuilder fileName = new StringBuilder();
    fileName.append(reportVariant);
    fileName.append("_").append(PersonalBudgetingApp.getCurrentAppUser().getUsername());
    LocalDateTime currentDateTime = LocalDateTime.now();
    fileName
        .append("_")
        .append(currentDateTime.getDayOfMonth())
        .append(currentDateTime.getMonthValue())
        .append(currentDateTime.getYear());
    fileName
        .append("_")
        .append(currentDateTime.getHour())
        .append("_")
        .append(currentDateTime.getMinute())
        .append("_")
        .append(currentDateTime.getSecond());
    return fileName.toString();
  }

  /**
   * Метод для запроса даты при добавлении операций дохода или расхода или при фильтрации данных
   * кошелька по периоду.
   */
  protected LocalDateTime requestDateFromUser(String optionalMessage)
      throws CancellationRequestedException {
    LocalDateTime dateTime;
    do {
      try {
        printYellow(optionalMessage);
        printCyan(
            "Укажите дату в формате \"день месяц год час:минуты\" (например, 05 05 2025 00:05): ");
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        if (getCurrentUserInput().matches("^\\d{2}\\s+\\d{2}\\s+\\d{4}\\s+\\d{2}:\\d{2}$")) {
          String[] splitDateTime = getCurrentUserInput().split("\\s");
          String[] splitTime = splitDateTime[3].split(":");
          dateTime =
              LocalDateTime.of(
                  Integer.parseInt(splitDateTime[2]),
                  Integer.parseInt(splitDateTime[1]),
                  Integer.parseInt(splitDateTime[0]),
                  Integer.parseInt(splitTime[0]),
                  Integer.parseInt(splitTime[1]));
          break;
        } else {
          throw new IllegalArgumentException();
        }
      } catch (IllegalArgumentException e) {
        printlnRed("Дата введена в некорректном формате. Повторите ввод.");
      } catch (DateTimeException e) {
        printlnRed("Введены невозможные значения для даты или времени. Повторите ввод.");
      }
    } while (true);
    return dateTime;
  }

  /**
   * Метод для запроса одного варианта из двух предлагаемых (импорт или экспорт, доходы или расходы)
   * в некоторых функциях.
   */
  protected boolean requestOptionFirstOrSecond(String requestMessage)
      throws CancellationRequestedException {
    while (true) {
      printCyan(requestMessage);
      requestUserInput();
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      switch (getCurrentUserInput().toLowerCase()) {
        case "1" -> {
          return true;
        }
        case "2" -> {
          return false;
        }
        default -> printlnRed("Некорректный ввод. Введите \"1\" или \"2\".");
      }
    }
  }
}
