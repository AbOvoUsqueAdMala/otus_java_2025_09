package ru.otus.crm.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.crm.model.Client;
import ru.otus.crm.repository.ClientRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class DbServiceClientImpl implements DBServiceClient {
    private final ClientRepository clientRepository;

    @Override
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClient(long id) {
        return clientRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return StreamSupport.stream(clientRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Client::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }
}
