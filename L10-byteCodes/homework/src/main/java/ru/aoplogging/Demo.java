package ru.aoplogging;

class Demo {

    public static void main(String[] args) {
        TestLoggingInterface myClass = Ioc.createMyClass();
        myClass.calculation(2);
        myClass.calculation(2, 10);
    }
}
