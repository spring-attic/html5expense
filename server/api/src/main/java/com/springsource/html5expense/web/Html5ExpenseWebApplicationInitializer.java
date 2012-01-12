package com.springsource.html5expense.web;

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;

/**
 *
 *
 * Removing <CODE>web.xml</CODE>
 *
 * @author Josh Long
 */
public class Html5ExpenseWebApplicationInitializer /*implements WebApplicationInitializer*/ {

    private AnnotationConfigWebApplicationContext applicationContextForServletContext(ServletContext sc, String... pkgs) {
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.setServletContext(sc);
        if(pkgs !=null && pkgs.length > 0)
        applicationContext.scan(pkgs);
        return applicationContext;
    }
    
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
