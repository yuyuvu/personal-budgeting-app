package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class BudgetingManagementMenu extends Menu {

    @Override
    public void showMenu() {
        println("""
                
                Меню управления категориями доходов и расходов:
                Операции с категориями расходов:
                1. Добавить новую категорию расходов с бюджетом (лимитом).
                2. Изменить значение существующего лимита для категории расходов.
                3. Изменить название существующей категории расходов.
                4. Объединить категории расходов.
                5. Удалить бюджет для категории расходов.
                Операции с категориями доходов:
                6. Изменить название категории доходов.
                7. Объединить категории доходов.
                8. Возврат в главное меню.""");
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

                }
                case "2" -> {

                }
                case "3" -> {

                }
                case "4" -> {
                    PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
                }
                default -> {
                    printlnYellow("Некорректный ввод, введите цифру от 1 до 7.");
                }
            }
        } catch (CancellationRequestedException e) {
            printlnPurple(e.getMessage());
            return;
        }
    }
}
