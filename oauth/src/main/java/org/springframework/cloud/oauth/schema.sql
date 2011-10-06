drop table oauth_access_token; 
drop table oauth_refresh_token; 

create table oauth_access_token (
  token_id VARCHAR(256),
  token BYTEA,
  authentication BYTEA,
  refresh_token VARCHAR(256)
);

create table oauth_refresh_token (
  token_id VARCHAR(256),
  token BYTEA,
  authentication BYTEA
);

