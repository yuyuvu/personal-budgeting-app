package com.github.yuyuvu.personalbudgetingapp.integration;

import com.github.yuyuvu.personalbudgetingapp.domainservices.WalletOperationsService;
import com.github.yuyuvu.personalbudgetingapp.infrastructure.DataPersistenceService;
import com.github.yuyuvu.personalbudgetingapp.model.User;
import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Класс для взаимодействия методов из WalletOperationsService и DataPersistenceService. Проходим
 * через весь цикл перевода средств.
 */
public class TransferMoneyTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк перед каждым тестом. */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
  }

  /**
   * Проверяем, что метод создаёт расход у одного пользователя и доход на аналогичную сумму у
   * другого пользователя. Метод должен работать в том числе после сохранений данных кошелька
   * другого пользователя в файловую систему и загрузок данных из неё.
   */
  @Test
  void transferMoneyToAnotherUserTest() {
    // Потенциальный текущий пользователь приложения.
    User sender = new User("username1", new String[] {"hash", "salt"}, new Properties());

    // Потенциальный уже имеющийся пользователь приложения, у которого есть файл кошелька.
    // Имитируем наличие файла кошелька получателя и после этого тестируем нужный метод.
    User recipient = new User("username2", new String[] {"hash", "salt"}, new Properties());

    // Никаких операций до теста нет
    Assertions.assertEquals(0, sender.getWallet().getWalletOperations().size());
    Assertions.assertEquals(0, recipient.getWallet().getWalletOperations().size());

    User recipientAfterTransfer = null;
    String pathWhereSaved;
    try {
      pathWhereSaved = DataPersistenceService.makeNewUserWalletFile(recipient.getUsername());
      DataPersistenceService.saveUserdataToFile(recipient);

      // Метод, который тестируется
      WalletOperationsService.transferMoneyToAnotherUser(sender, recipient.getUsername(), 15000);
      recipientAfterTransfer = DataPersistenceService.loadUserdataFromFile(recipient.getUsername());

      // Удаляем ненужный временный файл после теста
      Files.deleteIfExists(Path.of(pathWhereSaved));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }

    // Проверяем списание у текущего пользователя приложения.
    ArrayList<Wallet.WalletOperation> senderOperations =
        sender.getWallet().getExpensesWalletOperations();
    Assertions.assertEquals(1, senderOperations.size());
    Assertions.assertEquals(-15000, sender.getWallet().getBalance());
    Assertions.assertFalse(senderOperations.get(0).isIncome());

    // Проверяем получение перевода у другого пользователя приложения
    Assertions.assertNotNull(recipientAfterTransfer);
    ArrayList<Wallet.WalletOperation> recipientOperations =
        recipientAfterTransfer.getWallet().getIncomeWalletOperations();
    Assertions.assertEquals(1, recipientOperations.size());
    Assertions.assertEquals(15000, recipientAfterTransfer.getWallet().getBalance());
    Assertions.assertTrue(recipientOperations.get(0).isIncome());
  }
}
