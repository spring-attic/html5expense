package com.springsource.html5expense.config;

import org.joda.time.LocalTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(Map<String, Object> model) {

        LocalTime localTime = new LocalTime();
        model.put("now", localTime.toString());

        return "index";
    }

}
