package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.AnalyticsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.io.IOException;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AnalyticsMenu extends Menu {

    @Override
    public void showMenu() {
        super.showMenu();
        printlnYellow("Меню аналитики:");
        println("""
                1. Вывод общей сводки.
                2. Вывод сводки по доходам.
                3. Вывод сводки по расходам.
                4. Вывод сводки по бюджетам и остаткам.
                5. Вывод списков операций и расширенная аналитика с фильтрацией по категориям или периодам.
                6. Возврат в главное меню.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        String displayedUserNameForReports = "Отчёт для пользователя: " + PersonalBudgetingApp.getCurrentAppUser().getUsername() + ".\n";
        String pathToReportFile;
        String reportFormat;
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
            switch (getCurrentUserInput()) {
                case "1" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeTotalSummary(wallet));
                    } else if (reportFormat.equals(".txt")) {
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + AnalyticsService.makeTotalSummary(wallet)),
                                makeFilenameForReportFile("total_summary"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "2" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeIncomeSummary(wallet));
                    } else if (reportFormat.equals(".txt")) {
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + AnalyticsService.makeIncomeSummary(wallet)),
                                makeFilenameForReportFile("income_summary"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "3" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeExpensesSummary(wallet));
                    } else if (reportFormat.equals(".txt")) {
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + AnalyticsService.makeExpensesSummary(wallet)),
                                makeFilenameForReportFile("expenses_summary"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "4" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeBudgetCategoriesAndLimitsSummary(wallet));
                    } else if (reportFormat.equals(".txt")) {
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + AnalyticsService.makeBudgetCategoriesAndLimitsSummary(wallet)),
                                makeFilenameForReportFile("budget_limits_summary"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "5" -> {
                    PersonalBudgetingApp.setCurrentMenu(new AnalyticsExtendedMenu());
                }
                case "6" -> {
                    PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
                }
                default -> {
                    printlnYellow("Некорректный ввод, введите цифру от 1 до 6.");
                }
            }
        } catch (CancellationRequestedException e) {
            printlnPurple(e.getMessage());
            return;
        } catch (IOException e) {
            printlnRed(e.getMessage());
        }
    }
}
