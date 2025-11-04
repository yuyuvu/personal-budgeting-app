package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import java.util.Properties;

/**
 * Класс ConfigManager - менеджер конфигурации для приложения. Сейчас добавлен только для
 * возможности скрывать / включать обратно уведомления, так как они могут сильно загромождать вывод.
 * <br>
 * Настройки каждого пользователя хранятся в объекте User и сохраняются между перезапусками
 * аналогично данным кошелька и данным для аутентификации.
 */
public class ConfigManager {
  /** Перечисление для хранения единообразных имён для отдельных настроек. */
  private enum ConfigProperties {
    APP_SHOW_NOTIFICATIONS
  }

  /**
   * Перечисление для возможности установления и проверки заранее ожидаемых значений для отдельных
   * настроек.
   */
  public enum BooleanPropertiesValues {
    TRUE,
    FALSE
  }

  /**
   * Метод, который используется для создания объекта настроек и установления настроек в значение по
   * умолчанию при регистрации какого-либо нового пользователя.
   */
  public static Properties makeAppConfigOnRegistration() {
    Properties defaultProperties = new Properties();
    defaultProperties.setProperty(
        ConfigProperties.APP_SHOW_NOTIFICATIONS.name(), BooleanPropertiesValues.TRUE.name());
    return defaultProperties;
  }

  /** Метод для проверки текущего значения настройки уведомлений. */
  public static String checkNotificationsConfigForCurrentUser() {
    return PersonalBudgetingApp.getCurrentAppUser()
        .getUserAppConfig()
        .getProperty(ConfigProperties.APP_SHOW_NOTIFICATIONS.name());
  }

  /** Метод для обращения текущего значения настройки уведомлений на противоположное. */
  public static boolean reverseNotificationsConfigForCurrentUser() {
    if (checkNotificationsConfigForCurrentUser().equals(BooleanPropertiesValues.TRUE.name())) {
      PersonalBudgetingApp.getCurrentAppUser()
          .getUserAppConfig()
          .setProperty(
              ConfigProperties.APP_SHOW_NOTIFICATIONS.name(), BooleanPropertiesValues.FALSE.name());
      return false;
    } else {
      PersonalBudgetingApp.getCurrentAppUser()
          .getUserAppConfig()
          .setProperty(
              ConfigProperties.APP_SHOW_NOTIFICATIONS.name(), BooleanPropertiesValues.TRUE.name());
      return true;
    }
  }
}
