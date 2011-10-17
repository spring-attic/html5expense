--drop table AppDeveloper
--drop table oauth_client_details;
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

