-- noinspection SqlNoDataSourceInspectionForFile

create table search_statistics
(
    id         serial primary key,
    title      text      not null,
    ip         text      not null,
    location   text,
    created_at timestamp not null default now()
)