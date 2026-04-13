create sequence client_seq start with 1 increment by 1;
create sequence address_seq start with 1 increment by 1;
create sequence phone_seq start with 1 increment by 1;

create table client
(
    id bigint not null primary key,
    name varchar(50)
);

create table address
(
    id bigint not null primary key,
    street varchar(255) not null,
    client_id bigint not null unique,
    constraint fk_address_client foreign key (client_id) references client (id)
);

create table phone
(
    id bigint not null primary key,
    number varchar(30) not null,
    client_id bigint not null,
    constraint fk_phone_client foreign key (client_id) references client (id)
);
