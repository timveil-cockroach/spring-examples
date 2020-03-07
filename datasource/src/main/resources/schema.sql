CREATE TABLE IF NOT EXISTS datasource_users
(
    id                uuid PRIMARY KEY NOT NULL,
    first_name        varchar(50)      NOT NULL,
    last_name         varchar(50)      NOT NULL,
    email             varchar(50)      NOT NULL,
    address           varchar(50)      NOT NULL,
    city              varchar(50)      NOT NULL,
    state_code        varchar(2)       NOT NULL,
    zip_code          varchar(50)      NOT NULL,
    created_timestamp timestamp        NOT NULL,
    updated_timestamp timestamp        NULL
);