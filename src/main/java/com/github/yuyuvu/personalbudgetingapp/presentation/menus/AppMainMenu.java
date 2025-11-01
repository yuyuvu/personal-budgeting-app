package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.domainservices.NotificationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AppMainMenu extends Menu {

    @Override
    public void showMenu() {
        print(NotificationsService
                .checkAndPrepareNotifications(PersonalBudgetingApp.getCurrentAppUser().getWallet()));
        println("""
                
                Меню приложения:
                1. Просмотр информации о своих доходах, расходах и лимитах по категориям.
                2. Управление доходами, расходами и переводами другим пользователям.
                3. Управление категориями доходов, расходов и бюджетами.
                4. Экспорт / импорт снимков состояния кошелька.
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
                    PersonalBudgetingApp.setCurrentMenu(new BudgetingManagementMenu());
                }
                case "4" -> {
                    //TODO
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
