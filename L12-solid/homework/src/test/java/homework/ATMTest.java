package homework;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.atm.ATM;
import ru.atm.Banknote;

class ATMTest {

    private final Banknote b100 = new Banknote(100);
    private final Banknote b500 = new Banknote(500);
    private final Banknote b1000 = new Banknote(1000);

    @Test
    void shouldWithdrawExactAmount() {

        ATM atm = new ATM(List.of(b100, b500, b1000));

        atm.deposit(Map.of(
                b1000, 2,
                b500, 2,
                b100, 5));

        Map<Banknote, Integer> result = atm.withdrawal(2600);

        assertEquals(2, result.get(b1000));
        assertEquals(1, result.get(b500));
        assertEquals(1, result.get(b100));

        assertEquals(0, atm.getSlots().get(b1000));
        assertEquals(1, atm.getSlots().get(b500));
        assertEquals(4, atm.getSlots().get(b100));
    }

    @Test
    void shouldThrowIfNotEnoughMoney() {

        ATM atm = new ATM(List.of(b100, b500));

        atm.deposit(Map.of(b500, 1));

        assertThrows(IllegalArgumentException.class, () -> atm.withdrawal(1000));
    }

    @Test
    void shouldThrowIfImpossibleCombination() {

        ATM atm = new ATM(List.of(b100, b500));

        atm.deposit(Map.of(b500, 1));

        assertThrows(IllegalArgumentException.class, () -> atm.withdrawal(400));
    }

    @Test
    void shouldNotAffectAnotherATMInstance() {

        ATM atm1 = new ATM(List.of(b100, b500));
        ATM atm2 = new ATM(List.of(b100, b500));

        atm1.deposit(Map.of(b500, 2));
        atm2.deposit(Map.of(b500, 5));

        atm1.withdrawal(500);

        assertEquals(1, atm1.getSlots().get(b500));
        assertEquals(5, atm2.getSlots().get(b500));
    }

    @Test
    void shouldThrowIfAmountIsNegativeOrZero() {

        ATM atm = new ATM(List.of(b100));

        assertThrows(IllegalArgumentException.class, () -> atm.withdrawal(0));

        assertThrows(IllegalArgumentException.class, () -> atm.withdrawal(-100));
    }

    @Test
    void shouldUseMinimalNumberOfBanknotes() {

        ATM atm = new ATM(List.of(b100, b500, b1000));

        atm.deposit(Map.of(
                b1000, 1,
                b500, 10,
                b100, 10));

        Map<Banknote, Integer> result = atm.withdrawal(1000);

        assertEquals(1, result.get(b1000));
        assertFalse(result.containsKey(b500));
    }
}
