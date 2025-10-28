package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.model.User;

import java.util.HashSet;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class AuthorizationService {
    private static HashSet<String> loadedUsers; //= new HashSet<>();

    // Register already existed users
    static {
        loadedUsers = DataPersistenceService.getRegisteredUsernames();
    }

    public static User registerUser() {
        String inputNewUsername;
        String inputNewPassword;

        do {
            printCyan("Введите имя пользователя (логин): ");
            String tempNewUsername = PersonalBudgetingApp.getUserInput().nextLine();

            if (tempNewUsername == null || tempNewUsername.isEmpty() || tempNewUsername.contains(" ") || !tempNewUsername.matches("^[a-zA-Z0-9]+$")) {
                printlnRed("Введено некорректное имя пользователя, введите имя из как минимум одного символа (допустимы только цифры и латиница) без пробелов.");
                continue;
            }
            if (loadedUsers.contains(tempNewUsername)) {
                printlnRed("Такой пользователь уже существует. Введите другое имя пользователя.");
                continue;
            }

            inputNewUsername = tempNewUsername;
            break;
        } while (true);

        do {
            printCyan("Введите пароль: ");
            String tempNewPassword = PersonalBudgetingApp.getUserInput().nextLine();

            if (tempNewPassword == null || tempNewPassword.isEmpty() || tempNewPassword.contains(" ")) {
                printlnRed("Введён некорректный пароль, введите пароль из как минимум одного символа без пробелов.");
                continue;
            }

            inputNewPassword = tempNewPassword;
            break;
        } while (true);

        loadedUsers.add(inputNewUsername);
        DataPersistenceService.makeNewUserWalletFile(inputNewUsername);
        return new User(inputNewUsername, inputNewPassword);
    }

    public static User logInToAccount() {
        String inputExistingUsername;
        String inputExistingPassword;

        do {
            printCyan("Введите имя пользователя (логин): ");
            String tempExistingUsername = PersonalBudgetingApp.getUserInput().nextLine();

            if (!loadedUsers.contains(tempExistingUsername)) {
                printlnRed("Такого пользователя не существует. Введите корректное имя существующего пользователя.");
                continue;
            }

            inputExistingUsername = tempExistingUsername;
            break;
        } while (true);

        do {
            printCyan("Введите пароль: ");
            String tempExistingPassword = PersonalBudgetingApp.getUserInput().nextLine();

            if () {
                printlnRed("Введён некорректный пароль для аккаунта пользователя " +  inputExistingUsername + ", повторите попытку.");
                continue;
            }

            inputExistingPassword = tempExistingPassword;
            break;
        } while (true);

        return DataPersistenceService.loadUserdataFromFile(inputExistingUsername);
    }
}
