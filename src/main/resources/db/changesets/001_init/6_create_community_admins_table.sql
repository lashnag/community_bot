create table community_admins (
    community_admins_id bigserial primary key,
    community_id bigint not null,
    user_id bigint not null,
    foreign key (community_id) references community (community_id),
    foreign key (user_id) references users (user_id),
    unique(community_id, user_id)
);
