package com.github.yuyuvu.personalbudgetingapp.exceptions;

/**
 * Исключение, которое используется при валидации переданных в функции параметров и выбрасывается
 * при вводе пользователем некорректного значения на текущем шаге работы приложения. Практически
 * всегда приводит к новой итерации цикла и повторному запросу ввода корректных данных.
 */
public class CheckedIllegalArgumentException extends Exception {
  /** В конструктор обязательно должно передаваться сообщение, что именно было введено не так. */
  public CheckedIllegalArgumentException(String message) {
    super(message);
  }
}
