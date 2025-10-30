package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AppMainMenu extends Menu {

    @Override
    public void showMenu() {
        println("""
                
                Меню приложения:
                1. Просмотр информации о своих доходах, расходах и лимитах по категориям.
                2. Управление доходами и расходами.
                3. Управление категориями расходов и лимитами по ним.
                4. Перевод средств другому пользователю.
                5. Выход из аккаунта.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            switch (getCurrentUserInput()) {
                case "1" -> {
                    PersonalBudgetingApp.setCurrentMenu(new AnalyticsMenu());
                }
                case "2" -> {
                    PersonalBudgetingApp.setCurrentMenu(new IncomeAndExpensesManagementMenu());
                }
                case "3" -> {
                    println("Не добавлено.");
                }
                case "4" -> {
                    println("Не добавлено.");
                }
                case "5" -> {
                    logOutOfCurrentUser(true);
                }
                default -> {
                    printlnYellow("Некорректный ввод, введите цифру от 1 до 5.");
                }
            }
        } catch (CancellationRequestedException e) {
            printlnPurple(e.getMessage());
            return;
        }
    }
}
