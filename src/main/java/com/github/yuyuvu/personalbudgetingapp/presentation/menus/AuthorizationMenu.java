package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.AuthorizationService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AuthorizationMenu extends Menu {

    @Override
    public void showMenu() {
        skipLine();
        printlnYellow("Меню авторизации:");
        println("""
                1. Зарегистрироваться в системе.
                2. Зайти в аккаунт имеющегося пользователя.
                3. Выключить приложение.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            switch (getCurrentUserInput()) {
                case "1" -> {
                    PersonalBudgetingApp.setCurrentAppUser(AuthorizationService.registerUser());
                    PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
                }
                case "2" -> {
                    PersonalBudgetingApp.setCurrentAppUser(AuthorizationService.logInToAccount());
                    PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
                }
                case "3" -> {
                    turnOffApplication(true);
                }
                default -> {
                    printlnYellow("Некорректный ввод, введите цифру от 1 до 3.");
                }
            }
        } catch (CancellationRequestedException e) {
            printlnPurple(e.getMessage());
            return;
        }
    }
}
