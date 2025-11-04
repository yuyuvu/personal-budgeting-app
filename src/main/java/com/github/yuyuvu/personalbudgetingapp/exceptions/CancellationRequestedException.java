package com.github.yuyuvu.personalbudgetingapp.exceptions;

/**
 * Исключение, которое выбрасывается при запросе любой служебной команды приложения (через --) и
 * позволяет при любом вводе пользователя вернуться в предыдущее меню.
 */
public class CancellationRequestedException extends Exception {
  /** Конструктор со стандартным сообщением. */
  public CancellationRequestedException() {
    super("Запрошена отмена действия или служебная команда. Возврат в меню...");
  }
}
