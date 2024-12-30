package org.example;

public class Transaction {
    private Category category;
    private double amount;
    private TransactionType type;

    public Transaction(Category category, double amount, TransactionType type) {
        this.category = category;
        this.amount = amount;
        this.type = type;
    }
    public enum TransactionType {
        INCOME,
        EXPENSE
    }

    public Category getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }
}
