package org.example;

import java.io.IOException;

public interface DataStorage {
    void saveWallet(String filename, Wallet wallet) throws IOException;
    Wallet loadWallet(String filename) throws IOException;
}