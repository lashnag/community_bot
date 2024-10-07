create table event (
    event_id bigserial primary key,
    community_id bigint not null,
    event_description varchar not null,
    poll_id varchar default null,
    poll_confirmation_id varchar default null,
    event_date timestamp not null,
    poll_date timestamp not null,
    poll_confirmation_date timestamp not null,
    notification_date timestamp not null,
    notification_was_sent bool default false,
    foreign key (community_id) references community (community_id)
);
