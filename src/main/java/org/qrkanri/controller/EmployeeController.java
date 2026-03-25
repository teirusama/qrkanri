package org.qrkanri.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.Employee;
import org.qrkanri.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin/employees")
    @ResponseBody
    public Map<String,String> createEmployeeByAdmin(@RequestBody Employee employee,
                                                    HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return Map.of("result","unauthorized");
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return Map.of("result","forbidden");
        }
        if (employeeRepository.existsByLoginid(employee.getLoginid())){
            return Map.of("result","duplicate");
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setRole("USER");
        employee.setActive(true);
        employeeRepository.save(employee);
        return Map.of("result","ok");
    }

    @GetMapping("/employees")
    @ResponseBody
    public Object getAllEmployees(HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "権限がありません。";
        }
        return employeeRepository.findByActiveTrueOrderByIdAsc();
    }

    @GetMapping("/admin/employee-list")
    public String employeeListPage(HttpSession session, Model model){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "redirect:/login";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "redirect:/home";
        }
        List<Employee> employees = employeeRepository.findByActiveTrueOrderByIdAsc();
        model.addAttribute("employees", employees);
        return "employee-list";
    }

    @GetMapping("/admin/employees/search")
    @ResponseBody
    public List<Employee> searchEmployees(@RequestParam String type,
                                          @RequestParam String keyword){
        switch(type){
            case "name":
                return employeeRepository.findByNameContainingAndActiveTrueOrderByIdAsc(keyword);
            case "loginid":
                return employeeRepository.findByLoginidContainingAndActiveTrueOrderByIdAsc(keyword);
            case "phone":
                return employeeRepository.findByPhoneContainingAndActiveTrueOrderByIdAsc(keyword);
            case "email":
                return employeeRepository.findByEmailContainingAndActiveTrueOrderByIdAsc(keyword);
            default:
                return employeeRepository.findByActiveTrueOrderByIdAsc();
        }
    }

    @DeleteMapping("/employees/{id}")
    @ResponseBody
    public String deleteEmployee(@PathVariable Long id,
                                 HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "権限がありません。";
        }
        Employee target = employeeRepository.findById(id)
                .orElseThrow();
        if("ADMIN".equals(target.getRole())){
            throw new RuntimeException("管理者IDは削除できません。");
        }
        target.setActive(false);
        employeeRepository.save(target);
        return "deleted";
    }

    @PostMapping("/admin/verify-password")
    @ResponseBody
    public Map<String, Boolean> verifyPassword(@RequestBody Map<String,String> body,
                                               HttpSession session){
        Employee admin = (Employee) session.getAttribute("employee");
        if (admin == null){
            return Map.of("valid", false);
        }
        boolean valid = passwordEncoder.matches(body.get("password"), admin.getPassword());
        return Map.of("valid", valid);
    }

    @PatchMapping("/employees/{id}")
    @ResponseBody
    public Object updateEmployee(@PathVariable Long id,
                                 @RequestBody Employee updated,
                                 HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "権限がありません。";
        }
        Employee employee = employeeRepository.findById(id)
                .orElseThrow();
        employee.setDuty(updated.getDuty());
        employee.setPhone(updated.getPhone());
        employee.setEmail(updated.getEmail());
        return employeeRepository.save(employee);
    }

    @PostMapping("/admin/reset-password")
    @ResponseBody
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body,
                                             HttpSession session){
        Employee admin = (Employee) session.getAttribute("employee");
        if (admin == null || !"ADMIN".equals(admin.getRole())){
            return Map.of("result", "forbidden");
        }
        Long employeeId = Long.valueOf(body.get("employeeId"));
        String newPassword = body.get("newPassword");
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null){
            return Map.of("result", "notfound");
        }
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
        return Map.of("result", "ok");
    }
}
