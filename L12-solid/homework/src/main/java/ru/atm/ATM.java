package ru.atm;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ATM {

    private final Map<Banknote, Integer> slots = new HashMap<>();

    public ATM(List<Banknote> banknotes) {
        banknotes.forEach(banknote -> slots.put(banknote, 0));
    }

    public Map<Banknote, Integer> withdrawal(Integer amount) {

        if (amount == null || amount <= 0) {
            throw new WithdrawalException("Сумма должна быть положительной");
        }

        int totalBalance = slots.entrySet().stream()
                .mapToInt(e -> e.getKey().denomination() * e.getValue())
                .sum();

        if (amount > totalBalance) {
            throw new WithdrawalException("Недостаточно средств в банкомате");
        }

        List<Banknote> sortedBanknotes = slots.keySet().stream()
                .sorted((b1, b2) -> b2.denomination().compareTo(b1.denomination()))
                .toList();

        Map<Banknote, Integer> dispensedBanknotes = new LinkedHashMap<>();
        int remaining = amount;

        for (Banknote banknote : sortedBanknotes) {

            int denomination = banknote.denomination();
            int availableCount = slots.get(banknote);

            int neededCount = remaining / denomination;
            int usedCount = Math.min(neededCount, availableCount);

            if (usedCount > 0) {
                dispensedBanknotes.put(banknote, usedCount);
                remaining -= usedCount * denomination;
            }
        }

        if (remaining != 0) {
            throw new WithdrawalException("Невозможно выдать запрошенную сумму доступными купюрами");
        }

        dispensedBanknotes.forEach((banknote, count) -> slots.put(banknote, slots.get(banknote) - count));

        return dispensedBanknotes;
    }

    public void deposit(Map<Banknote, Integer> banknotes) {

        Map<Banknote, Integer> rejected = new HashMap<>();

        banknotes.forEach((banknote, amount) -> {
            if (slots.containsKey(banknote)) {
                slots.put(banknote, slots.get(banknote) + amount);
            } else {
                rejected.put(banknote, amount);
            }
        });

        if (!rejected.isEmpty()) {
            log.info("Купюры не могут быть внесены и будут выданы обратно: {}", rejected);
        }
    }
}
