package dev.yuriymordashev.financemanagement.userdata;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Wallet {
    private ArrayList<WalletOperation> WalletOperations;

    public Wallet() {
        WalletOperations = new ArrayList<WalletOperation>();
    }

    class WalletOperation {
        boolean isIncome;
        String category;
        double amount;
        LocalDateTime dateTime;
    }

    public ArrayList<WalletOperation>  getWalletOperations() {
        return WalletOperations;
    }
}
