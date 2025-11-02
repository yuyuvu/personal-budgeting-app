package com.github.yuyuvu.personalbudgetingapp.appservices;

import java.util.Properties;

public class ConfigManager {
  private final Properties appProperties = new Properties();

  public ConfigManager() {
    appProperties.setProperty("app.show.notifications", "true");
  }

  public String checkNotificationsConfig() {
    return appProperties.getProperty("app.show.notifications");
  }

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
