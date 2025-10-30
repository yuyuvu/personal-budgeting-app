package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.presentation.menus.Menu;

import java.util.HashMap;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

// TODO: Добавить хеширование паролей
public class AuthorizationService {
    private static HashMap<String, String> loadedUsernamesAndPasswords;

    // Получаем данные об уже имеющихся пользователях после полного выключения и перезапуска всего приложения
    static {
        loadedUsernamesAndPasswords = DataPersistenceService.getRegisteredUsernamesAndPasswords();
    }

    public static User registerUser() throws CancellationRequestedException {
        String inputNewUsername;
        String inputNewPassword;

        do {
            printCyan("Введите имя пользователя (логин): ");
            String tempNewUsername = PersonalBudgetingApp.getUserInput().nextLine().strip();

            Menu.checkUserInputForAppGeneralCommands(tempNewUsername);
            if (tempNewUsername.isBlank() || tempNewUsername.contains(" ") || !tempNewUsername.matches("^[a-zA-Z0-9]+$")) {
                printlnRed("Введено некорректное имя пользователя, введите имя из как минимум одного символа (допустимы только цифры и латиница) без пробелов.");
                continue;
            }
            if (checkUserExistence(tempNewUsername)) {
                printlnRed("Такой пользователь уже существует. Введите другое имя пользователя.");
                continue;
            }

            inputNewUsername = tempNewUsername;
            break;
        } while (true);

        do {
            printCyan("Введите пароль: ");
            String tempNewPassword = PersonalBudgetingApp.getUserInput().nextLine().strip();

            Menu.checkUserInputForAppGeneralCommands(tempNewPassword);
            if (tempNewPassword.isBlank() || tempNewPassword.contains(" ")) {
                printlnRed("Введён некорректный пароль, введите пароль из как минимум одного символа без пробелов.");
                continue;
            }

            inputNewPassword = tempNewPassword;
            break;
        } while (true);

        printlnGreen("Успешная регистрация пользователя " + inputNewUsername + "!");
        loadedUsernamesAndPasswords.put(inputNewUsername, inputNewPassword);
        DataPersistenceService.makeNewUserWalletFile(inputNewUsername);
        return new User(inputNewUsername, inputNewPassword);
    }

    public static User logInToAccount() throws CancellationRequestedException {
        String inputExistingUsername;

        do {
            printCyan("Введите имя пользователя (логин): ");
            String tempExistingUsername = PersonalBudgetingApp.getUserInput().nextLine().strip();

            Menu.checkUserInputForAppGeneralCommands(tempExistingUsername);
            if (!checkUserExistence(tempExistingUsername)) {
                printlnRed("Такого пользователя не существует. Введите корректное имя существующего пользователя.");
                continue;
            }

            inputExistingUsername = tempExistingUsername;
            break;
        } while (true);

        do {
            printCyan("Введите пароль: ");
            String tempExistingPassword = PersonalBudgetingApp.getUserInput().nextLine().strip();

            Menu.checkUserInputForAppGeneralCommands(tempExistingPassword);
            if (!loadedUsernamesAndPasswords.get(inputExistingUsername).equals(tempExistingPassword)) {
                printlnRed("Введён некорректный пароль для аккаунта пользователя " +  inputExistingUsername + ", повторите попытку.");
                continue;
            }
            break;
        } while (true);

        printlnGreen("Успешный вход в аккаунт пользователя " + inputExistingUsername + "!");
        return DataPersistenceService.loadUserdataFromFile(inputExistingUsername);
    }

    public static boolean checkUserExistence(String username) {
        return loadedUsernamesAndPasswords.containsKey(username);
    }
}
