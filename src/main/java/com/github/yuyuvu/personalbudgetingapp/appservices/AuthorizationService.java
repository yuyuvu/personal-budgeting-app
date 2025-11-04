package com.github.yuyuvu.personalbudgetingapp.appservices;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;

import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.InvalidCredentialsException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import java.io.IOException;
import java.util.HashMap;

/**
 * Класс AuthorizationService отвечает за операции регистрации и авторизации. Постоянно хранит в
 * себе HashMap для сравнения вводимых данных для авторизации с правильными. После перезапуска
 * считывает данные для авторизации в эту HashMap из файлов данных пользователей при помощи методов
 * из DataPersistenceService. Обращается к PasswordHasher для вычисления хэшей вводимых паролей и
 * сравнения с правильным хэшем. Создаёт новые объекты User при регистрации пользователей.
 */
public class AuthorizationService {
  // Хэщ-таблица с данными для аутентификации
  private static HashMap<String, String[]> loadedUsernamesAndHashesAndSalts = new HashMap<>();

  private static HashMap<String, String[]> getLoadedUsernamesAndHashesAndSalts() {
    return loadedUsernamesAndHashesAndSalts;
  }

  /**
   * Перечисление, указывающее к каким индексам нужно обращаться для чтения хэшей и солей при
   * десериализации данных пользователей для аутентификации из файлов.
   */
  public enum PasswordData {
    HASH,
    SALT
  }

  // Получаем данные об уже имеющихся пользователях после полного выключения
  // и перезапуска всего приложения
  static {
    try {
      loadedUsernamesAndHashesAndSalts =
          DataPersistenceService.getRegisteredUsernamesAndHashesAndSalts();
    } catch (Exception e) {
      // Ошибки чтения файлов с данными пользователей. Приложение всё ещё может работать,
      // но не будет помнить о части пользователей из прошлого запуска.
      // Может возникнуть только при добавлении лишних файлов в директорию с данными
      // или при ручном изменении файла в неверный формат.
      printlnRed(e.getMessage());
    }
  }

  /**
   * Метод для валидации вводимого имени нового пользователя. Проверяет отсутствие пользователя с
   * введённым именем. И соответствие правилам именования.
   */
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

  /** Метод для валидации вводимого пароля нового пользователя. */
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

  /**
   * Метод для регистрации нового пользователя, если проверки имени и пароля прошли успешно. Создаёт
   * новую соль для пользователя. Создаёт новый объект пользователя. Сохраняет хэш и соль в
   * loadedUsernamesAndHashesAndSalts. Создаёт файл для хранения данных пользователя: кошелька и
   * данных для аутентификации. Также задаёт новому пользователю настройки приложения по-умолчанию.
   */
  public static User registerUser(String inputNewUsername, String inputNewPassword)
      throws IOException {
    String newSalt = PasswordHasher.SaltGenerator.makeSalt();
    String newHash = PasswordHasher.makePasswordHash(inputNewPassword, newSalt);
    String[] passwordData = new String[] {newHash, newSalt};
    getLoadedUsernamesAndHashesAndSalts().put(inputNewUsername, passwordData);

    User newUser =
        new User(inputNewUsername, passwordData, ConfigManager.makeAppConfigOnRegistration());

    // Следующие два вызова могут выбрасывать IOException
    DataPersistenceService.makeNewUserWalletFile(inputNewUsername);
    DataPersistenceService.saveUserdataToFile(newUser);

    return newUser;
  }

  /**
   * Метод для валидации вводимого имени имеющегося пользователя при аутентификации. Проверяет
   * наличие пользователя с введённым именем.
   */
  public static boolean validateExistingUsername(String tempExistingUsername)
      throws InvalidCredentialsException {
    if (!checkUserExistence(tempExistingUsername)) {
      throw new InvalidCredentialsException(
          "Такого пользователя не существует. Введите корректное имя существующего пользователя.");
    }
    return true;
  }

  /**
   * Метод для проверки правильности введённого пароля имеющегося пользователя при аутентификации.
   * Вычисляет хэш на основе введённого пароля и соли с правильным хэшем, хранящимся в
   * loadedUsernamesAndHashesAndSalts.
   */
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

  /**
   * При условии правильного ввода логина и пароля данный метод загружает из файла и возвращает
   * сохранённые при предыдущем выходе из аккаунта данные кошелька пользователя.
   */
  public static User logInToAccount(String inputExistingUsername) throws IOException {
    return DataPersistenceService.loadUserdataFromFile(inputExistingUsername);
  }

  /**
   * Метод проверяет наличие пользователя с определённым именем в loadedUsernamesAndHashesAndSalts.
   * Проверяет с учётом регистра.
   */
  public static boolean checkUserExistence(String username) {
    return getLoadedUsernamesAndHashesAndSalts().containsKey(username);
  }

  /**
   * Метод проверяет наличие пользователя с определённым именем в loadedUsernamesAndHashesAndSalts.
   * Проверяет без учёта регистра.
   */
  public static boolean checkUserExistenceIrrespectiveOfCase(String username) {
    for (String key : getLoadedUsernamesAndHashesAndSalts().keySet()) {
      if (key.equalsIgnoreCase(username)) {
        return true;
      }
    }
    return false;
  }
}
