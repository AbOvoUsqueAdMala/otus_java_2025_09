package homework;

import java.util.*;

public class CustomerService {

    private final NavigableMap<Customer, String> treeMap = new TreeMap<>(Comparator.comparingLong(Customer::getScores));

    // todo: 3. надо реализовать методы этого класса
    // важно подобрать подходящую Map-у, посмотрите на редко используемые методы, они тут полезны

    public Map.Entry<Customer, String> getSmallest() {
        return copyEntry(treeMap.firstEntry());
    }

    public Map.Entry<Customer, String> getNext(Customer customer) {
        return copyEntry(treeMap.higherEntry(customer));
    }

    public void add(Customer customer, String data) {
        treeMap.put(customer, data);
    }

    private Map.Entry<Customer, String> copyEntry(Map.Entry<Customer, String> entry) {
        if (entry == null) {
            return null;
        }
        Customer c = entry.getKey();
        Customer copy = new Customer(c.getId(), c.getName(), c.getScores());
        return new AbstractMap.SimpleEntry<>(copy, entry.getValue());
    }
}
