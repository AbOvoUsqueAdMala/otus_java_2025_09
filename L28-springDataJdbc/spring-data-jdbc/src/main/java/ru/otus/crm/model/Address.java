package ru.otus.crm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "address")
public class Address {

    @Id
    @Column("id")
    private Long id;

    @Column("street")
    private String street;

    public Address(Long id, String street) {
        this.id = id;
        this.street = street;
    }
}
