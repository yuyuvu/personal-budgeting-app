package com.github.yuyuvu.personalbudgetingapp.appservices;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;

import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.InvalidCredentialsException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import java.io.IOException;
import java.util.HashMap;

/** Класс AuthorizationService. */
public class AuthorizationService {
  private static HashMap<String, String[]> loadedUsernamesAndHashesAndSalts = new HashMap<>();

  public enum PasswordData {
    HASH,
    SALT
  }

  // Получаем данные об уже имеющихся пользователях после полного выключения и перезапуска всего
  // приложения
  static {
    try {
      loadedUsernamesAndHashesAndSalts =
          DataPersistenceService.getRegisteredUsernamesAndHashesAndSalts();
    } catch (Exception e) {
      // Ошибки чтения файлов с данными пользователей. Приложение всё ещё может работать,
      // но не будет помнить о пользователях из прошлого запуска.
      printlnRed(e.getMessage());
    }
  }

  private static HashMap<String, String[]> getLoadedUsernamesAndHashesAndSalts() {
    return loadedUsernamesAndHashesAndSalts;
  }

  public static boolean validateNewUsername(String tempNewUsername)
      throws CheckedIllegalArgumentException, InvalidCredentialsException {
    if (tempNewUsername.isBlank()
        || tempNewUsername.contains(" ")
        || !tempNewUsername.matches("^[a-zA-Z0-9]{3,}$")) {
      throw new CheckedIllegalArgumentException(
          "Введено некорректное имя пользователя, введите имя из как минимум трёх символов "
              + "(допустимы только цифры и латиница) без пробелов.");
    }
    if (checkUserExistenceIrrespectiveOfCase(tempNewUsername)) {
      throw new InvalidCredentialsException(
          "Такой пользователь уже существует. Введите другое имя пользователя.");
    }
    return true;
  }

  public static boolean validateNewPassword(String tempNewPassword)
      throws CheckedIllegalArgumentException {
    if (tempNewPassword.isBlank()
        || tempNewPassword.contains(" ")
        || tempNewPassword.length() < 3) {
      throw new CheckedIllegalArgumentException(
          "Введён некорректный пароль, введите пароль из как минимум трёх символов без пробелов.");
    }
    return true;
  }

  public static User registerUser(String inputNewUsername, String inputNewPassword)
      throws IOException {
    String newSalt = PasswordHasher.SaltGenerator.makeSalt();
    String newHash = PasswordHasher.makePasswordHash(inputNewPassword, newSalt);
    String[] passwordData = new String[] {newHash, newSalt};
    getLoadedUsernamesAndHashesAndSalts().put(inputNewUsername, passwordData);

    User newUser = new User(inputNewUsername, passwordData);

    // Следующие два вызова могут выбрасывать IOException
    DataPersistenceService.makeNewUserWalletFile(inputNewUsername);
    DataPersistenceService.saveUserdataToFile(newUser);

    printlnGreen("Успешная регистрация пользователя " + inputNewUsername + "!");
    return newUser;
  }

  public static boolean validateExistingUsername(String tempExistingUsername)
      throws InvalidCredentialsException {
    if (!checkUserExistence(tempExistingUsername)) {
      throw new InvalidCredentialsException(
          "Такого пользователя не существует. Введите корректное имя существующего пользователя.");
    }
    return true;
  }

  public static boolean validateExistingPassword(
      String inputExistingUsername, String tempExistingPassword)
      throws InvalidCredentialsException {
    String hash =
        getLoadedUsernamesAndHashesAndSalts()
            .get(inputExistingUsername)[PasswordData.HASH.ordinal()];
    String salt =
        getLoadedUsernamesAndHashesAndSalts()
            .get(inputExistingUsername)[PasswordData.SALT.ordinal()];
    if (!hash.equals(PasswordHasher.makePasswordHash(tempExistingPassword, salt))) {
      throw new InvalidCredentialsException(
          "Введён некорректный пароль для аккаунта пользователя "
              + inputExistingUsername
              + ", повторите попытку.");
    }
    return true;
  }

  public static User logInToAccount(String inputExistingUsername) throws IOException {
    User loadedUser = DataPersistenceService.loadUserdataFromFile(inputExistingUsername);
    printlnGreen("Успешный вход в аккаунт пользователя " + inputExistingUsername + "!");
    return loadedUser;
  }

  public static boolean checkUserExistence(String username) {
    return getLoadedUsernamesAndHashesAndSalts().containsKey(username);
  }

  public static boolean checkUserExistenceIrrespectiveOfCase(String username) {
    for (String key : getLoadedUsernamesAndHashesAndSalts().keySet()) {
      if (key.equalsIgnoreCase(username)) {
        return true;
      }
    }
    return false;
  }
}
