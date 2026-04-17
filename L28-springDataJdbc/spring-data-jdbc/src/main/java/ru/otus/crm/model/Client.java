package ru.otus.crm.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "client")
public class Client {

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @MappedCollection(idColumn = "client_id")
    private Address address;

    @MappedCollection(idColumn = "client_id", keyColumn = "phone_order")
    private List<Phone> phones = new ArrayList<>();

    public Client(String name) {
        this(null, name, null, List.of());
    }

    public Client(Long id, String name) {
        this(id, name, null, List.of());
    }

    public Client(Long id, String name, Address address, List<Phone> phones) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phones = phones == null ? new ArrayList<>() : new ArrayList<>(phones);
    }

    @Override
    public String toString() {
        return "Client{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
