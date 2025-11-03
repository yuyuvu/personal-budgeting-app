package com.github.yuyuvu.personalbudgetingapp.appservices;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Класс PasswordHasher хранит в себе два метода: 1) для получения уникальной соли пользователя при
 * регистрации; 2) для вычисления хэша на основе пароля и соли при авторизации. Они используются в
 * AuthorizationService.
 */
public class PasswordHasher {
  /*Для экономии места в сохраняемых файлах кошельков пользователей выбран алгоритм SHA256
  Помимо хэширования паролей, стоило бы также добавить шифрование самих файлов кошельков.
  Это направление для дальнейшего улучшения проекта. */
  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int ITERATIONS = 600000; // Рекомендуемое значение OWASP 2023 для SHA256
  private static final int HASH_LENGTH = 256; // 256 бит

  /** Метод для получения хэшей на основе пароля и соли. */
  public static String makePasswordHash(String password, String salt) {
    try {
      // Декодируем соль из Base64
      byte[] saltByteArray = Base64.getDecoder().decode(salt);

      PBEKeySpec spec =
          new PBEKeySpec(password.toCharArray(), saltByteArray, ITERATIONS, HASH_LENGTH);

      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
      // Генерируем сам хэш
      byte[] hash = factory.generateSecret(spec).getEncoded();

      return Base64.getEncoder().encodeToString(hash);

    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(
          "Проблемы при хэшировании паролей пользователей: " + e.getMessage());
    }
  }

  /**
   * Метод для получения уникальной соли при регистрации пользователя для более безопасного
   * вычисления хэша на основе неё и пароля.
   */
  static class SaltGenerator {
    public static String makeSalt() {
      SecureRandom random = new SecureRandom();
      byte[] salt = new byte[16];
      random.nextBytes(salt);

      return Base64.getEncoder().encodeToString(salt);
    }
  }
}
