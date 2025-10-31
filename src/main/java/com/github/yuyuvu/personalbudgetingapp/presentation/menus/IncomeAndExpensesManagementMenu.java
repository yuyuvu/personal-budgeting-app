package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import javax.swing.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class IncomeAndExpensesManagementMenu extends Menu {

    @Override
    public void showMenu() {
        println("""
                
                Меню управления доходами и расходами:
                1. Добавление дохода.
                2. Добавление расхода.
                3. Удаление ранее добавленного дохода или расхода.
                4. Возврат в главное меню.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
            switch (getCurrentUserInput()) {
                case "1" -> {
                    handleAddWalletOperation(wallet, true);
                }
                case "2" -> {
                    handleAddWalletOperation(wallet, false);
                }
                case "3" -> {
                    handleWalletOperationRemoval(wallet);
                }
                case "4" -> {
                    PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
                }
                default -> {
                    printlnYellow("Некорректный ввод, введите цифру от 1 до 4.");
                }
            }
        } catch (CancellationRequestedException e) {
            printlnPurple(e.getMessage());
            return;
        }
    }

    private void handleAddWalletOperation(Wallet wallet, boolean isIncome) throws CancellationRequestedException {
        String category;
        double amount;
        LocalDateTime dateTime = LocalDateTime.now();

        // Получение категории операции
        printCyan(String.format("Введите название категории %s: ", (isIncome ? "дохода" : "расхода")));
        requestUserInput();
        Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
        category = getCurrentUserInput().toLowerCase();
        if (category.isBlank()) {
            category = "без категории";
            printlnRed("Введено пустое название категории. Операция будет помечена как \"Без категории\".");
        }

        // Получение суммы
        do {
            try {
                printCyan(String.format("Введите сумму %s: ", (isIncome ? "дохода" : "расхода")));
                requestUserInput();
                Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
                amount = Double.parseDouble(getCurrentUserInput());
                if (amount <= 0) {
                    throw new IllegalArgumentException();
                }
                break;
            } catch (NumberFormatException e) {
                printlnRed("Введено не число. Повторите ввод.");
            } catch (IllegalArgumentException e) {
                printlnRed("Сумма должна быть больше нуля. Повторите ввод.");
            }
        } while (true);

        // Получение даты операции

        boolean doesUserWantToSpecifyDate;
        continueToRequestVarDoesUserWantToSpecifyDate: do {
            printCyan("Желаете указать дату и время операции? (да/нет; в случае выбора \"нет\" будут указаны текущие дата и время): ");
            requestUserInput();
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            switch (getCurrentUserInput().toLowerCase()) {
                case "да"  -> {
                    doesUserWantToSpecifyDate =  true;
                    break continueToRequestVarDoesUserWantToSpecifyDate;
                }
                case "нет"   -> {
                    doesUserWantToSpecifyDate =  false;
                    break continueToRequestVarDoesUserWantToSpecifyDate;
                }
                default -> {
                    printlnRed("Некорректный ввод. Введите \"да\" или \"нет\".");
                    continue;
                }
            }
        } while (true);

        // Автоматическое или ручное указание даты операции
        if (doesUserWantToSpecifyDate) {
            do {
                try {
                    printCyan("Укажите дату в формате \"день месяц год час:минуты\" (например, 05 05 2025 00:05): ");
                    requestUserInput();
                    Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
                    if (getCurrentUserInput().matches("^\\d{2}\\s+\\d{2}\\s+\\d{4}\\s+\\d{2}:\\d{2}$")) {
                        String[] splittedDateTime = getCurrentUserInput().split("\\s");
                        String[] splittedTime = splittedDateTime[3].split(":");
                        dateTime = LocalDateTime.of(Integer.parseInt(splittedDateTime[2]),
                                Integer.parseInt(splittedDateTime[1]),
                                Integer.parseInt(splittedDateTime[0]),
                                Integer.parseInt(splittedTime[0]),
                                Integer.parseInt(splittedTime[1]));
                        break;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException e) {
                    printlnRed("Дата введена в некорректном формате. Повторите ввод.");
                } catch (DateTimeException e) {
                    printlnRed("Введены невозможные значения для даты или времени. Повторите ввод.");
                }
            } while (true);
        }

        // Добавление операции
        if (isIncome) {
            WalletOperationsService.addIncome(wallet, amount, category, dateTime);
        } else {
            WalletOperationsService.addExpense(wallet, amount, category, dateTime);
        }
        printlnGreen(String.format("%s успешно добавлен!", (isIncome ? "Доход" : "Расход")));
    }

    private void handleWalletOperationRemoval(Wallet wallet) throws CancellationRequestedException {
        do {
            try {
                printCyan("Введите id операции, которую желаете удалить (можно посмотреть в меню вывода информации об операциях, \n--cancel для возврата в меню): ");
                requestUserInput();
                Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
                long id = Long.parseLong(getCurrentUserInput());
                boolean success = WalletOperationsService.removeWalletOperationById(wallet, id);
                if (success) {
                    printGreen("Операция успешно удалена!");
                    break;
                } else {
                    printlnRed("Операции с данным id не существует.");
                }
            } catch (NumberFormatException e) {
                printlnRed("Id операции должен содержать только числа.");
            }
        } while (true);
    }
}
