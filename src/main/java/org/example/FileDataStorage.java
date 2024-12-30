package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class FileDataStorage implements DataStorage {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void saveWallet(String filename, Wallet wallet) throws IOException {
        File file = new File(filename);
        objectMapper.writeValue(file, wallet);
    }

    @Override
    public Wallet loadWallet(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists() && file.length() > 0) {
            return objectMapper.readValue(file, Wallet.class);
        } else {
            return new Wallet();
        }
    }
}