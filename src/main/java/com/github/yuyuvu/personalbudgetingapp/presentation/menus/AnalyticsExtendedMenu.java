package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.AnalyticsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

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
        String reportFormat;
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
            switch (getCurrentUserInput()) {
                case "1" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        print(handleRequestByCategories(wallet));
                    } else if (reportFormat.equals(".txt")) {
                        String reportContent = handleRequestByCategories(wallet);
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + reportContent),
                                makeFilenameForReportFile("summary_by_categories"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "2" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        print(handleRequestByPeriod(wallet));
                    } else if (reportFormat.equals(".txt")) {
                        String reportContent = handleRequestByPeriod(wallet);
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + reportContent),
                                makeFilenameForReportFile("summary_by_period"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "3" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeIncomeWalletOperationsList(wallet));
                    } else if (reportFormat.equals(".txt")) {
                        pathToReportFile = DataPersistenceService.saveAnalyticsReportToFile(
                                deleteColorsFromString(displayedUserNameForReports + AnalyticsService.makeIncomeWalletOperationsList(wallet)),
                                makeFilenameForReportFile("income_operations_list"), ".txt");
                        printlnGreen(String.format("Отчёт успешно сохранён в \"%s\"!", pathToReportFile));
                    }
                }
                case "4" -> {
                    reportFormat = requestReportFormat();
                    if (reportFormat.isEmpty()) {
                        skipLine();
                        print(AnalyticsService.makeExpensesWalletOperationsList(wallet));
                    } else if (reportFormat.equals(".txt")) {
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

    private String handleRequestByCategories(Wallet wallet) throws CancellationRequestedException {
        String[] requestedCategories;
        boolean isIncome = requestIncomeOrExpenses();
        do {
            try {
                printCyan(String.format("Введите названия категорий %s, по которым требуется вывод. Указывайте их в кавычках и через пробел: ", (isIncome ? "дохода" : "расхода")));
                requestUserInput();
                Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
                if (getCurrentUserInput().toLowerCase()
                        .matches("^(\\s*\"(([^\\s\"']+)|((\\s*)([^\\s\"']+)(\\s*))+)\"\\s*)+$")) {
                    requestedCategories = getCurrentUserInput().toLowerCase().split("\"\\s+\"");
                    requestedCategories = Arrays.stream(requestedCategories)
                            .map(s -> s.replaceAll("\"", ""))
                            .map(String::strip)
                            .distinct().toArray(String[]::new);
                    printlnCyan("Введены категории: " + Arrays.toString(requestedCategories).replaceAll("[\\[\\]]", "") + ".");
                } else {
                    throw new CheckedIllegalArgumentException("Неверный формат ввода категорий. Повторите ввод в формате: \"категория\" \"категория2\" \"категория3\" и т.д.");
                }
                break;
            } catch (CheckedIllegalArgumentException e) {
                printlnRed(e.getMessage());
            }
        } while (true);
        return AnalyticsService.makeSummaryByCategories(wallet, isIncome, new ArrayList<>(Arrays.asList(requestedCategories)));
    }

    private String handleRequestByPeriod(Wallet wallet) throws CancellationRequestedException {
        LocalDateTime periodStart = requestDateFromUser("Введите дату и время начала периода фильтрации (включительно).\n");
        LocalDateTime periodEnd = requestDateFromUser("Введите дату и время конца периода фильтрации (включительно).\n");
        return AnalyticsService.makeSummaryByPeriod(wallet, periodStart, periodEnd);
    }

    private boolean requestIncomeOrExpenses() throws CancellationRequestedException {
        boolean isIncome;
        do {
            printCyan("Вывести результаты по доходам (1) или по расходам (2)? (введите 1 или 2): ");
            requestUserInput();
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            switch (getCurrentUserInput().toLowerCase()) {
                case "1"  -> {
                    isIncome = true;
                    return isIncome;
                }
                case "2"   -> {
                    isIncome = false;
                    return isIncome;
                }
                default -> printlnRed("Некорректный ввод. Введите \"1\" или \"2\".");
            }
        } while (true);
    }
}
