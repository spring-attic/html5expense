/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.html5expense.config;

import com.mongodb.Mongo;
import org.postgresql.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

@Configuration
@Profile("local")
public class LocalDataSourceConfig implements DataSourceConfig {

    private Class<Driver> driverClass = Driver.class;

    private String postgresDb = "expenses", postgresHost = "127.0.0.1", user = "expenses", pw = "expenses";

    private int postgresPort = 5432;

    private String mongoDatabaseName = "expensesfs";

    private String mongoDbHost = "127.0.0.1";

    private String url = String.format("jdbc:postgresql://%s:%s/%s", this.postgresHost, this.postgresPort, this.postgresDb);

    @Bean
    @Override
    public DataSource dataSource() throws Exception {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setUrl(this.url);
        dataSource.setDriverClass(this.driverClass);
        dataSource.setUsername(this.user);
        dataSource.setPassword(this.pw);
        return dataSource;
    }

    @Bean
    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(new Mongo(this.mongoDbHost), this.mongoDatabaseName);
    }

}
