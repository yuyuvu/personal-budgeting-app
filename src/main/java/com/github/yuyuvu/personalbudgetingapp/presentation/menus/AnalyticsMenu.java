package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.domainservices.AnalyticsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AnalyticsMenu extends Menu {

    @Override
    public void showMenu() {
        println("""
                
                Меню аналитики:
                1. Вывод общей сводки.
                2. Вывод сводки по доходам.
                3. Вывод сводки по расходам.
                4. Вывод сводки по бюджетам и остаткам.
                5. Вывод списков операций и расширенная аналитика с фильтрацией по категориям, суммам или периодам.
                6. Возврат в главное меню.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
            switch (getCurrentUserInput()) {
                case "1" -> {
                    skipLine();
                    AnalyticsService.printTotalSummary(wallet);
                }
                case "2" -> {
                    skipLine();
                    AnalyticsService.printIncomeSummary(wallet);
                }
                case "3" -> {
                    skipLine();
                    AnalyticsService.printExpensesSummary(wallet);
                }
                case "4" -> {
                    skipLine();
                    AnalyticsService.printBudgetCategoriesAndLimitsSummary(wallet);
                }
                case "5" -> {
                    //TODO
                    println("Не добавлено.");
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
        }
    }
}
