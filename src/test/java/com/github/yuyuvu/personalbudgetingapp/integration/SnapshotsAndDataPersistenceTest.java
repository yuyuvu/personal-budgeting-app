package com.github.yuyuvu.personalbudgetingapp.integration;

import com.github.yuyuvu.personalbudgetingapp.appservices.SnapshotsService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.BudgetingService;
import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.exceptions.SnapshotException;
import com.github.yuyuvu.personalbudgetingapp.infrastructure.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Проверяем методы SnapshotsService и DataPersistenceService. */
public class SnapshotsAndDataPersistenceTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк и нужную локаль перед каждым тестом. */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
    Locale.setDefault(Locale.US);
  }

  /** Проверяем экспорт и импорт снапшотов на основе данных кошелька. */
  @Test
  void exportAndThenImportSnapshotTest() {

    WalletOperationsService.addExpense(wallet, 1000, "расход", LocalDateTime.now());
    WalletOperationsService.addExpense(wallet, 1600, "расход2", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 500, "доход", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 600, "доход2", LocalDateTime.now());
    WalletOperationsService.addIncome(wallet, 600, "доход3", LocalDateTime.now());
    BudgetingService.addNewExpensesCategoryLimit(wallet, "расход", 1000);
    BudgetingService.addNewExpensesCategoryLimit(wallet, "траты", 1000);

    User newUserForTotalImport = new User("user", new String[] {"hash", "salt"}, new Properties());
    User newUserForIncomeImport =
        new User("user1", new String[] {"hash", "salt"}, new Properties());
    User newUserForExpensesImport =
        new User("user2", new String[] {"hash", "salt"}, new Properties());
    User newUserForBudgetsImport =
        new User("user3", new String[] {"hash", "salt"}, new Properties());
    try {
      String snap1 = SnapshotsService.makeTotalExportContents(wallet);
      String snap2 = SnapshotsService.makeOnlyIncomeExportContents(wallet);
      String snap3 = SnapshotsService.makeOnlyExpensesExportContents(wallet);
      String snap4 = SnapshotsService.makeOnlyBudgetsExportContents(wallet);

      DataPersistenceService.saveSnapshotToFile(snap1, "snap1");
      DataPersistenceService.saveSnapshotToFile(snap2, "snap2");
      DataPersistenceService.saveSnapshotToFile(snap3, "snap3");
      DataPersistenceService.saveSnapshotToFile(snap4, "snap4");

      Path pathToSnapshots = Path.of("personal_budgeting_appdata").resolve("userdata_snapshots");
      String snap1Contents =
          DataPersistenceService.loadSnapshotFromFile(
              pathToSnapshots.resolve("snap1.json").toString());
      String snap2Contents =
          DataPersistenceService.loadSnapshotFromFile(
              pathToSnapshots.resolve("snap2.json").toString());
      String snap3Contents =
          DataPersistenceService.loadSnapshotFromFile(
              pathToSnapshots.resolve("snap3.json").toString());
      String snap4Contents =
          DataPersistenceService.loadSnapshotFromFile(
              pathToSnapshots.resolve("snap4.json").toString());

      SnapshotsService.importTotalSnapshot(newUserForTotalImport, snap1Contents);
      SnapshotsService.importOnlyIncomeSnapshot(newUserForIncomeImport, snap2Contents);
      SnapshotsService.importOnlyExpensesSnapshot(newUserForExpensesImport, snap3Contents);
      SnapshotsService.importOnlyBudgetsSnapshot(newUserForBudgetsImport, snap4Contents);

      // Нельзя загружать другой тип
      Assertions.assertThrows(
          SnapshotException.class,
          () -> {
            SnapshotsService.importTotalSnapshot(newUserForTotalImport, snap4Contents);
          });
      // Нельзя загружать другой тип
      Assertions.assertThrows(
          SnapshotException.class,
          () -> {
            SnapshotsService.importOnlyBudgetsSnapshot(newUserForBudgetsImport, snap1Contents);
          });
      // Нельзя загружать другой тип
      Assertions.assertThrows(
          SnapshotException.class,
          () -> {
            SnapshotsService.importOnlyIncomeSnapshot(newUserForIncomeImport, snap3Contents);
          });

      // Проверки, что в снапшоты попали нужные данные и загрузились так же
      Assertions.assertEquals(5, newUserForTotalImport.getWallet().getWalletOperations().size());
      Assertions.assertEquals(
          2, newUserForTotalImport.getWallet().getBudgetCategoriesAndLimits().size());

      Assertions.assertEquals(3, newUserForIncomeImport.getWallet().getWalletOperations().size());
      Assertions.assertEquals(
          0, newUserForIncomeImport.getWallet().getExpensesWalletOperations().size());

      Assertions.assertEquals(2, newUserForExpensesImport.getWallet().getWalletOperations().size());
      Assertions.assertEquals(
          0, newUserForExpensesImport.getWallet().getIncomeWalletOperations().size());

      Assertions.assertEquals(0, newUserForBudgetsImport.getWallet().getWalletOperations().size());
      Assertions.assertEquals(
          2, newUserForBudgetsImport.getWallet().getBudgetCategoriesAndLimits().size());

      Files.delete(pathToSnapshots.resolve("snap1.json"));
      Files.delete(pathToSnapshots.resolve("snap2.json"));
      Files.delete(pathToSnapshots.resolve("snap3.json"));
      Files.delete(pathToSnapshots.resolve("snap4.json"));

    } catch (SnapshotException | IOException | CheckedIllegalArgumentException e) {
      throw new RuntimeException(e);
    }
  }
}
