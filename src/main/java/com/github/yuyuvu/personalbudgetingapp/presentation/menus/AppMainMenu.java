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
                2. Добавление доходов или расходов.
                3. Управление категориями расходов и лимитами по ним.
                4. Перевод средств другому пользователю.
                5. Выход из аккаунта.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        getCurrentUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(currentInput);
            switch (currentInput) {
                case "1" -> {
                    println("");
                }
                case "2" -> {
                    println("");
                }
                case "3" -> {
                    println("");
                }
                case "4" -> {
                    println("");
                }
                case "5" -> {
                    logOutOfCurrentUser();
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
