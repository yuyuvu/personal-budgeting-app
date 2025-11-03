package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.print;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.skipLine;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.ConfigManager;
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

  /**
   * Метод, который должен отображать меню. Также отвечает за вывод уведомлений перед меню, если
   * пользователь их не отключил.
   */
  public void showMenu() {
    if (ConfigManager.checkNotificationsConfigForCurrentUser()
        .equals(ConfigManager.BooleanPropertiesValues.TRUE.name())) {
      print(
          NotificationsService.checkAndPrepareNotifications(
              PersonalBudgetingApp.getCurrentAppUser().getWallet()));
    }
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
   * позволяет отменить текущее действие, вернуться в меню, скрыть / отобразить уведомления и т.д.
   */
  public static void checkUserInputForAppGeneralCommands(String userInput)
      throws CancellationRequestedException {
    switch (userInput.toLowerCase()) {
      case "--cancel" -> throw new CancellationRequestedException();
      case "--help" -> {
        printlnCyan(
            """
                        ----------------------------------------------------------------------------
                        Помощь по приложению:
                        ----------------------------------------------------------------------------
                        Приложение работает по принципу ввода цифровых команд в консоль.
                        Для получения доступа к требуемой пользователю функции нужно последовательно
                        пройти через ряд меню, вводя цифру интересующего пункта.
                        На каждом шаге работы приложения для пользователя выводятся подсказки,
                        что сейчас можно или нужно сделать.
                        Приложение проверяет ввод пользователя, и, в случае предоставления
                        некорректных значений для меню или параметров для функций, выводит
                        информативное сообщение об ошибке и повторяет запрос ввода.
                        Помимо ввода цифр или запрашиваемых параметров для отдельных функций,
                        на любом этапе работы приложения можно вводить служебные команды.
                        
                        1. Например, если имеющийся пользователь хочет сохранить в файл список всех
                        добавленных операций дохода, нужно последовательно ввести:
                        2 (вход в аккаунт) -> логин и пароль (по отображаемым подсказкам) ->
                        1 (меню базовой аналитики) -> 5 (меню расширенной аналитики) -> 3 (вывод
                        всех операций дохода) -> 2 (сохранить в файл)
                        
                        2. Если имеющийся пользователь хочет импортировать данные снимка состояния
                        заданных бюджетов по расходам:
                        2 (вход в аккаунт) -> логин и пароль (по отображаемым подсказкам) ->
                        4 (меню экспорта и импорта) -> 4 (экспорт / импорт бюджетов) ->
                        2 (импорт) -> да (подтвердить импорт) -> путь до снимка состояния.
                        
                        3. Пользователь находится в меню экспорта и хочет отключить приложение,
                        не возвращаясь в меню авторизации: для этого нужно ввести --off.
                        
                        Служебные команды приложения:
                        --cancel - отмена текущего выбора и возврат в предыдущее меню;
                        --help - вывод помощи по приложению;
                        --hide_notifications - скрыть уведомления во всём приложении;
                        --show_notifications - вернуть уведомления во всём приложении;
                        --logout - выход из текущего аккаунта пользователя;
                        --off - выключение приложения.
                        
                        Приложение предназначено для отслеживания личных финансов.
                        Оно позволяет:
                        1) Добавлять доходы и расходы по категориям.
                        2) Устанавливать бюджеты для отдельных категорий расходов.
                        3) Управлять категориями и бюджетами.
                        4) Выводить аналитику по ранее добавленным операциям.
                        5) Отслеживать перерасход и просматривать остаток возможных расходов
                        по определённым категориям.
                        6) Выводить информацию с учётом фильтрации по категориям и периодам.
                        7) Выводить отсортированные списки всех добавленных операций.
                        8) Экспортировать и импортировать все или отдельные данные
                        кошелька пользователя.
                        9) Учитывать переводы другим пользователям.
                        10) Сохранять отчёты по кошельку в файл.
                        11) Просматривать уведомления, которые выводятся в случае перерасхода
                        бюджетов, наличия отрицательного баланса и при выполнении других
                        условий.
                        12) Отключать и возвращать отображение уведомлений.
                        13) Авторизоваться в аккаунте после полного перезапуска приложения
                        с сохранением всех данных пользователя.
                        14) Регистрировать любое количество пользователей с разными данными.
                        ----------------------------------------------------------------------------""");
        throw new CancellationRequestedException();
      }
      case "--hide_notifications" -> {
        if (ConfigManager.checkNotificationsConfigForCurrentUser()
            .equals(ConfigManager.BooleanPropertiesValues.TRUE.name())) {
          ConfigManager.reverseNotificationsConfigForCurrentUser();
          printlnCyan("Теперь уведомления скрыты во всём приложении.");
        } else {
          printlnCyan("Уведомления уже скрыты во всём приложении.");
        }
        throw new CancellationRequestedException();
      }
      case "--show_notifications" -> {
        if (ConfigManager.checkNotificationsConfigForCurrentUser()
            .equals(ConfigManager.BooleanPropertiesValues.FALSE.name())) {
          ConfigManager.reverseNotificationsConfigForCurrentUser();
          printlnCyan("Теперь уведомления снова будут отображаться во всём приложении.");
        } else {
          printlnCyan("Уведомления уже отображаются во всём приложении.");
        }
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
