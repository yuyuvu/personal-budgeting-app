package com.github.yuyuvu.personalbudgetingapp.integration;

import com.github.yuyuvu.personalbudgetingapp.appservices.AuthorizationService;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.InvalidCredentialsException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов из AuthorizationService и DataPersistenceService. */
public class AuthorizationAndDataPersistenceServicesTest {

  /**
   * Проверяем методы, регистрирующие и авторизующие пользователя. А также методы, записывающие в
   * файлы и считывающие данные пользователей из файлов.
   */
  @Test
  void registerAndLoginUserTest() {
    // Ошибки чтения / записи не выбрасываются
    Assertions.assertDoesNotThrow(() -> AuthorizationService.registerUser("test123", "test123"));

    // Файл нормально сериализуется и десериализуется
    try {
      // удаление прошлого файла
      Path pathToFile =
          Path.of("personal_budgeting_appdata").resolve("userdata_wallets").resolve("test123.json");
      Files.delete(pathToFile);

      // регистрация и добавление операции
      User user = AuthorizationService.registerUser("test123", "test123");
      String username = user.getUsername();
      WalletOperationsService.addIncome(user.getWallet(), 100, "траты", LocalDateTime.now());

      // сохранение в файл
      DataPersistenceService.saveUserdataToFile(user);

      // авторизация, загрузка из файла
      User userLoaded = AuthorizationService.logInToAccount(username);

      // проверки созданного при регистрации и прочитанного пользователей на равенство
      Assertions.assertNotNull(userLoaded);
      Assertions.assertEquals(username, userLoaded.getUsername());

      Assertions.assertTrue(
          userLoaded.getWallet().getWalletOperationsIncomeCategories().contains("траты"));
      Assertions.assertEquals(
          1, userLoaded.getWallet().getWalletOperationsIncomeCategories().size());
      Assertions.assertEquals(user.getWallet().getBalance(), userLoaded.getWallet().getBalance());

      // удаление файла
      Files.delete(pathToFile);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Проверяем методы, отвечающие за проверку логина, пароля и валидацию данных при регистрации и
   * аутентификации.
   */
  @Test
  void validateCredentialsTest() {
    try {
      final Path pathToFile =
          Path.of("personal_budgeting_appdata").resolve("userdata_wallets").resolve("test123.json");
      User alreadyRegisteredUser = AuthorizationService.registerUser("test123", "test123");

      // новое имя
      Assertions.assertThrows(
          CheckedIllegalArgumentException.class,
          () -> {
            // имя c пробелом не разрешено
            AuthorizationService.validateNewUsername("test 123");
          });
      Assertions.assertThrows(
          CheckedIllegalArgumentException.class,
          () -> {
            // имя меньше 3 символов не разрешено
            AuthorizationService.validateNewUsername("12");
          });
      Assertions.assertThrows(
          CheckedIllegalArgumentException.class,
          () -> {
            // имя из некорректных символов не разрешено
            AuthorizationService.validateNewUsername("_+2фыа+ЫФС?");
          });
      Assertions.assertThrows(
          InvalidCredentialsException.class,
          () -> {
            // имеющееся имя не разрешено
            AuthorizationService.validateNewUsername(alreadyRegisteredUser.getUsername());
          });

      // имеющееся имя
      Assertions.assertDoesNotThrow(
          () -> {
            // имеющееся имя подходит
            AuthorizationService.validateExistingUsername(alreadyRegisteredUser.getUsername());
          });
      Assertions.assertThrows(
          InvalidCredentialsException.class,
          () -> {
            // незарегистрированное не подходит
            AuthorizationService.validateExistingUsername("not_existent_username");
          });

      // новый пароль
      Assertions.assertThrows(
          CheckedIllegalArgumentException.class,
          () -> {
            // пароль c пробелом не разрешён
            AuthorizationService.validateNewPassword("test 123");
          });
      Assertions.assertThrows(
          CheckedIllegalArgumentException.class,
          () -> {
            // пароль меньше 3 символов не разрешён
            AuthorizationService.validateNewPassword("12");
          });

      // имеющийся пароль
      Assertions.assertDoesNotThrow(
          () -> {
            // пароль подходит
            AuthorizationService.validateExistingPassword("test123", "test123");
          });
      Assertions.assertThrows(
          InvalidCredentialsException.class,
          () -> {
            // пароль не подходит
            AuthorizationService.validateExistingPassword("test123", "incorrect");
          });

      Files.delete(pathToFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
