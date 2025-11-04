package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.deleteColorsFromString;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.print;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.println;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnPurple;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.skipLine;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.domainservices.AnalyticsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.infrastructure.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;

/**
 * AnalyticsMenu отвечает за предоставление доступа к функциям получения обобщённой информации по
 * кошельку. Вызывает для пользователя нужные функции AnalyticsService, формирующие отчёты
 * определённого типа. Печатает полученные от AnalyticsService данные в консоль или сохраняет в файл
 * при помощи методов DataPersistenceService. Выводит уведомления.
 */
public class AnalyticsMenu extends Menu {

  /** Показ меню. */
  @Override
  public void showMenu() {
    super.showMenu();
    printlnYellow("Меню аналитики:");
    println(
        """
                1. Вывод общей сводки.
                2. Вывод сводки по доходам.
                3. Вывод сводки по расходам.
                4. Вывод сводки по бюджетам и остаткам.
                5. Вывод списков операций и расширенная аналитика с фильтрацией по категориям или периодам.
                6. Возврат в главное меню.""");
    printYellow("Введите номер желаемого действия: ");
  }

  /** Направление на нужную функцию. */
  @Override
  public void handleUserInput() {
    String displayedUserNameForReports =
        "Отчёт для пользователя: " + PersonalBudgetingApp.getCurrentAppUser().getUsername() + ".\n";
    String pathToReportFile;
    String reportFormat;
    requestUserInput(); // складывается в переменную super.currentInput
    try {
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
      switch (getCurrentUserInput()) {
        case "1" -> { // отчёт-сводка по всем данным кошелька
          reportFormat = requestReportFormat(); // проверка желания вывода в консоль или в файл
          if (reportFormat.isEmpty()) { // вывод в консоль
            skipLine();
            // Обращение к сервису
            print(AnalyticsService.makeTotalSummary(wallet));
          } else if (reportFormat.equals(".txt")) { // вывод в файл
            pathToReportFile =
                DataPersistenceService.saveAnalyticsReportToFile(
                    deleteColorsFromString(
                        displayedUserNameForReports + AnalyticsService.makeTotalSummary(wallet)),
                    makeFilenameForReportFile("total_summary"),
                    ".txt");
            printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
          }
        }
        case "2" -> { // отчёт-сводка по доходам
          reportFormat = requestReportFormat();
          if (reportFormat.isEmpty()) { // вывод в консоль
            skipLine();
            // Обращение к сервису
            print(AnalyticsService.makeIncomeSummary(wallet));
          } else if (reportFormat.equals(".txt")) { // вывод в файл
            pathToReportFile =
                DataPersistenceService.saveAnalyticsReportToFile(
                    deleteColorsFromString(
                        displayedUserNameForReports + AnalyticsService.makeIncomeSummary(wallet)),
                    makeFilenameForReportFile("income_summary"),
                    ".txt");
            printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
          }
        }
        case "3" -> { // отчёт-сводка по расходам
          reportFormat = requestReportFormat();
          if (reportFormat.isEmpty()) { // вывод в консоль
            skipLine();
            // Обращение к сервису
            print(AnalyticsService.makeExpensesSummary(wallet));
          } else if (reportFormat.equals(".txt")) { // вывод в файл
            pathToReportFile =
                DataPersistenceService.saveAnalyticsReportToFile(
                    deleteColorsFromString(
                        displayedUserNameForReports + AnalyticsService.makeExpensesSummary(wallet)),
                    makeFilenameForReportFile("expenses_summary"),
                    ".txt");
            printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
          }
        }
        case "4" -> { // отчёт-сводка по бюджетам и расходованию лимитов
          reportFormat = requestReportFormat();
          if (reportFormat.isEmpty()) { // вывод в консоль
            skipLine();
            // Обращение к сервису
            print(AnalyticsService.makeBudgetCategoriesAndLimitsSummary(wallet));
          } else if (reportFormat.equals(".txt")) { // вывод в файл
            pathToReportFile =
                DataPersistenceService.saveAnalyticsReportToFile(
                    deleteColorsFromString(
                        displayedUserNameForReports
                            + AnalyticsService.makeBudgetCategoriesAndLimitsSummary(wallet)),
                    makeFilenameForReportFile("budget_limits_summary"),
                    ".txt");
            printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
          }
        }
        // Переход в AnalyticsExtendedMenu
        case "5" -> PersonalBudgetingApp.setCurrentMenu(new AnalyticsExtendedMenu());
        case "6" -> PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
        default -> printlnYellow("Некорректный ввод, введите цифру от 1 до 6.");
      }
    } catch (CancellationRequestedException e) {
      printlnPurple(e.getMessage());
    } catch (IOException e) {
      printlnRed(e.getMessage());
    }
  }
}
