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

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

/**
 * Removing <CODE>web.xml</CODE>
 *
 * @author Josh Long
 */
public class Html5ExpenseWebApplicationInitializer
//        implements WebApplicationInitializer
{

    private AnnotationConfigWebApplicationContext applicationContextForServletContext(ServletContext sc, String... pkgs) {
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.setServletContext(sc);
        if (pkgs != null && pkgs.length > 0)
            applicationContext.scan(pkgs);
        return applicationContext;
    }

 /*   @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        AnnotationConfigWebApplicationContext servicesContext = applicationContextForServletContext(servletContext, CustomerService.class.getPackage().getName());
        servletContext.addListener(new ContextLoaderListener(servicesContext));
        servletContext.addFilter("hiddenMethodHttpFilter", new HiddenHttpMethodFilter());

        AnnotationConfigWebApplicationContext webContext = applicationContextForServletContext(servletContext, CustomerController.class.getPackage().getName());
        ServletRegistration.Dynamic servlet = servletContext.addServlet("spring", new DispatcherServlet(webContext));
        servlet.setLoadOnStartup(1);
        Set<String> conflicts = servlet.addMapping("/");

        if (!conflicts.isEmpty())
            throw new IllegalStateException("'appServlet' could not be mapped to '/' due "
                    + "to an existing mapping. This is a known issue under Tomcat versions "
                    + "<= 7.0.14; see https://issues.apache.org/bugzilla/show_bug.cgi?id=51278");

    }*/
/*
   <filter>
           <filter-name>hiddenHttpMethodFilter</filter-name>
           <filter-class>org.springframework.web.filter.HiddenHttpMethodFilter</filter-class>
       </filter>
       <filter-mapping>
           <filter-name>hiddenHttpMethodFilter</filter-name>
           <url-pattern>/</url-pattern>
           <servlet-name>appServlet</servlet-name>
       </filter-mapping>
    */

    //  @Override
    /*public void onStartup(ServletContext servletContext) throws ServletException {

        AnnotationConfigWebApplicationContext servicesContext = applicationContextForServletContext( servletContext ) ; 
        servletContext.addListener(new ContextLoaderListener(servicesContext));



        servletContext.addFilter("hiddenMethodHttpFilter", new HiddenHttpMethodFilter() ).addMappingForUrlPatterns( null ,false, "/");

        AnnotationConfigWebApplicationContext webContext = applicationContextForServletContext(
                servletContext, ExpenseReportingApiController.class.getPackage().getName() );
        ServletRegistration.Dynamic servlet = servletContext.addServlet("spring", new DispatcherServlet(webContext));
        servlet.setLoadOnStartup(1);
        Set<String> conflicts = servlet.addMapping("/");

        if (!conflicts.isEmpty())
            throw new IllegalStateException("'appServlet' could not be mapped to '/' due "
                    + "to an existing mapping. This is a known issue under Tomcat versions "
                    + "<= 7.0.14; see https://issues.apache.org/bugzilla/show_bug.cgi?id=51278");

    }*/
}
