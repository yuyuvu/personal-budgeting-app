package com.github.yuyuvu.personalbudgetingapp.appservices;

import java.util.Properties;

/**
 * Класс ConfigManager - небольшой упрощённый менеджер конфигурации для приложения. Добавлен только
 * для возможности скрывать / включать обратно уведомления, так как они могут сильно загромождать
 * вывод.
 */
public class ConfigManager {
  private final Properties appProperties = new Properties();

  /**
   * Конструктор, который используется для возвращения настроек в значение по умолчанию при заходе в
   * аккаунт какого-либо пользователя.
   */
  public ConfigManager() {
    appProperties.setProperty("app.show.notifications", "true");
  }

  /** Метод для проверки текущего значения настройки уведомлений. */
  public String checkNotificationsConfig() {
    return appProperties.getProperty("app.show.notifications");
  }

  /** Метод для обращения текущего значения настройки уведомлений. */
  public boolean reverseNotificationsConfig() {
    if (checkNotificationsConfig().equals("true")) {
      appProperties.setProperty("app.show.notifications", "false");
      return false;
    } else {
      appProperties.setProperty("app.show.notifications", "true");
      return true;
    }
  }
}
