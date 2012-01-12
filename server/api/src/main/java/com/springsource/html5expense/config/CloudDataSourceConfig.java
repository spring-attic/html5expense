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

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.relational.PostgresqlServiceCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.List;

@Profile("cloud")
@Configuration
public class CloudDataSourceConfig implements DataSourceConfig {

    @Override
    @Bean
    public DataSource dataSource() {
        CloudEnvironment cloudEnvironment = new CloudEnvironment();
        List<RdbmsServiceInfo> serviceInfoList = cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class);
        Assert.isTrue(serviceInfoList.size() == 1,
                "There must be only one RDBMS service bound to this application." +
                        " Currently, there are " + serviceInfoList.size() + " RDBMS data sources.");
        RdbmsServiceInfo rdbmsServiceInfo = serviceInfoList.iterator().next();
        PostgresqlServiceCreator creator = new PostgresqlServiceCreator();
        return creator.createService(rdbmsServiceInfo);
    }
}
