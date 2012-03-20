# --- !Ups

CREATE OR REPLACE FUNCTION updated_at_column()
        RETURNS TRIGGER AS '
  BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
  END;
' LANGUAGE 'plpgsql';

create table users(
 id serial not null primary key ,
 name varchar(30) unique not null ,
 password char(50) not null ,
 timezone integer not null,
 picture_path varchar(1024),
 token char(50) unique not null,
 description varchar(1024),
 website varchar(1024),
 created_at timestamp not null DEFAULT current_timestamp,
 updated_at timestamp not null DEFAULT current_timestamp
);

create index username_index on users using hash(name);

CREATE TRIGGER users_update_updated_at_column BEFORE UPDATE
  ON users FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create table experiments(
  id serial not null primary key,
  name varchar(100) not null,
  picture_path varchar(1024),
  description varchar(1024),
  access_restriction varchar(30),
  user_id integer not null references users ON DELETE CASCADE,
  timezone integer not null,
  token char(50) not null,
  website varchar(1024),
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp
);

CREATE TRIGGER experiments_update_updated_at_column BEFORE UPDATE
  ON experiments FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create index experiment_user_id_index on experiments using hash(user_id);

create table nodes(
  id serial not null primary key,
  name varchar(100) not null,
  picture_path varchar(1024),
  description varchar(1024),
  latitude numeric,
  longitude numeric,
  altitude numeric,
  user_id Integer not null references users ON DELETE CASCADE,
  token char(50) not null,
  website varchar(1024),
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  experiment_id Integer not null references experiments ON DELETE CASCADE
);
CREATE TRIGGER nodes_update_updated_at_column BEFORE UPDATE
  ON nodes FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create index nodes_experiment_id_index on nodes using hash(experiment_id);

create table measurements (
  id serial not null primary key,
  name varchar(100) not null,
  description varchar(1024),
  website varchar(1024),
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp
);

CREATE TRIGGER measurements_update_updated_at_column BEFORE UPDATE
  ON measurements FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create table streams(
  id serial not null primary key,
  name varchar(100) not null,
  picture_path varchar(1024),
  description varchar(1024),
  measurement_id Integer not null references measurements ON DELETE RESTRICT ,
  node_id Integer not null references nodes ON DELETE CASCADE,
  user_id Integer not null references users ON DELETE CASCADE,
  token char(50) not null,
  website varchar(1024),
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp
);

CREATE TRIGGER streams_update_updated_at_column BEFORE UPDATE
  ON streams FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create index stream_node_id_index on streams using hash(node_id);

create table widgets(
  id serial not null primary key,
  name varchar(100) not null,
  description text,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  sample_config text not null,
  picture_path varchar(1024),
  website varchar(1024)
);

CREATE TRIGGER widgets_update_updated_at_column BEFORE UPDATE
  ON widgets FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create table users_widgets(
  user_id integer references users ON DELETE CASCADE,
  widget_id integer references widgets ON DELETE RESTRICT ,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  primary key(user_id,widget_id)
);

CREATE TRIGGER users_widgets_update_updated_at_column BEFORE UPDATE
  ON users_widgets FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create table analysis(
  id serial not null primary key,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  user_id integer not null references users ON DELETE CASCADE
);

CREATE TRIGGER analysis_update_updated_at_column BEFORE UPDATE
  ON users FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create index analysis_user_id_index on analysis using hash(user_id);

create table widget_instances(
  id serial not null primary key,
  title varchar(100) not null,
  widget_id Integer references widgets ON DELETE RESTRICT ,
  created_at timestamp not null DEFAULT current_timestamp ,
  updated_at timestamp not null DEFAULT current_timestamp ,
  config text not null,
  analysis_id integer not null references analysis ON DELETE CASCADE,
  placement_order integer
);

CREATE TRIGGER widget_instances_update_updated_at_column BEFORE UPDATE
  ON widget_instances FOR EACH ROW EXECUTE PROCEDURE
  updated_at_column();

create index widget_instances_analysis_id_index on widget_instances using hash(analysis_id);

# --- !Downs