package ru.otus.numbers.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LastServerValueHolderTest {

    @Test
    void shouldConsumeOnlyLatestValueOnce() {
        LastServerValueHolder valueHolder = new LastServerValueHolder();

        valueHolder.update(2);
        valueHolder.update(3);

        assertThat(valueHolder.consume()).isEqualTo(3);
        assertThat(valueHolder.consume()).isEqualTo(LastServerValueHolder.NO_VALUE);
    }
}
