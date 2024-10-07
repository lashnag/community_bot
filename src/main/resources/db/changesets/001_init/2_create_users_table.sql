create table users (
    user_id bigserial primary key,
    telegram_id bigint unique not null,
    telegram_username varchar unique not null,
    is_superuser boolean default false not null
);
