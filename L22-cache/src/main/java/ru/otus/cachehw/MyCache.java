package ru.otus.cachehw;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class MyCache<K, V> implements HwCache<K, V> {

    private final WeakHashMap<K, V> values = new WeakHashMap<>();
    private final List<HwListener<K, V>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public synchronized void put(K key, V value) {
        values.put(key, value);
        notifyListeners(key, value, "put");
    }

    @Override
    public synchronized void remove(K key) {
        var removedValue = values.remove(key);
        notifyListeners(key, removedValue, "remove");
    }

    @Override
    public synchronized V get(K key) {
        var value = values.get(key);
        notifyListeners(key, value, "get");
        return value;
    }

    @Override
    public void addListener(HwListener<K, V> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(HwListener<K, V> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(K key, V value, String action) {
        listeners.forEach(listener -> {
            try {
                listener.notify(key, value, action);
            } catch (Exception ex) {
                log.warn("При оповещении слушателя возникла ошибка: {}", ex.getMessage());
            }
        });
    }
}
