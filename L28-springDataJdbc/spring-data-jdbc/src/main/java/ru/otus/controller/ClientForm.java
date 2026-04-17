package ru.otus.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientForm {
    private String name;
    private String street;
    private String phones;
}
