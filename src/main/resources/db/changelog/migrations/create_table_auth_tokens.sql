CREATE TABLE if not exists roles
(
    id name primary key,
    token varchar(255) not null unique
);