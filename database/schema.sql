-- noinspection SqlNoDataSourceInspectionForFile

create schema if not exists alexandria;

create table alexandria.search_statistics
(
    id         serial primary key,
    title      text      not null,
    ip         text      not null,
    location   text,
    created_at timestamp not null default now()
)
