package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public abstract class Menu {

    public abstract void showMenu();
    public abstract void handleUserInput();

    protected void turnOffApplictation() {
        // TODO: Data Persistence
        printlnGreen("Выключаем приложение...");
        System.exit(0);
    }

    protected void logOutOfCurrentUser() {
        printlnGreen("Осуществляется выход из аккаунта текущего пользователя...");
        DataPersistenceService.saveUserdataToFile(PersonalBudgetingApp.getCurrentAppUser());
        PersonalBudgetingApp.setCurrentAppUser(null);
        PersonalBudgetingApp.setCurrentMenu(new AuthorizationMenu());
    }
}
