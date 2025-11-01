package com.github.yuyuvu.personalbudgetingapp.appservices;

import java.util.Properties;

public class ConfigManager {
    private Properties appProperies = new Properties();

    public ConfigManager() {
        appProperies.setProperty("app.show.notifications", "true");
    }

    public String checkNotificationsConfig() {
        return appProperies.getProperty("app.show.notifications");
    }

    public boolean reverseNotificationsConfig() {
        if (checkNotificationsConfig().equals("true")) {
            appProperies.setProperty("app.show.notifications", "false");
            return false;
        } else  {
            appProperies.setProperty("app.show.notifications", "true");
            return true;
        }
    }
}
