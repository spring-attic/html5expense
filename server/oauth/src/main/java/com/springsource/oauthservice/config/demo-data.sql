insert into App (name, slug, description, organization, website, apiKey, secret, redirectUrl, grantTypes)
    values ('Demo', 'demo', 'Demo', 'SpringSource', 'http://www.springsource.org', '09e749d8309f4044', '189309492722aa5a', '', 'password,authorization_code,refresh_token');

insert into AppDeveloper (app, developer) values (1, 1);