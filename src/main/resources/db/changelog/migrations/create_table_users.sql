CREATE TABLE if not exists app_users
(
    id        bigserial primary key,
    login     varchar(255) unique not null,
    password  varchar(255)
);