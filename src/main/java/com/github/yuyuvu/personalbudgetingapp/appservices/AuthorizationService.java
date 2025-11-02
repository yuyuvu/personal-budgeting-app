package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.InvalidCredentialsException;
import com.github.yuyuvu.personalbudgetingapp.model.User;

import java.io.IOException;
import java.util.HashMap;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

// TODO: Добавить хеширование паролей
public class AuthorizationService {
    private static HashMap<String, String> loadedUsernamesAndPasswords = new HashMap<>();

    // Получаем данные об уже имеющихся пользователях после полного выключения и перезапуска всего приложения
    static {
        try {
            loadedUsernamesAndPasswords = DataPersistenceService.getRegisteredUsernamesAndPasswords();
        } catch (Exception e) {
            // Ошибки чтения файлов с данными пользователей. Приложение всё ещё может работать,
            // но не будет помнить о пользователях из прошлого запуска.
            printlnRed(e.getMessage());
        }
    }

    private static HashMap<String, String> getLoadedUsernamesAndPasswords() {
        return loadedUsernamesAndPasswords;
    }

    public static boolean validateNewUsername(String tempNewUsername) throws CheckedIllegalArgumentException, InvalidCredentialsException {
        if (tempNewUsername.isBlank() || tempNewUsername.contains(" ") || !tempNewUsername.matches("^[a-zA-Z0-9]{3,}$")) {
            throw new CheckedIllegalArgumentException("Введено некорректное имя пользователя, введите имя из как минимум трёх символов (допустимы только цифры и латиница) без пробелов.");
        }
        if (checkUserExistenceIrrespectiveOfCase(tempNewUsername)) {
            throw new InvalidCredentialsException("Такой пользователь уже существует. Введите другое имя пользователя.");
        }
        return true;
    }

    public static boolean validateNewPassword(String tempNewPassword) throws CheckedIllegalArgumentException {
        if (tempNewPassword.isBlank() || tempNewPassword.contains(" ") || tempNewPassword.length() < 3) {
            throw new CheckedIllegalArgumentException("Введён некорректный пароль, введите пароль из как минимум трёх символов без пробелов.");
        }
        return true;
    }

    public static User registerUser(String inputNewUsername, String inputNewPassword) throws IOException {
        printlnGreen("Успешная регистрация пользователя " + inputNewUsername + "!");
        getLoadedUsernamesAndPasswords().put(inputNewUsername, inputNewPassword);
        User newUser = new User(inputNewUsername, inputNewPassword);
        // Следующие два вызова могут выбрасывать IOException
        DataPersistenceService.makeNewUserWalletFile(inputNewUsername);
        DataPersistenceService.saveUserdataToFile(newUser);
        return newUser;
    }

    public static boolean validateExistingUsername(String tempExistingUsername) throws InvalidCredentialsException {
        if (!checkUserExistence(tempExistingUsername)) {
            throw new InvalidCredentialsException("Такого пользователя не существует. Введите корректное имя существующего пользователя.");
        }
        return true;
    }

    public static boolean validateExistingPassword(String inputExistingUsername, String tempExistingPassword) throws InvalidCredentialsException {
        if (!getLoadedUsernamesAndPasswords().get(inputExistingUsername).equals(tempExistingPassword)) {
            throw new InvalidCredentialsException("Введён некорректный пароль для аккаунта пользователя " +  inputExistingUsername + ", повторите попытку.");
        }
        return true;
    }

    public static User logInToAccount(String inputExistingUsername) throws IOException {
        User loadedUser = DataPersistenceService.loadUserdataFromFile(inputExistingUsername);
        printlnGreen("Успешный вход в аккаунт пользователя " + inputExistingUsername + "!");
        return loadedUser;
    }

    public static boolean checkUserExistence(String username) {
        return getLoadedUsernamesAndPasswords().containsKey(username);
    }

    public static boolean checkUserExistenceIrrespectiveOfCase(String username) {
        for (String key : getLoadedUsernamesAndPasswords().keySet()) {
            if (key.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
}
