package ru.otus.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlternatingNumberPrinterTest {

    @Test
    @DisplayName("Потоки стартуют с первого, строго чередуются и печатают одинаковую последовательность")
    void shouldStartFromFirstThreadAndAlternateWithBouncingNumbers() throws InterruptedException {
        var printer = new AlternatingNumberPrinter();
        int printsPerThread = 25;
        var expectedValues = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6, 7);

        var events = printer.runAndCollect(printsPerThread);

        assertThat(events).hasSize(printsPerThread * 2);
        assertThat(events.getFirst().threadNumber()).isEqualTo(1);

        for (int index = 0; index < events.size(); index++) {
            var event = events.get(index);
            int expectedThreadNumber = index % 2 == 0 ? 1 : 2;
            int expectedValue = expectedValues.get(index / 2);

            assertThat(event.threadNumber()).isEqualTo(expectedThreadNumber);
            assertThat(event.value()).isEqualTo(expectedValue);
            assertThat(event.indexInThread()).isEqualTo(index / 2);
        }

        assertThat(events.stream()
                        .filter(event -> event.threadNumber() == 1)
                        .map(AlternatingNumberPrinter.PrintEvent::value)
                        .toList())
                .containsExactlyElementsOf(expectedValues);

        assertThat(events.stream()
                        .filter(event -> event.threadNumber() == 2)
                        .map(AlternatingNumberPrinter.PrintEvent::value)
                        .toList())
                .containsExactlyElementsOf(expectedValues);
    }
}
