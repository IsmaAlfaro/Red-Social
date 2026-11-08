package com.unimatch.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/access")
    public String access() {
        return "access";
    }

    @GetMapping("/discover")
    public String discover() {
        return "discover";
    }
}
