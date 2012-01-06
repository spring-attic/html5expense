package com.springsource.html5expense;


import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class LoggingHttpSessionListener  implements HttpSessionListener
{
    @Override
    public void sessionCreated(HttpSessionEvent se) {
     System.out.println( se.getSource());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
