package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileOutput implements Output {
    private final String filename;

    public FileOutput(String filename) {
        this.filename = filename;
    }

    public String getFileName() {
        return filename;
    }

    @Override
    public void print(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл: " + e.getMessage());
        }
    }
    public String getFilename() {
        return filename;
    }
}