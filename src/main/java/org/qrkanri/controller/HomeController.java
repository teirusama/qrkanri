package org.qrkanri.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.Attendance;
import org.qrkanri.entity.Employee;
import org.qrkanri.repository.AttendanceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final AttendanceRepository attendanceRepository;

    @GetMapping("/home")
    public String home(HttpSession session, Model model){
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null){
            return "redirect:/login";
        }
        Optional<Attendance> attendance = attendanceRepository.findByEmployeeAndWorkDate(employee, LocalDate.now());
        String status = "OFF";
        if (attendance.isPresent() && attendance.get().getCheckOut() == null){
            status ="ON";
        }
        model.addAttribute("status", status);
        model.addAttribute("employee", employee);
        return "home";
    }
}
