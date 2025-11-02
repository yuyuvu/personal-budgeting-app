package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printCyan;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.println;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnPurple;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnYellow;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.skipLine;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.AuthorizationService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.InvalidCredentialsException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import java.io.IOException;

public class AuthorizationMenu extends Menu {

  @Override
  public void showMenu() {
    skipLine();
    printlnYellow("Меню авторизации:");
    println(
        """
                1. Зарегистрироваться в системе.
                2. Зайти в аккаунт имеющегося пользователя.
                3. Выключить приложение.""");
    printYellow("Введите номер желаемого действия: ");
  }

  @Override
  public void handleUserInput() {
    requestUserInput(); // складывается в переменную super.currentInput
    try {
      Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
      switch (getCurrentUserInput()) {
        case "1" -> {
          PersonalBudgetingApp.setCurrentAppUser(handleRegistration());
          PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
        }
        case "2" -> {
          PersonalBudgetingApp.setCurrentAppUser(handleLogInToAccount());
          PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
        }
        case "3" -> turnOffApplication(true);
        default -> printlnYellow("Некорректный ввод, введите цифру от 1 до 3.");
      }
    } catch (CancellationRequestedException e) {
      printlnPurple(e.getMessage());
    } catch (IOException e) {
      // Вывод сообщений при наличии ошибок с созданием файла пользователя при регистрации
      // или с загрузкой данных из него при авторизации.
      // В таком случае не пускаем в приложение.
      printlnRed(e.getMessage());
    }
  }

  private User handleRegistration() throws CancellationRequestedException, IOException {
    String inputNewUsername;
    String inputNewPassword;

    while (true) {
      printCyan("Введите имя пользователя (логин): ");
      requestUserInput();
      String tempNewUsername = getCurrentUserInput();
      Menu.checkUserInputForAppGeneralCommands(tempNewUsername);
      try {
        if (AuthorizationService.validateNewUsername(tempNewUsername)) {
          inputNewUsername = tempNewUsername;
          break;
        }
      } catch (CheckedIllegalArgumentException | InvalidCredentialsException e) {
        printlnRed(e.getMessage());
      }
    }

    while (true) {
      printCyan("Введите пароль: ");
      requestUserInput();
      String tempNewPassword = getCurrentUserInput();
      Menu.checkUserInputForAppGeneralCommands(tempNewPassword);
      try {
        if (AuthorizationService.validateNewPassword(tempNewPassword)) {
          inputNewPassword = tempNewPassword;
          break;
        }
      } catch (CheckedIllegalArgumentException e) {
        printlnRed(e.getMessage());
      }
    }
    // Данный вызов может выбрасывать IOException
    return AuthorizationService.registerUser(inputNewUsername, inputNewPassword);
  }

  private User handleLogInToAccount() throws CancellationRequestedException, IOException {
    String inputExistingUsername;

    while (true) {
      printCyan("Введите имя пользователя (логин) с учётом регистра: ");
      requestUserInput();
      String tempExistingUsername = getCurrentUserInput();
      Menu.checkUserInputForAppGeneralCommands(tempExistingUsername);
      try {
        if (AuthorizationService.validateExistingUsername(tempExistingUsername)) {
          inputExistingUsername = tempExistingUsername;
          break;
        }
      } catch (InvalidCredentialsException e) {
        printlnRed(e.getMessage());
      }
    }

    while (true) {
      printCyan("Введите пароль: ");
      requestUserInput();
      String tempExistingPassword = getCurrentUserInput();
      Menu.checkUserInputForAppGeneralCommands(tempExistingPassword);
      try {
        if (AuthorizationService.validateExistingPassword(
            inputExistingUsername, tempExistingPassword)) {
          break;
        }
      } catch (InvalidCredentialsException e) {
        printlnRed(e.getMessage());
      }
    }
    // Данный вызов может выбрасывать IOException
    return AuthorizationService.logInToAccount(inputExistingUsername);
  }
}
