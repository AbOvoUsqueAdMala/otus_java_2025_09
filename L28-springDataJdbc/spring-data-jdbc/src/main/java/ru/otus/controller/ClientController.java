package ru.otus.controller;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.crm.model.Address;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Phone;
import ru.otus.crm.service.DBServiceClient;

@Controller
public class ClientController {
    private final DBServiceClient dbServiceClient;

    public ClientController(DBServiceClient dbServiceClient) {
        this.dbServiceClient = dbServiceClient;
    }

    @GetMapping({"/", "/clients"})
    public String clientsPage(Model model, @ModelAttribute("form") ClientForm form) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ClientForm());
        }
        model.addAttribute("clients", dbServiceClient.findAll());
        return "clients";
    }

    @PostMapping("/clients")
    public String saveClient(@ModelAttribute("form") ClientForm form, RedirectAttributes redirectAttributes) {
        try {
            dbServiceClient.saveClient(new Client(
                    null,
                    extractRequiredValue(form.getName()),
                    new Address(null, extractRequiredValue(form.getStreet())),
                    extractPhones(form.getPhones())));
            redirectAttributes.addFlashAttribute("successMessage", "Client has been saved");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
        }
        return "redirect:/clients";
    }

    private static String extractRequiredValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Fields name, street and phones are required");
        }
        return value.trim();
    }

    private static List<Phone> extractPhones(String rawPhones) {
        var phones = Arrays.stream(extractRequiredValue(rawPhones).split("[,\\r\\n]+"))
                .map(String::trim)
                .filter(phone -> !phone.isEmpty())
                .map(phone -> new Phone(null, phone))
                .toList();
        if (phones.isEmpty()) {
            throw new IllegalArgumentException("At least one phone is required");
        }
        return phones;
    }
}
