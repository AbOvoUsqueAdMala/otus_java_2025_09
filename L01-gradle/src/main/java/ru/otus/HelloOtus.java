package ru.otus;

import com.google.common.base.CharMatcher;
import java.util.Scanner;

@SuppressWarnings("java:S106")
public class HelloOtus {
    public static void main(String[] args) {

        System.out.print("Введите текст: ");

        var scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        var result = CharMatcher.inRange('1', '9')
                .retainFrom(input);
        System.out.println("Извлеченные цифры из строки: " + result);
    }
}
