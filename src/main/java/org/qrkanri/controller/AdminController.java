package org.qrkanri.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.qrkanri.dto.DashboardDto;
import org.qrkanri.entity.Employee;
import org.qrkanri.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final DashboardService dashboardService;

    @GetMapping("/admin/employee-new")
    public String newEmployeePage(HttpSession session){
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null){
            return "redirect:/login";
        }
        if (!"ADMIN".equals(employee.getRole())){
            return "redirect:/home";
        }
        return "employee-new";
    }

    @GetMapping("/admin/shift-manage")
    public String shiftManagePage(HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "redirect:/login";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "redirect:/home";
        }
        return "shift-manage";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "redirect:/login";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "redirect:/home";
        }
        return "admin-dashboard";
    }

    @GetMapping("/api/dashboard/today")
    @ResponseBody
    public List<DashboardDto> getTodayDashboard() {
        return dashboardService.getTodayDashboard();
    }
}
