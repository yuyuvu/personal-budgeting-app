package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public abstract class Menu {

    protected String currentInput;

    public abstract void showMenu();
    public abstract void handleUserInput();

    protected static void turnOffApplication() {
        // TODO: Data Persistence
        printlnGreen("Выключаем приложение...");
        System.exit(0);
    }

    protected static void logOutOfCurrentUser() {
        printlnGreen("Осуществляется выход из аккаунта текущего пользователя...");
        DataPersistenceService.saveUserdataToFile(PersonalBudgetingApp.getCurrentAppUser());
        PersonalBudgetingApp.setCurrentAppUser(null);
        PersonalBudgetingApp.setCurrentMenu(new AuthorizationMenu());
    }

    protected void getCurrentUserInput() {
        currentInput = PersonalBudgetingApp.getUserInput().nextLine().strip();
    }

    public static void checkUserInputForAppGeneralCommands(String userInput) throws CancellationRequestedException {
        switch (userInput) {
            case "--cancel" -> {
                throw new CancellationRequestedException();
            }
            case "--help" -> {
                printlnCyan("""
                        Помощь по приложению:
                        --cancel - отмена текущего выбора и возврат в предыдущее меню;
                        --help - вывод помощи по приложению;
                        --logout - выход из текущего аккаунта пользователя;
                        --turnoff - выключение приложения.""");
                throw new CancellationRequestedException();
            }
            case "--logout" -> {
                logOutOfCurrentUser();
                throw new CancellationRequestedException();
            }
            case "--turnoff" -> {
                logOutOfCurrentUser();
                turnOffApplication();
            }
        }
    }
}
