# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table game (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  date                          timestamp,
  constraint pk_game primary key (id)
);

create table player (
  id                            bigint auto_increment not null,
  game_id                       bigint not null,
  score                         varchar(1000),
  super_score                   varchar(1000),
  name                          varchar(255),
  valid                         boolean,
  winner                        boolean,
  constraint pk_player primary key (id)
);

alter table player add constraint fk_player_game_id foreign key (game_id) references game (id) on delete restrict on update restrict;
create index ix_player_game_id on player (game_id);


# --- !Downs

alter table player drop constraint if exists fk_player_game_id;
drop index if exists ix_player_game_id;

drop table if exists game;

drop table if exists player;

