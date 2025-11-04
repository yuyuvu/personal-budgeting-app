package com.github.yuyuvu.personalbudgetingapp.unit.testappservices;

import com.github.yuyuvu.personalbudgetingapp.appservices.PasswordHasher;
import java.util.Base64;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Класс для тестов методов PasswordHasher. */
public class PasswordHasherTest {

  /** Подготавливаем нужную локаль перед каждым тестом. */
  @BeforeEach
  void prepareLocale() {
    Locale.setDefault(Locale.US);
  }

  /** Проверяем метод генерации хэша. */
  @Test
  void makePasswordHashTest() {
    Assertions.assertDoesNotThrow(
        () -> {
          String salt = PasswordHasher.SaltGenerator.makeSalt();
          String hash1 = PasswordHasher.makePasswordHash("password", salt);
          String hash2 = PasswordHasher.makePasswordHash("password", salt);

          // равный хэш с одним паролем и солью
          Assertions.assertEquals(hash1, hash2);

          hash1 = PasswordHasher.makePasswordHash("password", salt);
          hash2 = PasswordHasher.makePasswordHash("password12345", salt);

          // неравный хэш с отличающимся паролем и одной солью
          Assertions.assertNotEquals(hash1, hash2);

          hash1 = PasswordHasher.makePasswordHash("password", salt);
          hash2 = PasswordHasher.makePasswordHash("password", "FLDmIK/p1IIo/uCAgzfcsw==");

          // неравный хэш с одним паролем и отличающейся солью
          Assertions.assertNotEquals(hash1, hash2);
        });
  }

  /** Проверяем метод генерации соли. */
  @Test
  void makeSaltTest() {
    Assertions.assertDoesNotThrow(
        () -> {
          for (int i = 1; i <= 10; i++) {
            String salt = PasswordHasher.SaltGenerator.makeSalt();
            if (Base64.getDecoder().decode(salt).length != 16) {
              throw new RuntimeException(
                  "Не та длина соли для хэширования: "
                      + Base64.getDecoder().decode(salt).length
                      + " "
                      + salt);
            }
          }
        });
  }
}
