--drop table oauth_client_details;
--drop table oauth_access_token; 
--drop table oauth_refresh_token; 

-- Due to JdbcClientDetailsService's inflexibility, using names as defined by S2OAuth
-- TODO: Change table/column names once JdbcClientDetailsService and JdbcOAuth2ProviderTokenServices are changed to pull data by column position instead of by column name
-- TODO: Submit pull request for JdbcClientDetailsService and JdbcOAuth2ProviderTokenServices 
create table oauth_client_details (id serial,
				name varchar not null unique, 
				slug varchar not null unique,
				description varchar not null,
				organization varchar,
				website varchar,
				client_id varchar not null unique,
				client_secret varchar not null unique,
				web_server_redirect_uri varchar,
				resource_ids varchar,
				scope varchar,
				authorized_grant_types varchar,
				authorities varchar,
				primary key (id));

create table AppDeveloper (app bigint, 
							developer varchar, 
							primary key (app, developer),
							foreign key (app) references oauth_client_details(id) on delete cascade);
				
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

