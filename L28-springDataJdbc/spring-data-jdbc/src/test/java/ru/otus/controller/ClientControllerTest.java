package ru.otus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Phone;
import ru.otus.crm.repository.ClientRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void cleanUp() {
        clientRepository.deleteAll();
    }

    @Test
    void shouldRenderClientsPage() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Client")))
                .andExpect(content().string(containsString("Clients List")));
    }

    @Test
    void shouldSaveClient() throws Exception {
        mockMvc.perform(post("/clients")
                        .param("name", "Ivan Ivanov")
                        .param("street", "Lenina 10")
                        .param("phones", "13-555-22, 14-666-333"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        var clients = StreamSupport.stream(clientRepository.findAll().spliterator(), false)
                .toList();
        assertThat(clients).hasSize(1);

        Client client = clients.getFirst();
        assertThat(client.getName()).isEqualTo("Ivan Ivanov");
        assertThat(client.getAddress()).isNotNull();
        assertThat(client.getAddress().getStreet()).isEqualTo("Lenina 10");
        assertThat(client.getPhones()).extracting(Phone::getNumber).containsExactly("13-555-22", "14-666-333");
    }
}
