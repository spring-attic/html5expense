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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.relational.PostgresqlServiceCreator;
import org.hibernate.dialect.H2Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

import com.springsource.html5expense.Expense;
import com.springsource.html5expense.services.JpaExpenseReportingService;

/**
 * Configuration for application @Components such as @Services, @Repositories, and @Controllers.
 * Loads externalized property values required to configure the various application properties.
 * Not much else here, as we rely on @Component scanning in conjunction with @Inject by-type autowiring.
 *
 * @author Keith Donald
 * @author Josh Long
 * @author Roy Clarkson
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = JpaExpenseReportingService.class)
public class ComponentConfig {
    
    @Bean
    public DataSource dataSource() {
        CloudEnvironment ce = new CloudEnvironment();
        List<RdbmsServiceInfo> l = ce.getServiceInfos(RdbmsServiceInfo.class);
        Assert.isTrue(l.size() == 1, "There must be only one RDBMS service bound to this application");
        RdbmsServiceInfo rsi = l.iterator().next();        
        PostgresqlServiceCreator creator = new PostgresqlServiceCreator();
        return creator.createService(rsi);
    }
    
    @Bean
    DataSourceInitializer dataSourceInitializer() {
        DataSourceInitializer dsi = new DataSourceInitializer();
        dsi.setDataSource(dataSource());
        dsi.setEnabled(true);
        ResourceDatabasePopulator dp = new ResourceDatabasePopulator();
        dp.setContinueOnError(true);
        dp.setIgnoreFailedDrops(true);
        dp.setScripts(new Resource[]{ 
            new ClassPathResource("com/springsource/html5expense/config/schema.sql"),
            new ClassPathResource("com/springsource/html5expense/config/demo-data.sql")
            });
        dsi.setDatabasePopulator(dp);
        return dsi;
    }

//    @Bean
//    public DataSource dataSource() throws Exception {
//        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
//        dataSource.setUrl("jdbc:h2:tcp://localhost/~/expenses");
//        dataSource.setDriverClass(Driver.class);
//        dataSource.setUsername("sa");
//        dataSource.setPassword("");
//        return dataSource;
//    }

    @Bean
    public PlatformTransactionManager transactionManager() throws Exception {
        EntityManagerFactory emf  = entityManagerFactory().getObject() ;
        return new JpaTransactionManager(emf);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws Exception {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("hibernate.dialect", H2Dialect.class.getName());

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(jpaVendorAdapter);
        factory.setJpaPropertyMap(properties);
        factory.setDataSource(dataSource());
        factory.setPackagesToScan(new String[]{Expense.class.getPackage().getName()});

        return factory;
    }

}
