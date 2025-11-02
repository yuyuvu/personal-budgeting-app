package com.github.yuyuvu.personalbudgetingapp.exceptions;

public class CancellationRequestedException extends Exception {
  public CancellationRequestedException() {
    super("Запрошена отмена действия или служебная команда. Возврат в меню...");
  }
}
