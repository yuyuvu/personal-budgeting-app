package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.AnalyticsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.io.IOException;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AnalyticsExtendedMenu extends Menu {

    @Override
    public void showMenu() {
        println("""
                
                Меню расширенной аналитики:
                1. Вывод данных по доходам или расходам с фильтрацией по категориям.
                2. Вывод данных по доходам или расходам с фильтрацией по периоду.
                3. Вывод всех операций дохода.
                4. Вывод всех операций расхода.
                5. Возврат в меню базовой аналитики.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        String displayedUserNameForReports = "Отчёт для пользователя: " + PersonalBudgetingApp.getCurrentAppUser().getUsername() + ".\n";
        String pathToReportFile;
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
            switch (getCurrentUserInput()) {
                case "1" -> {
                    if (requestReportFormat().isEmpty()) {
                        skipLine();
                        print(handleRequestByCategories(wallet));
                    } else if (requestReportFormat().equals(".txt")) {
                        String reportContent = handleRequestByCategories(wallet);
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + reportContent),
                                makeFilenameForReportFile("summary_by_categories"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "2" -> {
                    if (requestReportFormat().isEmpty()) {
                        skipLine();
                        print(handleRequestByPeriod(wallet));
                    } else if (requestReportFormat().equals(".txt")) {
                        String reportContent = handleRequestByPeriod(wallet);
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + reportContent),
                                makeFilenameForReportFile("summary_by_period"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "3" -> {
                    if (requestReportFormat().isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeIncomeWalletOperationsList(wallet));
                    } else if (requestReportFormat().equals(".txt")) {
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + AnalyticsService.makeIncomeWalletOperationsList(wallet)),
                                makeFilenameForReportFile("income_operations_list"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "4" -> {
                    if (requestReportFormat().isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeExpensesWalletOperationsList(wallet));
                    } else if (requestReportFormat().equals(".txt")) {
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + AnalyticsService.makeExpensesWalletOperationsList(wallet)),
                                makeFilenameForReportFile("expenses_operations_list"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "5" -> {
                    PersonalBudgetingApp.setCurrentMenu(new AnalyticsMenu());
                }
                default -> {
                    printlnYellow("Некорректный ввод, введите цифру от 1 до 5.");
                }
            }
        } catch (CancellationRequestedException e) {
            printlnPurple(e.getMessage());
            return;
        } catch (IOException e) {
            printlnRed(e.getMessage());
        }
    }

    private String handleRequestByCategories(Wallet wallet) {
        AnalyticsService.makeSummaryByCategories(wallet, isIncome, categories);
    }

    private String handleRequestByPeriod(Wallet wallet) {
        AnalyticsService.makeSummaryByPeriod(wallet, isIncome, periodStart, periodEnd);
    }
}
