package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;

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
                5. Расширенная аналитика с фильтрацией по категориям, суммам или периодам.
                6. Возврат в главное меню.""");
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
                    println("");
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
