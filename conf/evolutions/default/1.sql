# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table huit_game_table (
  id                            bigserial not null,
  name                          varchar(255),
  date                          timestamptz,
  owner_id                      bigint,
  constraint pk_huit_game_table primary key (id)
);

create table huit_player_table (
  id                            bigserial not null,
  game_id                       bigint not null,
  score                         bigint[],
  super_score                   bigint[],
  name                          varchar(255),
  valid                         boolean,
  winner                        boolean,
  distributor                   boolean,
  constraint pk_huit_player_table primary key (id)
);

create table huit_user_table (
  id                            bigserial not null,
  first_name                    varchar(255),
  last_name                     varchar(255),
  email                         varchar(255) not null,
  password                      varchar(255),
  avatar_url                    varchar(255),
  auth_token                    varchar(255),
  constraint uq_huit_user_table_email unique (email),
  constraint pk_huit_user_table primary key (id)
);

alter table huit_game_table add constraint fk_huit_game_table_owner_id foreign key (owner_id) references huit_user_table (id) on delete restrict on update restrict;
create index ix_huit_game_table_owner_id on huit_game_table (owner_id);

alter table huit_player_table add constraint fk_huit_player_table_game_id foreign key (game_id) references huit_game_table (id) on delete restrict on update restrict;
create index ix_huit_player_table_game_id on huit_player_table (game_id);


# --- !Downs

alter table if exists huit_game_table drop constraint if exists fk_huit_game_table_owner_id;
drop index if exists ix_huit_game_table_owner_id;

alter table if exists huit_player_table drop constraint if exists fk_huit_player_table_game_id;
drop index if exists ix_huit_player_table_game_id;

drop table if exists huit_game_table cascade;

drop table if exists huit_player_table cascade;

drop table if exists huit_user_table cascade;

