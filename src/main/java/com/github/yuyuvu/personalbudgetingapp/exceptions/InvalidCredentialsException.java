package com.github.yuyuvu.personalbudgetingapp.exceptions;

/**
 * Исключение, которое используется при валидации данных для аутентификации и выбрасывается при
 * вводе пользователем некорректного значения (неправильный или уже занятый логин, неверный пароль).
 * Приводит к новой итерации цикла и повторному запросу ввода корректных данных.
 */
public class InvalidCredentialsException extends Exception {
  /** В конструктор обязательно должно передаваться сообщение, что именно было введено не так. */
  public InvalidCredentialsException(String message) {
    super(message);
  }
}
