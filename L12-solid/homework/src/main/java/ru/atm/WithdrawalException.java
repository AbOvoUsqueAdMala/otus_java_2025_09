package ru.atm;

public class WithdrawalException extends RuntimeException {
    public WithdrawalException(String message) {
        super(message);
    }
}
