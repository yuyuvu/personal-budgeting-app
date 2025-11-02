package com.github.yuyuvu.personalbudgetingapp.presentation.menus;

import com.github.yuyuvu.personalbudgetingapp.PersonalBudgetingApp;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.appservices.SnapshotsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CancellationRequestedException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.SnapshotException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class CheckpointsSaveAndLoadMenu extends Menu {

    @Override
    public void showMenu() {
        //super.showMenu();
        skipLine();
        printlnYellow("Меню работы со снимками состояния:");
        println("""
                1. Экспорт/импорт всех данных кошелька.
                2. Экспорт/импорт всех доходов.
                3. Экспорт/импорт всех расходов.
                4. Экспорт/импорт всех бюджетов на категории расходов.
                5. Возврат в главное меню.""");
        printYellow("Введите номер желаемого действия: ");
    }

    @Override
    public void handleUserInput() {
        requestUserInput(); // складывается в переменную super.currentInput
        try {
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            Wallet wallet = PersonalBudgetingApp.getCurrentAppUser().getWallet();
            User user = PersonalBudgetingApp.getCurrentAppUser();
            switch (getCurrentUserInput()) {
                case "1" -> {
                    handleTotalExportImport(wallet, user);
                }
                case "2" -> {
                    handleIncomeExportImport(wallet, user);
                }
                case "3" -> {
                    handleExpensesExportImport(wallet, user);
                }
                case "4" -> {
                    handleBudgetsExportImport(wallet, user);
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
        }
    }

    private boolean requestExportOrImport() throws CancellationRequestedException {
        while (true) {
            printCyan("Желаете ли вы экспортировать (1) или импортировать (2) снимок состояния? (введите 1 или 2): ");
            requestUserInput();
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            switch (getCurrentUserInput().toLowerCase()) {
                case "1"  -> {
                    return true;
                }
                case "2"   -> {
                    return false;
                }
                default -> printlnRed("Некорректный ввод. Введите \"1\" или \"2\".");
            }
        }
    }

    private boolean requestImportCancellation(String categoryMessage) throws CancellationRequestedException {
        while (true) {
            printlnPurple(categoryMessage);
            printlnPurple("Внимание: импорт данных из снимка состояния сотрёт соответствующие данные вашего кошелька.");
            printPurple("Точно продолжить? (введите да или нет): ");
            requestUserInput();
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            switch (getCurrentUserInput().toLowerCase()) {
                case "да"  -> {
                    return false;
                }
                case "нет"   -> {
                    printlnGreen("Отмена импорта...");
                    return true;
                }
                default -> printlnRed("Некорректный ввод. Введите \"да\" или \"нет\".");
            }
        }
    }

    private String requestPathToSnapshotForImport() throws CancellationRequestedException {
        String pathToFile;
        while (true) {
            printCyan("Укажите абсолютный путь до файла снимка состояния: ");
            requestUserInput();
            Menu.checkUserInputForAppGeneralCommands(getCurrentUserInput());
            try {
                pathToFile = Path.of(getCurrentUserInput().replace("\"", "")).toAbsolutePath().toString();
                if (!Files.exists(Path.of(pathToFile))) {
                    throw new CheckedIllegalArgumentException("Файла по указанному пути не существует. Повторите ввод.");
                }
            } catch (CheckedIllegalArgumentException e) {
                printlnRed(e.getMessage());
                continue;
            } catch (InvalidPathException e) {
                printlnRed("Неверный формат пути до файла: " + e.getMessage());
                continue;
            }
            break;
        }
        return pathToFile;
    }

    private void handleTotalExportImport(Wallet wallet, User user) throws CancellationRequestedException {
        if (requestExportOrImport()) {
            handleTotalExport(wallet);
        } else {
            handleTotalImport(user);
        }
    }

    private void handleTotalExport(Wallet wallet) throws CancellationRequestedException {
        printlnYellow("Экспорт снимка всего кошелька.");

        String exportContents = null;
        try {
            exportContents = SnapshotsService.makeTotalExportContents(wallet);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        String whereSaved;
        try {
            whereSaved = DataPersistenceService.saveSnapshotToFile(exportContents, makeFilenameForReportFile("wallet_total_data"));
        } catch (IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);
    }

    private void handleTotalImport(User user) throws CancellationRequestedException {
        if (requestImportCancellation("Импорт снимка всего кошелька.")) {
            return;
        }
        String pathToFile = requestPathToSnapshotForImport();
        String loadedContents = null;

        try {
            loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
        } catch (CheckedIllegalArgumentException | IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        try {
            SnapshotsService.importTotalSnapshot(user, loadedContents);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
    }




    private void handleIncomeExportImport(Wallet wallet, User user) throws CancellationRequestedException {
        if (requestExportOrImport()) {
            handleIncomeExport(wallet);
        } else {
            handleIncomeImport(user);
        }
    }

    private void handleIncomeExport(Wallet wallet) throws CancellationRequestedException {
        printlnYellow("Экспорт снимка доходов.");

        String exportContents = null;
        try {
            exportContents = SnapshotsService.makeOnlyIncomeExportContents(wallet);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        String whereSaved;
        try {
            whereSaved = DataPersistenceService.saveSnapshotToFile(exportContents, makeFilenameForReportFile("wallet_only_income_data"));
        } catch (IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);
    }

    private void handleIncomeImport(User user) throws CancellationRequestedException {
        if (requestImportCancellation("Импорт снимка доходов.")) {
            return;
        }
        String pathToFile = requestPathToSnapshotForImport();
        String loadedContents = null;

        try {
            loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
        } catch (CheckedIllegalArgumentException | IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        try {
            SnapshotsService.importOnlyIncomeSnapshot(user, loadedContents);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
    }

    private void handleExpensesExportImport(Wallet wallet, User user) throws CancellationRequestedException {
        if (requestExportOrImport()) {
            handleExpensesExport(wallet);
        } else {
            handleExpensesImport(user);
        }
    }

    private void handleExpensesExport(Wallet wallet) throws CancellationRequestedException {
        printlnYellow("Экспорт снимка расходов.");

        String exportContents = null;
        try {
            exportContents = SnapshotsService.makeOnlyExpensesExportContents(wallet);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        String whereSaved;
        try {
            whereSaved = DataPersistenceService.saveSnapshotToFile(exportContents, makeFilenameForReportFile("wallet_only_expenses_data"));
        } catch (IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);
    }

    private void handleExpensesImport(User user) throws CancellationRequestedException {
        if (requestImportCancellation("Импорт снимка расходов.")) {
            return;
        }
        String pathToFile = requestPathToSnapshotForImport();
        String loadedContents = null;

        try {
            loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
        } catch (CheckedIllegalArgumentException | IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        try {
            SnapshotsService.importOnlyExpensesSnapshot(user, loadedContents);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
    }

    private void handleBudgetsExportImport(Wallet wallet, User user) throws CancellationRequestedException {
        if (requestExportOrImport()) {
            handleBudgetsExport(wallet);
        } else {
            handleBudgetsImport(user);
        }
    }

    private void handleBudgetsExport(Wallet wallet) throws CancellationRequestedException {
        printlnYellow("Экспорт снимка бюджетов.");

        String exportContents = null;
        try {
            exportContents = SnapshotsService.makeOnlyBudgetsExportContents(wallet);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        String whereSaved;
        try {
            whereSaved = DataPersistenceService.saveSnapshotToFile(exportContents, makeFilenameForReportFile("wallet_only_budgets_data"));
        } catch (IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Экспорт успешно совершён в файл: " + whereSaved);

    }

    private void handleBudgetsImport(User user) throws CancellationRequestedException {
        if (requestImportCancellation("Импорт снимка бюджетов.")) {
            return;
        }
        String pathToFile = requestPathToSnapshotForImport();
        String loadedContents = null;

        try {
            loadedContents = DataPersistenceService.loadSnapshotFromFile(pathToFile);
        } catch (CheckedIllegalArgumentException | IOException e) {
            printlnRed(e.getMessage());
            return;
        }

        try {
            SnapshotsService.importOnlyBudgetsSnapshot(user, loadedContents);
        } catch (SnapshotException e) {
            printlnRed(e.getMessage());
            return;
        }

        printlnGreen("Импорт успешно завершён из снимка состояния: " + pathToFile);
    }
}
