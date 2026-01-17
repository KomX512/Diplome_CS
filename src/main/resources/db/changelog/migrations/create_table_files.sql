CREATE TABLE if not exists files
(
    id       bigserial primary key,
    filename varchar(255) unique,
    content     text,
    owner    varchar(255)
);