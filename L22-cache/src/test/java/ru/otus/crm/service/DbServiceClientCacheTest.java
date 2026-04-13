package ru.otus.crm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.cachehw.MyCache;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.DataTemplateHibernate;
import ru.otus.core.sessionmanager.TransactionManagerHibernate;
import ru.otus.crm.model.Address;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Phone;

class DbServiceClientCacheTest {
    private StandardServiceRegistry registry;
    private SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        properties.setProperty("hibernate.connection.url", "jdbc:h2:mem:test-cache;DB_CLOSE_DELAY=-1");
        properties.setProperty("hibernate.connection.username", "sa");
        properties.setProperty("hibernate.connection.password", "");
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");

        registry =
                new StandardServiceRegistryBuilder().applySettings(properties).build();
        sessionFactory = new MetadataSources(registry)
                .addAnnotatedClass(Client.class)
                .addAnnotatedClass(Address.class)
                .addAnnotatedClass(Phone.class)
                .buildMetadata()
                .buildSessionFactory();
    }

    @AfterEach
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    @DisplayName("Должен использовать кэш и не выполнять повторное чтение из базы данных")
    void shouldUseCacheAndAvoidSecondDatabaseRead() {
        var template = new DelayedClientDataTemplate(new DataTemplateHibernate<>(Client.class), 120);
        var warmUpService =
                new DbServiceClientImpl(new TransactionManagerHibernate(sessionFactory), template, new MyCache<>());
        var savedClient = warmUpService.saveClient(
                new Client(null, "cache-me", new Address(null, "Lenina"), List.of(new Phone(null, "111-11"))));
        var service =
                new DbServiceClientImpl(new TransactionManagerHibernate(sessionFactory), template, new MyCache<>());

        long firstReadNanos =
                measure(() -> assertThat(service.getClient(savedClient.getId())).isPresent());
        long secondReadNanos =
                measure(() -> assertThat(service.getClient(savedClient.getId())).isPresent());

        assertThat(template.findByIdCalls()).isEqualTo(1);
        assertThat(secondReadNanos).isLessThan(firstReadNanos);
        assertThat(Duration.ofNanos(firstReadNanos).toMillis()).isGreaterThanOrEqualTo(100);
        assertThat(Duration.ofNanos(secondReadNanos).toMillis()).isLessThan(100);
    }

    @Test
    @DisplayName("Должен возвращать из кэша независимую копию объекта")
    void shouldReturnClonedObjectFromCache() {
        DataTemplate<Client> template = new DataTemplateHibernate<>(Client.class);
        var service =
                new DbServiceClientImpl(new TransactionManagerHibernate(sessionFactory), template, new MyCache<>());
        var savedClient = service.saveClient(
                new Client(null, "initial", new Address(null, "Pushkina"), List.of(new Phone(null, "222-22"))));

        var firstRead = service.getClient(savedClient.getId()).orElseThrow();
        firstRead.setName("changed-outside");
        var secondRead = service.getClient(savedClient.getId()).orElseThrow();

        assertThat(secondRead.getName()).isEqualTo("initial");
    }

    private long measure(Runnable action) {
        long startedAt = System.nanoTime();
        action.run();
        return System.nanoTime() - startedAt;
    }

    private static class DelayedClientDataTemplate implements DataTemplate<Client> {
        private final DataTemplate<Client> delegate;
        private final long delayMillis;
        private final AtomicInteger findByIdCalls = new AtomicInteger();

        private DelayedClientDataTemplate(DataTemplate<Client> delegate, long delayMillis) {
            this.delegate = delegate;
            this.delayMillis = delayMillis;
        }

        @Override
        public Optional<Client> findById(Session session, long id) {
            findByIdCalls.incrementAndGet();
            sleep();
            return delegate.findById(session, id);
        }

        @Override
        public List<Client> findByEntityField(Session session, String entityFieldName, Object entityFieldValue) {
            return delegate.findByEntityField(session, entityFieldName, entityFieldValue);
        }

        @Override
        public List<Client> findAll(Session session) {
            return delegate.findAll(session);
        }

        @Override
        public Client insert(Session session, Client object) {
            return delegate.insert(session, object);
        }

        @Override
        public Client update(Session session, Client object) {
            return delegate.update(session, object);
        }

        private int findByIdCalls() {
            return findByIdCalls.get();
        }

        private void sleep() {
            try {
                TimeUnit.MILLISECONDS.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }
}
