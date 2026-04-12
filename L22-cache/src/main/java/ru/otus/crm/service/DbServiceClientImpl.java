package ru.otus.crm.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.cachehw.HwCache;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.sessionmanager.TransactionManager;
import ru.otus.crm.model.Client;

@Slf4j
@RequiredArgsConstructor
public class DbServiceClientImpl implements DBServiceClient {

    private final TransactionManager transactionManager;
    private final DataTemplate<Client> clientDataTemplate;
    private final HwCache<Long, Client> cache;

    @Override
    public Client saveClient(Client client) {
        return transactionManager.doInTransaction(session -> {
            var clientCloned = client.clone();
            var savedClient = client.getId() == null
                    ? clientDataTemplate.insert(session, clientCloned)
                    : clientDataTemplate.update(session, clientCloned);
            cache.put(savedClient.getId(), savedClient.clone());
            log.info("{} client: {}", client.getId() == null ? "created" : "updated", savedClient);
            return savedClient.clone();
        });
    }

    @Override
    public Optional<Client> getClient(long id) {
        var clientFromCache = cache.get(id);
        if (clientFromCache != null) {
            log.info("client from cache: {}", clientFromCache);
            return Optional.of(clientFromCache.clone());
        }

        return transactionManager.doInReadOnlyTransaction(session -> {
            var clientOptional = clientDataTemplate.findById(session, id).map(Client::clone);
            clientOptional.ifPresent(client -> cache.put(client.getId(), client.clone()));
            log.info("client from db: {}", clientOptional);
            return clientOptional;
        });
    }

    @Override
    public List<Client> findAll() {
        return transactionManager.doInReadOnlyTransaction(session -> {
            var clientList = clientDataTemplate.findAll(session).stream()
                    .map(Client::clone)
                    .toList();
            clientList.forEach(client -> cache.put(client.getId(), client.clone()));
            log.info("clientList:{}", clientList);
            return clientList;
        });
    }
}
