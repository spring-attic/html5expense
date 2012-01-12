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

import com.springsource.html5expense.Expense;
import com.springsource.html5expense.services.JpaExpenseReportingService;
import org.hibernate.dialect.H2Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

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
@Import(LocalDataSourceConfig.class)
@ComponentScan(basePackageClasses = JpaExpenseReportingService.class)
public class ComponentConfig {

    @Inject
    private DataSourceConfig dataSourceConfig;

    @Bean
    public DataSourceInitializer dataSourceInitializer() {
        DataSourceInitializer dsi = new DataSourceInitializer();
        dsi.setDataSource(this.dataSourceConfig.dataSource());
        dsi.setEnabled(true);
        ResourceDatabasePopulator dp = new ResourceDatabasePopulator();
        dp.setContinueOnError(true);
        dp.setIgnoreFailedDrops(true);
        dp.setScripts(new Resource[]{
                new ClassPathResource("/setup/schema.sql"),
                new ClassPathResource("/setup/demo-data.sql")
        });
        dsi.setDatabasePopulator(dp);
        return dsi;
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws Exception {
        EntityManagerFactory emf = entityManagerFactory().getObject();
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
        factory.setDataSource(this.dataSourceConfig.dataSource());
        factory.setPackagesToScan(Expense.class.getPackage().getName());

        return factory;
    }

}
