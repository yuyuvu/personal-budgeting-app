package com.github.yuyuvu.personalbudgetingapp.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/** Класс User используется для более наглядного разделения логики работы авторизации и операций с кошельком.
 * <br>Также выделение пользователя отдельно от его кошелька позволяет более удобно работать с сохранением данных в файлы.
 * <br>В логике управления личными финансами используется только для получения экземпляра кошелька (getWallet).*/
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class User {
    private String username;
    private String password;
    private Wallet wallet;

    /** Данный конструктор должен использоваться только библиотекой Jackson для десериализации */
    private User() {}

    /** Данный конструктор используется при создании нового пользователя в AuthorizationService */
    public User (String username, String password) {
        this.username = username;
        this.password = password;
        this.wallet = new Wallet(false);
    }

    /** Метод для получения имени пользователя в виде строки.*/
    public String getUsername() {
        return username;
    }

    /** Метод для получения экземпляра кошелька, который привязан к данному пользователю.*/
    public Wallet getWallet() {
        return wallet;
    }

    /** Метод для смены экземпляра кошелька при загрузке снимка состояния из файла.*/
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    /** Используется для отладки. <br>Метод для перевода значений полей, хранимых в объекте пользователя, в строку.*/
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", wallet=" + wallet +
                '}';
    }
}
