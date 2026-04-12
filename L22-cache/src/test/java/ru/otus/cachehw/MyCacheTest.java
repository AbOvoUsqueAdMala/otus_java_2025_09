package ru.otus.cachehw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MyCacheTest {

    @Test
    @DisplayName("Должен уведомлять слушателей и сохранять значение при операциях put, get и remove")
    void shouldNotifyListenersAndStoreValue() {
        var cache = new MyCache<String, Integer>();
        List<String> events = new ArrayList<>();
        HwListener<String, Integer> listener =
                (key, value, action) -> events.add("%s:%s:%s".formatted(action, key, value));

        cache.addListener(listener);
        cache.put("k1", 10);
        var value = cache.get("k1");
        cache.remove("k1");

        assertThat(value).isEqualTo(10);
        assertThat(events).containsExactly("put:k1:10", "get:k1:10", "remove:k1:10");
    }

    @Test
    @DisplayName("Должен удалить запись из кэша после сборки мусора, если на ключ больше нет сильной ссылки")
    void shouldDropEntryAfterGcWhenKeyHasNoStrongReference() {
        var cache = new MyCache<Object, byte[]>();
        var key = new Object();
        cache.put(key, new byte[512 * 1024]);

        var weakKeyRef = new WeakReference<>(key);
        key = null;

        waitUntilCleared(weakKeyRef);

        assertThat(weakKeyRef.get()).isNull();
        assertThat(cache.get(new Object())).isNull();
    }

    private void waitUntilCleared(WeakReference<?> weakReference) {
        assertThatCode(() -> {
                    List<byte[]> memoryPressure = new ArrayList<>();
                    var deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
                    while (weakReference.get() != null && System.nanoTime() < deadline) {
                        try {
                            memoryPressure.add(new byte[256 * 1024]);
                        } catch (OutOfMemoryError ignored) {
                            memoryPressure.clear();
                        }
                        System.gc();
                        Thread.sleep(50);
                    }
                })
                .doesNotThrowAnyException();
    }
}
