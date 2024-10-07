create table reserved_participant (
    reserved_participant_id bigserial primary key,
    event_id bigint not null,
    user_id bigint not null,
    foreign key (event_id) references event (event_id),
    foreign key (user_id) references users (user_id),
    unique(event_id, user_id)
);
