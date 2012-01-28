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
package com.springsource.html5expense.web;

import com.springsource.html5expense.config.WebConfig;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

/**
 * This class is a programmatic alternative to the <CODE>web.xml</CODE> configuration file.
 * 
 * This works with Servlet 3 environments (GlassFish 3.1, Tomcat 7.0.15x or better, etc.).
 * 
 * This class is an implementation of {@link WebApplicationInitializer} which is a Spring SPI.
 * Spring will locate implementations of this SPI at servlet container startup and automatically give them a
 * chance to run. 
 * 
 * Here, we setup a root Spring {@link org.springframework.context.ApplicationContext}, as well as an instance of
 * {@link DispatcherServlet} and a filter, which transforms requests to a RESTful URL using one HTTP verb into a request
 * using another type of verb. It does this by relying on metadata in the request to tell Spring which HTTP verb.
 * This is handy for the situation where you want to make a RESTful request against a URL that requires an HTTP
 * verb that the client environment doesn't support, like <CODE>DELETE</CODE> and <CODE>PUT</CODE> methods, from a browser.
 *
 *
 * @author Josh Long
 */
public class WebApplicationInitializer implements org.springframework.web.WebApplicationInitializer {

    private String servletName = "appServlet";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {


        // listener (root)
        AnnotationConfigWebApplicationContext applicationContext = buildApplicationContext(
                servletContext,
                new String[0],
                new Class<?>[]{WebConfig.class},
                new CloudAwareApplicationContextInitializer());
        servletContext.addListener(new ContextLoaderListener(applicationContext));

        // filter
        servletContext.addFilter("hiddenHttpMethodFilter", new HiddenHttpMethodFilter()).addMappingForUrlPatterns(null, true, "/");

        // web endpoint
        Set<String> conflicts = servletContext.addServlet(servletName, new DispatcherServlet()).addMapping("/");
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("'" + servletName + "' could not be mapped to '/' due "
                    + "to an existing mapping. This is a known issue under Tomcat versions "
                    + "<= 7.0.14; see https://issues.apache.org/bugzilla/show_bug.cgi?id=51278");
        }

    }

    private AnnotationConfigWebApplicationContext buildApplicationContext(ServletContext sc,
                                                                          String[] pkgs,
                                                                          Class<?>[] configClasses,
                                                                          ApplicationContextInitializer<ConfigurableWebApplicationContext> applicationContextInitializer
    ) {
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.setServletContext(sc);

        if (pkgs != null && pkgs.length > 0)
            applicationContext.scan(pkgs);

        if (configClasses != null && configClasses.length > 0)
            for (Class<?> c : configClasses)
                applicationContext.register(c);

        if (null != applicationContextInitializer)
            applicationContextInitializer.initialize(applicationContext);

        return applicationContext;
    }

}
