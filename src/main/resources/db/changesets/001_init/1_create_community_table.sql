create table community (
    community_id bigserial primary key,
    chat_id varchar unique not null,
    capacity int not null,
    name varchar not null
);
