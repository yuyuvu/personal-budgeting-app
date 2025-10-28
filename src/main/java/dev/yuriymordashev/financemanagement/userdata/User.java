package dev.yuriymordashev.financemanagement.userdata;

import dev.yuriymordashev.financemanagement.applogic.DataPersistenceSystem;

public class User {
    String username;
    String password;
    Wallet wallet;

    public User () {
        this.wallet = DataPersistenceSystem.loadWalletDataFromFile(username+".json");
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
}
