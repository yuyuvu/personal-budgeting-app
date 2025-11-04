package com.github.yuyuvu.personalbudgetingapp.exceptions;

/**
 * Исключение, которое выбрасывается при проблемах с сериализацией или десериализацией снимков
 * состояния кошелька. Может быть связано с проблемами чтения / сохранения файлов или с неправильным
 * форматом снимка, который захотел загрузить пользователь.
 */
public class SnapshotException extends Exception {
  /** В конструктор обязательно должно передаваться сообщение, какая именно возникла проблема. */
  public SnapshotException(String message) {
    super(message);
  }
}
