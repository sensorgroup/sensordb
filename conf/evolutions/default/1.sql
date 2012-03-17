# --- !Ups

create table users(
 pk serial not null primary key ,
 username varchar(30) unique not null ,
 password char(50) not null ,
 token char(50) not null,
 picture_path varchar(1024),
 description varchar(1024),
 website varchar(1024),
 created_at timestamp not null DEFAULT current_timestamp,
 update_at timestamp not null DEFAULT current_timestamp ,
 url varchar(1024)
);

create index username_index on users using hash(username);

create table experiments(
  pk serial not null primary key,
  name varchar(100) not null,
  picture_path varchar(1024),
  description varchar(1024),
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  token char(50) not null,
  website varchar(1024),
  user_id integer not null references users
);

create index experiment_user_id_index on experiments using hash(user_id);

create table nodes(
  pk serial not null primary key,
  name varchar(100) not null,
  picture_path varchar(1024),
  description varchar(1024),
  latitude numeric,
  longitude numeric,
  altitude numeric,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  token char(50) not null,
  website varchar(1024),
  experiment_id Integer not null references experiments
);

create index nodes_experiment_id_index on nodes using hash(experiment_id);

create table measurements (
  pk serial not null primary key,
  name varchar(100) not null,
  description varchar(1024),
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  website varchar(1024)
);

create table streams(
  pk serial not null primary key,
  name varchar(100) not null,
  picture_path varchar(1024),
  description varchar(1024),
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  token char(50) not null,
  measurement_id Integer not null references measurements,
  website varchar(1024),
  node_id Integer not null references nodes
);

create index stream_node_id_index on streams using hash(node_id);

create table widgets(
  pk serial not null primary key,
  name varchar(100) not null,
  description text,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  sample_config text not null,
  picture_path varchar(1024),
  website varchar(1024)
);

create table users_widgets(
  user_id integer references users,
  widget_id integer references widgets,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  primary key(user_id,widget_id)
);

create table analysis(
  pk serial not null primary key,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  user_id integer not null references users
);

create index analysis_user_id_index on analysis using hash(user_id);

create table widget_instances(
  pk serial not null primary key,
  title varchar(100) not null,
  widget_id Integer references widgets,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  config text not null,
  analysis_id integer not null references analysis,
  placement_order integer
);

create index widget_instances_analysis_id_index on widget_instances using hash(analysis_id);

# --- !Downs