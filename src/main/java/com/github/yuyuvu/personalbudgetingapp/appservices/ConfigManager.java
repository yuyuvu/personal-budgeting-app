package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import java.util.HashMap;
import java.util.Properties;

/**
 * Класс ConfigManager - небольшой упрощённый менеджер конфигурации для приложения. Добавлен только
 * для возможности скрывать / включать обратно уведомления, так как они могут сильно загромождать
 * вывод. Настройки уведомлений специально сбрасываются при перезапуске приложения. При желании их
 * можно было бы также считывать из файла пользователя, как и данные кошелька вместе с данными для
 * аутентификации.
 */
public class ConfigManager {
  private static final HashMap<String, Properties> propertiesForUsers = new HashMap<>();

  /** Геттер для propertiesForUsers. */
  private static HashMap<String, Properties> getPropertiesForUsers() {
    return propertiesForUsers;
  }

  /** Получение настроек для текущего пользователя. */
  private static Properties getPropertiesForCurrentUser() {
    return getPropertiesForUsers().get(PersonalBudgetingApp.getCurrentAppUser().getUsername());
  }

  /**
   * Метод, который используется для создания объекта настроек и установления настроек в значение по
   * умолчанию при первом заходе в аккаунт какого-либо пользователя.
   */
  public static void makeAppConfigOnFirstLogin() {
    if (getPropertiesForCurrentUser() == null) {
      getPropertiesForUsers()
          .put(PersonalBudgetingApp.getCurrentAppUser().getUsername(), new Properties());
      getPropertiesForCurrentUser().setProperty("app.show.notifications", "true");
    }
  }

  /** Метод для проверки текущего значения настройки уведомлений. */
  public static String checkNotificationsConfigForCurrentUser() {
    return getPropertiesForCurrentUser().getProperty("app.show.notifications");
  }

  /** Метод для обращения текущего значения настройки уведомлений. */
  public static boolean reverseNotificationsConfigForCurrentUser() {
    if (checkNotificationsConfigForCurrentUser().equals("true")) {
      getPropertiesForCurrentUser().setProperty("app.show.notifications", "false");
      return false;
    } else {
      getPropertiesForCurrentUser().setProperty("app.show.notifications", "true");
      return true;
    }
  }
}
