package com.github.yuyuvu.personalbudgetingapp.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class User {
    private String username;
    private String password;
    private Wallet wallet;

    // Данный конструктор используется только библиотекой Jackson для десериализации
    public User() {}

    // Данный конструктор используется при создании нового пользователя в AuthorizationService
    public User (String username, String password) {
        this.username = username;
        this.password = password;
        this.wallet = new Wallet();
    }

    public String getUsername() {
        return username;
    }

    public Wallet getWallet() {
        return wallet;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", wallet=" + wallet +
                '}';
    }
}
