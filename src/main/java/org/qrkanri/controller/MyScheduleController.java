package org.qrkanri.controller;

import jakarta.servlet.http.HttpSession;
import org.qrkanri.entity.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyScheduleController {

    @GetMapping("/my-schedule")
    public String mySchedule(HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "redirect:/login";
        }
        return "my-schedule";
    }
}
