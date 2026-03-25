package org.qrkanri.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {

    @GetMapping("/")
    public String root(){
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }
}
