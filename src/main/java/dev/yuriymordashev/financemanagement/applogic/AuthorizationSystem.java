package dev.yuriymordashev.financemanagement.applogic;

import dev.yuriymordashev.financemanagement.userdata.User;

import java.util.HashSet;

import static dev.yuriymordashev.financemanagement.PersonalFinanceManagementApp.userInput;
import static dev.yuriymordashev.financemanagement.applogic.ColorPrinter.*;

public class AuthorizationSystem {
    static HashSet<String> loadedUsers = new HashSet<>();

    public User registerUser() {
        String inputNewUsername;
        String inputNewPassword;

        do {
            printCyan("Введите имя пользователя (логин): ");
            String tempNewUsername = userInput.nextLine();

            if (tempNewUsername == null || tempNewUsername.isEmpty() || tempNewUsername.contains(" ") || !tempNewUsername.matches("^[a-zA-Z0-9]+$")) {
                printlnRed("Введено некорректное имя пользователя, введите имя из как минимум одного символа (допустимы только цифры и латиница) без пробелов.");
                continue;
            }
            if (DataPersistenceSystem.getRegisteredUsernames().contains(tempNewUsername)) {
                printlnRed("Такой пользователь уже существует. Введите другое имя пользователя.");
                continue;
            }

            inputNewUsername = tempNewUsername;
            break;
        } while (true);

        do {
            printlnCyan("Введите пароль: ");
            String tempNewPassword = userInput.nextLine();

            if (tempNewPassword == null || tempNewPassword.isEmpty() || tempNewPassword.contains(" ")) {
                printlnRed("Введён некорректный пароль, введите пароль из как минимум одного символа без пробелов.");
                continue;
            }

            inputNewPassword = tempNewPassword;
            break;
        } while (true);

        loadedUsers.add(inputNewUsername);
        DataPersistenceSystem.makeNewUserWalletFile(inputNewUsername);
        return new User(inputNewUsername, inputNewPassword);
    }

    public User logInToAccount() {
        return new User();
    }
}
