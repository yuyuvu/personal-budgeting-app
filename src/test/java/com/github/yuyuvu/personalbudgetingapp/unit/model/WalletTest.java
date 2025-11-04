package com.github.yuyuvu.personalbudgetingapp.unit.model;

import com.github.yuyuvu.personalbudgetingapp.model.Wallet;
import org.junit.jupiter.api.BeforeEach;

/** Класс для тестов методов из Wallet. */
public class WalletTest {
  Wallet wallet;

  /** Подготавливаем пустой кошелёк перед каждым тестом. */
  @BeforeEach
  void prepareNewWallet() {
    wallet = new Wallet(false);
  }
}
