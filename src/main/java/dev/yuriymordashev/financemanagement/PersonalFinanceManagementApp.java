package dev.yuriymordashev.financemanagement;

import dev.yuriymordashev.financemanagement.applogic.AuthorizationSystem;
import dev.yuriymordashev.financemanagement.applogic.DataPersistenceSystem;
import dev.yuriymordashev.financemanagement.userdata.User;

import java.util.Scanner;

import static dev.yuriymordashev.financemanagement.applogic.ColorPrinter.*;

public class PersonalFinanceManagementApp {
    public static final Scanner userInput = new Scanner(System.in);

    public User currentUser;
    private final AuthorizationSystem authorizationSystem = new AuthorizationSystem();

    void start() {
        showGreetingMessage();
        while (true) {
            if (currentUser == null) {
                showAuthorizationMenu();
                handleUserAuthorizationInput();
            } else {
                showAppMainMenu();
                handleUserMainMenuInput();
            }
        }
    }

    private void showGreetingMessage() {
        printlnYellow("Итоговый проект по ООП выполнил Мордашев Юрий Вячеславович.");
        printlnGreen("Система управления личными финансами запущена!");
        skipLine();
    }

    private void showAuthorizationMenu() {
        println("""
                Меню авторизации:
                1. Зарегистрироваться в системе.
                2. Зайти в аккаунт имеющегося пользователя.
                3. Выключить приложение.""");
        printYellow("Введите номер желаемого действия:");
    }

    private void handleUserAuthorizationInput() {
        String currentInput = userInput.nextLine().strip();
        switch (currentInput) {
            case "1" -> {
                currentUser = authorizationSystem.registerUser();
            }
            case "2" -> {
                currentUser = authorizationSystem.logInToAccount();
            }
            case "3" -> {
                System.exit(0);
            }
            default -> {
                printlnYellow("Некорректный ввод, введите цифру от 1 до 3.");
            }
        }
    }

    private void showAppMainMenu() {
        println("""
                Меню приложения:
                1. Просмотр информации о своих доходах, расходах и лимитах по категориям.
                2. Добавление доходов или расходов.
                3. Управление категориями расходов и лимитами по ним.
                4. Перевод средств другому пользователю.
                5. Выход из аккаунта.""");
        printYellow("Введите номер желаемого действия:");
    }

    private void handleUserMainMenuInput() {
        String currentInput = userInput.nextLine().strip();
        switch (currentInput) {
            case "1" -> {

            }
            case "2" -> {

            }
            case "3" -> {

            }
            case "4" -> {

            }
            case "5" -> {
                DataPersistenceSystem.saveWalletDataToFile(currentUser.getUsername());
            }
            default -> {
                printlnYellow("Некорректный ввод, введите цифру от 1 до 3.");
            }
        }
    }
}
