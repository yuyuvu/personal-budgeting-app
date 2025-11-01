package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.AuthorizationService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.NotificationsService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.time.LocalDateTime;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class IncomeAndExpensesManagementMenu extends Menu {

    @Override
    public void showMenu() {
        print(NotificationsService
                .checkAndPrepareNotifications(PersonalBudgetingApp.getCurrentAppUser().getWallet()));
        println("""
                
                Меню управления доходами и расходами:
                1. Добавление дохода.
                2. Добавление расхода.
                3. Перевод средств другому пользователю.
                4. Удаление ранее добавленного дохода или расхода.
                5. Возврат в главное меню.""");
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
                    handleTransferToAnotherUser(wallet);
                }
                case "4" -> {
                    handleWalletOperationRemoval(wallet);
                }
                case "5" -> {
                    PersonalBudgetingApp.setCurrentMenu(new AppMainMenu());
                }
                default -> {
                    printlnYellow("Некорректный ввод, введите цифру от 1 до 5.");
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
            dateTime = requestDateFromUser("");
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

    private void handleTransferToAnotherUser(Wallet wallet) throws CancellationRequestedException {
        double amount;
        String anotherUser;

        // Получение имени получателя перевода
        while (true) {
            try {
                if (wallet.getBalance() <= 0.0) {
                    printlnPurple("Обратите внимание: вы собираетесь зафиксировать перевод, уже имея отрицательный или нулевой баланс.");
                }

                printCyan("Введите имя пользователя (без учёта регистра), для которого предназначается перевод: ");
                requestUserInput();
                Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());

                if (PersonalBudgetingApp.getCurrentAppUser().getUsername().equalsIgnoreCase(getCurrentUserInput())) {
                    throw new CheckedIllegalArgumentException("Нельзя переводить средства самому себе.");
                }
                if (!AuthorizationService.checkUserExistenceIrrespectiveOfCase(getCurrentUserInput())) {
                    throw new CheckedIllegalArgumentException("Пользователя с указанным именем не существует. Повторите ввод.");
                }

                anotherUser = getCurrentUserInput().toLowerCase();
                break;
            } catch (CheckedIllegalArgumentException e) {
                printlnRed(e.getMessage());
            }
        }

        // Получение суммы перевода
        while (true) {
            try {
                printCyan(String.format("Введите сумму перевода для пользователя \"%s\": ", anotherUser));
                requestUserInput();
                Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
                amount = Double.parseDouble(getCurrentUserInput());
                if (amount <= 0) throw new CheckedIllegalArgumentException("Сумма перевода должна быть больше нуля. Повторите ввод.");
                break;
            } catch (NumberFormatException e) {
                printlnRed("Введено не число. Повторите ввод.");
            } catch (CheckedIllegalArgumentException e) {
                printlnRed(e.getMessage());
            }
        }

        // Перевод
        WalletOperationsService.transferMoneyToAnotherUser(PersonalBudgetingApp.getCurrentAppUser(), anotherUser, amount);
        printlnGreen(String.format("Перевод пользователю \"%s\" на сумму %.2f успешно осуществлён!", anotherUser, amount));
    }
}
