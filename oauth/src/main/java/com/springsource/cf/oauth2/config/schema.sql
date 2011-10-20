--drop table App
--drop table AppDeveloper
--drop table oauth_access_token; 
--drop table oauth_refresh_token; 

create table App (id serial,
				name varchar not null unique, 
				slug varchar not null unique,
				description varchar not null,
				organization varchar,
				website varchar,
				apiKey varchar not null unique,
				secret varchar not null unique,
				redirectUrl varchar,
				resourceIds varchar,
				scope varchar,
				grantTypes varchar,
				authorities varchar,
				primary key (id));

create table AppDeveloper (app bigint, 
				developer varchar, 
				primary key (app, developer),
				foreign key (app) references App(id) on delete cascade);
			
-- Changing the names of the oauth_access_token and oauth_refresh_token tables would involve overriding 9 different queries in JdbcOAuth2ProviderTokenServices.
-- Will leave them as-is for now.
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

