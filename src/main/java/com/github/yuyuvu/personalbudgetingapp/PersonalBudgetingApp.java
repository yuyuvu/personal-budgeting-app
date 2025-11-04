package com.github.yuyuvu.personalbudgetingapp;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnYellow;

import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.presentation.menus.AuthorizationMenu;
import com.github.yuyuvu.personalbudgetingapp.presentation.menus.Menu;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Класс для точки входа в приложение. Также отвечает за основной цикл вывода меню и обработки ввода
 * пользователя. <br>
 * Хранит статические поля для текущего пользователя, текущего меню и сканера ввода, которые
 * используются другими классами.
 */
public class PersonalBudgetingApp {
  /* Получаем текущую системную кодировку и передаём её в InputStreamReader,
   * из которого будет читать Scanner
   * Это нужно для правильного считывания кириллицы из консоли */
  private static final String codePage = (String) System.getProperties().get("stdout.encoding");
  private static final Charset charsetName = Charset.forName(codePage);
  private static final Scanner userInput =
      new Scanner(new InputStreamReader(System.in, charsetName));

  private static Menu currentMenu = null;
  private static User currentAppUser = null;

  /** Точка входа в приложение, запускается из класса Main. */
  public void start() {
    showGreetingMessage();
    setCurrentMenu(new AuthorizationMenu());

    /* В процессе работы приложения в currentMenu через setCurrentMenu()
     * подставляются наследники абстрактного класса presentation.menus.Menu.
     * У их объектов в цикле ниже полиморфно вызываются методы showMenu() и handleUserInput().*/
    while (true) {
      // NotificationsService.checkNotifications();
      getCurrentMenu().showMenu();
      getCurrentMenu().handleUserInput();
    }
  }

  /** Метод, выводящий приветствие при запуске приложения. */
  private static void showGreetingMessage() {
    printlnYellow("Проект выполнил Мордашев Юрий Вячеславович.");
    printlnGreen("Система управления личными финансами запущена!");
    printlnGreen("Для получения помощи по приложению введите --help");
  }

  // Отдельные геттеры и сеттеры для полей класса.

  private static Menu getCurrentMenu() {
    return currentMenu;
  }

  public static void setCurrentMenu(Menu currentMenu) {
    PersonalBudgetingApp.currentMenu = currentMenu;
  }

  public static User getCurrentAppUser() {
    return currentAppUser;
  }

  public static void setCurrentAppUser(User currentAppUser) {
    PersonalBudgetingApp.currentAppUser = currentAppUser;
  }

  public static Scanner getUserInput() {
    return userInput;
  }
}
