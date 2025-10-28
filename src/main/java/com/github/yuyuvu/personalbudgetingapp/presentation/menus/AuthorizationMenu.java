package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.AuthorizationService;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AuthorizationMenu extends Menu {

    @Override
    public void showMenu() {
        println("""
                
                Меню авторизации:
                1. Зарегистрироваться в системе.
                2. Зайти в аккаунт имеющегося пользователя.
                3. Выключить приложение.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        String currentInput = PersonalBudgetingApp.getUserInput().nextLine().strip();
        switch (currentInput) {
            case "1" -> {
                PersonalBudgetingApp.setCurrentAppUser(AuthorizationService.registerUser());
                PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
            }
            case "2" -> {
                PersonalBudgetingApp.setCurrentAppUser(AuthorizationService.logInToAccount());
                PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
            }
            case "3" -> {
                turnOffApplictation();
            }
            default -> {
                printlnYellow("Некорректный ввод, введите цифру от 1 до 3.");
            }
        }
    }
}
