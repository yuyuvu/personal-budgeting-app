package com.github.yuyuvu.personalbudgetingapp.appservices;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.paintRed;

import com.github.yuyuvu.personalbudgetingapp.exceptions.SnapshotException;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Класс SnapshotsService хранит методы для сериализации всего кошелька или отдельных его частей
 * (списков доходов, расходов или хэш-таблицы лимитов по категориям) в снимки состояния в формате
 * json, а также для десериализации обратно: в Wallet, ArrayList или HashMap. Валидирует форматы
 * импортируемых отчётов.
 */
public class SnapshotsService {
  private static final ObjectMapper jsonObjectMapper = new ObjectMapper();

  /**
   * Метод сериализует данные всего кошелька в строку в формате json, создавая содержимое для
   * частичного снимка состояния.
   */
  public static String makeTotalExportContents(Wallet wallet) throws SnapshotException {
    String result;
    try (StringWriter resultWriter = new StringWriter()) {
      jsonObjectMapper.writeValue(resultWriter, wallet);
      result = resultWriter.toString();
    } catch (IOException | JacksonException e) {
      throw new SnapshotException(
          "Проблемы с формированием содержимого снапшота: " + e.getMessage());
    }
    return result;
  }

  /**
   * Метод сериализует только операции дохода из кошелька в строку в формате json, создавая
   * содержимое для частичного снимка состояния.
   */
  public static String makeOnlyIncomeExportContents(Wallet wallet) throws SnapshotException {
    String result;
    try (StringWriter resultWriter = new StringWriter()) {
      jsonObjectMapper.writeValue(resultWriter, wallet.getIncomeWalletOperations());
      result = resultWriter.toString();
    } catch (IOException | JacksonException e) {
      throw new SnapshotException(
          "Проблемы с формированием содержимого снапшота: " + e.getMessage());
    }
    return result;
  }

  /**
   * Метод сериализует только операции расхода из кошелька в строку в формате json, создавая
   * содержимое для частичного снимка состояния.
   */
  public static String makeOnlyExpensesExportContents(Wallet wallet) throws SnapshotException {
    String result;
    try (StringWriter resultWriter = new StringWriter()) {
      jsonObjectMapper.writeValue(resultWriter, wallet.getExpensesWalletOperations());
      result = resultWriter.toString();
    } catch (IOException | JacksonException e) {
      throw new SnapshotException(
          "Проблемы с формированием содержимого снапшота: " + e.getMessage());
    }
    return result;
  }

  /**
   * Метод сериализует только бюджеты по категориям расхода из кошелька в строку в формате json,
   * создавая содержимое для снимка состояния.
   */
  public static String makeOnlyBudgetsExportContents(Wallet wallet) throws SnapshotException {
    String result;
    try (StringWriter resultWriter = new StringWriter()) {
      jsonObjectMapper.writeValue(resultWriter, wallet.getBudgetCategoriesAndLimits());
      result = resultWriter.toString();
    } catch (IOException | JacksonException e) {
      throw new SnapshotException(
          "Проблемы с формированием содержимого снапшота: " + e.getMessage());
    }
    return result;
  }

  /**
   * Метод десериализует данные всего кошелька из строки в формате json, читая содержимое для
   * импорта снимка состояния. Далее текущий кошелёк пользователя заменяется прочитанным.
   */
  public static void importTotalSnapshot(User user, String snapshotContents)
      throws SnapshotException {
    Wallet readWalletData;
    try {
      readWalletData = jsonObjectMapper.readValue(snapshotContents, Wallet.class);
      // Проверка формата отчёта
      if (jsonObjectMapper.readTree(snapshotContents).get("balance").toString() == null) {
        throw new NullPointerException();
      }
    } catch (JacksonException | NullPointerException e) {
      throw new SnapshotException(
          paintRed(
              """
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
    }
    user.setWallet(readWalletData);
  }

  /**
   * Метод десериализует только операции дохода из строки в формате json, читая содержимое для
   * импорта частичного снимка состояния. Далее старые операции дохода в текущем кошельке заменяются
   * прочитанными.
   */
  public static void importOnlyIncomeSnapshot(User user, String snapshotContents)
      throws SnapshotException {
    ArrayList<Wallet.WalletOperation> readWalletIncomeOperations;
    try {
      TypeReference<ArrayList<Wallet.WalletOperation>> tr =
          new TypeReference<ArrayList<Wallet.WalletOperation>>() {};
      readWalletIncomeOperations = jsonObjectMapper.readValue(snapshotContents, tr);
      if (readWalletIncomeOperations.stream().anyMatch(wo -> !wo.isIncome())) {
        throw new SnapshotException("Не тот формат снимка состояния. Предоставлены расходы.");
      }
    } catch (JacksonException | SnapshotException e) {
      throw new SnapshotException(
          paintRed(
              """
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
    }
    ArrayList<Wallet.WalletOperation> tempSaveExpenses =
        user.getWallet().getExpensesWalletOperations();
    user.getWallet().getWalletOperations().clear();
    user.getWallet().getWalletOperations().addAll(readWalletIncomeOperations);
    user.getWallet().getWalletOperations().addAll(tempSaveExpenses);
  }

  /**
   * Метод десериализует только операции расхода из строки в формате json, читая содержимое для
   * импорта частичного снимка состояния. Далее старые операции расхода в текущем кошельке
   * заменяются прочитанными.
   */
  public static void importOnlyExpensesSnapshot(User user, String snapshotContents)
      throws SnapshotException {
    ArrayList<Wallet.WalletOperation> readWalletExpensesOperations;
    try {
      TypeReference<ArrayList<Wallet.WalletOperation>> tr =
          new TypeReference<ArrayList<Wallet.WalletOperation>>() {};
      readWalletExpensesOperations = jsonObjectMapper.readValue(snapshotContents, tr);
      if (readWalletExpensesOperations.stream().anyMatch(Wallet.WalletOperation::isIncome)) {
        throw new SnapshotException("Не тот формат снимка состояния. Предоставлены доходы.");
      }
    } catch (JacksonException e) {
      throw new SnapshotException(
          paintRed(
              """
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
    }
    ArrayList<Wallet.WalletOperation> tempSaveIncome = user.getWallet().getIncomeWalletOperations();
    user.getWallet().getWalletOperations().clear();
    user.getWallet().getWalletOperations().addAll(tempSaveIncome);
    user.getWallet().getWalletOperations().addAll(readWalletExpensesOperations);
  }

  /**
   * Метод десериализует только бюджеты по расходам из строки в формате json, читая содержимое для
   * импорта частичного снимка состояния. Далее имеющиеся бюджеты по расходам в текущем кошельке
   * заменяются прочитанными.
   */
  public static void importOnlyBudgetsSnapshot(User user, String snapshotContents)
      throws SnapshotException {
    HashMap<String, Double> readWalletBudgets;
    try {
      TypeReference<HashMap<String, Double>> tr = new TypeReference<HashMap<String, Double>>() {};
      readWalletBudgets = jsonObjectMapper.readValue(snapshotContents, tr);
    } catch (JacksonException e) {
      throw new SnapshotException(
          paintRed(
              """
                    Проблемы с загрузкой содержимого снимка состояния.
                    Файл содержит некорректный формат снимка состояния.
                    Вы должны загружать снимок только соответствующего типа (весь кошелёк, доходы, расходы или бюджеты). Нельзя загружать другой тип."""));
    }
    user.getWallet().getBudgetCategoriesAndLimits().clear();
    user.getWallet().getBudgetCategoriesAndLimits().putAll(readWalletBudgets);
  }
}
