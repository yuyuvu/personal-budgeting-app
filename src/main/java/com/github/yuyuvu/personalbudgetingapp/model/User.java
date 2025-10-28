package com.github.yuyuvu.personalbudgetingapp.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.yuyuvu.personalbudgetingapp.appservices.DataPersistenceService;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class User {
    String username;
    String password;
    Wallet wallet;

    public User () {
        this.wallet = DataPersistenceService.loadUserdataFromFile(username+".json").getWallet();
    }

    public User (String username, String password) {
        this.username = username;
        this.password = password;
        this.wallet = new Wallet();
    }

    public String getUsername() {
        return username;
    }
    
    @Override
    public String toString() {
        return username;
    }

    public Wallet getWallet() {
        return wallet;
    }
}
