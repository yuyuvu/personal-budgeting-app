package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.exceptions.SnapshotException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class SnapshotsService {
    private static final ObjectMapper jsonObjectMapper = new ObjectMapper();

    public static String makeTotalExportContents(Wallet wallet) throws SnapshotException {
        String result = "";
        try (StringWriter resultWriter = new StringWriter()) {
            jsonObjectMapper.writeValue(resultWriter, wallet);
            result = resultWriter.toString();
        } catch (IOException | JacksonException e) {
            throw new SnapshotException("Проблемы с формированием содержимого снапшота: " + e.getMessage());
        }
        return result;
    }

    public static String makeOnlyIncomeExportContents(Wallet wallet) throws SnapshotException {
        String result = "";
        try (StringWriter resultWriter = new StringWriter()) {
            jsonObjectMapper.writeValue(resultWriter, wallet.getIncomeWalletOperations());
            result = resultWriter.toString();
        } catch (IOException | JacksonException e) {
            throw new SnapshotException("Проблемы с формированием содержимого снапшота: " + e.getMessage());
        }
        return result;
    }

    public static String makeOnlyExpensesExportContents(Wallet wallet) throws SnapshotException {
        String result = "";
        try (StringWriter resultWriter = new StringWriter()) {
            jsonObjectMapper.writeValue(resultWriter, wallet.getExpensesWalletOperations());
            result = resultWriter.toString();
        } catch (IOException | JacksonException e) {
            throw new SnapshotException("Проблемы с формированием содержимого снапшота: " + e.getMessage());
        }
        return result;
    }

    public static String makeOnlyBudgetsExportContents(Wallet wallet) throws SnapshotException {
        String result = "";
        try (StringWriter resultWriter = new StringWriter()) {
            jsonObjectMapper.writeValue(resultWriter, wallet.getBudgetCategoriesAndLimits());
            result = resultWriter.toString();
        } catch (IOException | JacksonException e) {
            throw new SnapshotException("Проблемы с формированием содержимого снапшота: " + e.getMessage());
        }
        return result;
    }

    public static void importTotalSnapshot(User user, String snapshotContents) throws SnapshotException {
        Wallet readWalletData;
        try {
            readWalletData = jsonObjectMapper.readValue(snapshotContents, Wallet.class);
        } catch (JacksonException e) {
            throw new SnapshotException(
                    paintRed("""
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
        }
        user.setWallet(readWalletData);
    }

    public static void importOnlyIncomeSnapshot(User user, String snapshotContents) throws SnapshotException {
        ArrayList<Wallet.WalletOperation> readWalletIncomeOperations;
        try {
            TypeReference<ArrayList<Wallet.WalletOperation>> tr = new TypeReference<ArrayList<Wallet.WalletOperation>>(){};
            readWalletIncomeOperations = jsonObjectMapper.readValue(snapshotContents, tr);
            if (readWalletIncomeOperations.stream().anyMatch(wo -> !wo.isIncome())) {
                throw new SnapshotException("Не тот формат снимка состояния. Предоставлены расходы.");
            }
        } catch (JacksonException | SnapshotException e) {
            throw new SnapshotException(
                    paintRed("""
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
        }
        ArrayList<Wallet.WalletOperation> tempSaveExpenses = user.getWallet().getExpensesWalletOperations();
        user.getWallet().getWalletOperations().clear();
        user.getWallet().getWalletOperations().addAll(readWalletIncomeOperations);
        user.getWallet().getWalletOperations().addAll(tempSaveExpenses);
    }

    public static void importOnlyExpensesSnapshot(User user, String snapshotContents) throws SnapshotException {
        ArrayList<Wallet.WalletOperation> readWalletExpensesOperations;
        try {
            TypeReference<ArrayList<Wallet.WalletOperation>> tr = new TypeReference<ArrayList<Wallet.WalletOperation>>(){};
            readWalletExpensesOperations = jsonObjectMapper.readValue(snapshotContents, tr);
            if (readWalletExpensesOperations.stream().anyMatch(Wallet.WalletOperation::isIncome)) {
                throw new SnapshotException("Не тот формат снимка состояния. Предоставлены доходы.");
            }
        } catch (JacksonException e) {
            throw new SnapshotException(
                    paintRed("""
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
        }
        ArrayList<Wallet.WalletOperation> tempSaveIncome = user.getWallet().getIncomeWalletOperations();
        user.getWallet().getWalletOperations().clear();
        user.getWallet().getWalletOperations().addAll(tempSaveIncome);
        user.getWallet().getWalletOperations().addAll(readWalletExpensesOperations);
    }

    public static void importOnlyBudgetsSnapshot(User user, String snapshotContents) throws SnapshotException {
        HashMap<String, Double> readWalletBudgets;
        try {
            TypeReference<HashMap<String, Double>> tr = new TypeReference<HashMap<String, Double>>(){};
            readWalletBudgets = jsonObjectMapper.readValue(snapshotContents, tr);
        } catch (JacksonException e) {
            throw new SnapshotException(
                    paintRed("""
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
        }
        user.getWallet().getBudgetCategoriesAndLimits().clear();
        user.getWallet().getBudgetCategoriesAndLimits().putAll(readWalletBudgets);
    }
}
