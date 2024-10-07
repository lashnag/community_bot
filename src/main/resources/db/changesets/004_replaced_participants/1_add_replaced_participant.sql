create table replaced_participant (
    replaced_participant_id bigserial primary key,
    event_id bigint not null,
    user_id bigint not null,
    replace_user_id bigint default null,
    foreign key (event_id) references event (event_id),
    foreign key (user_id) references users (user_id),
    foreign key (replace_user_id) references users (user_id),
    unique(event_id, user_id)
);
