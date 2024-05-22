create schema if not exists user_management;

create table user_management.user
(
    id       serial primary key,
    username varchar not null check ( length(trim(username)) > 0 ) unique,
    password varchar
);

create table user_management.authority
(
    id             serial primary key,
    authority varchar not null check ( length(trim(authority.authority)) > 0 ) unique
);

create table user_management.user_authority
(
    id_user      int not null references user_management.user (id),
    id_authority int not null references user_management.authority (id),
    primary key (id_user, id_authority)
);