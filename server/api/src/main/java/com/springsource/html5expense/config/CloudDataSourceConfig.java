/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.html5expense.config;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.document.MongoServiceCreator;
import org.cloudfoundry.runtime.service.relational.PostgresqlServiceCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.List;

@Profile("cloud")
@Configuration
public class CloudDataSourceConfig implements DataSourceConfig {

    private CloudEnvironment cloudEnvironment = new CloudEnvironment();

    @Bean
    @Override
    public DataSource dataSource() throws Exception {
        RdbmsServiceInfo rdbmsServiceInfo = requireOneService("RDBMS", RdbmsServiceInfo.class);
        return new PostgresqlServiceCreator().createService(rdbmsServiceInfo);
    }

    @Bean
    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        MongoServiceInfo mongoServiceInfo = requireOneService("MongoDB", MongoServiceInfo.class);
        MongoDbFactory mongoDbFactory = new MongoServiceCreator().createService(mongoServiceInfo);
        return new MongoTemplate(mongoDbFactory);
    }

    private <T extends AbstractServiceInfo> T requireOneService(String serviceName, Class<T> clazzOfT) throws Exception {
        String errMsg = "There must be only one %s service bound to this application. Currently, there are %s %s services.";
        List<T> serviceInfoList = cloudEnvironment.getServiceInfos(clazzOfT);
        Assert.isTrue(serviceInfoList.size() == 1, String.format(errMsg, serviceName, serviceInfoList.size(), serviceName));
        return serviceInfoList.iterator().next();
    }
}
