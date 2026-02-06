package ru.aoplogging;

class Demo {

    public static void main(String[] args) {
        TestLoggingInterface testLogging = Ioc.createClass(TestLogging.class);
        testLogging.calculation(2);
        testLogging.calculation(2, 10);
        testLogging.calculation(5);
    }
}
