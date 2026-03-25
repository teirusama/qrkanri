package org.qrkanri.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.Employee;
import org.qrkanri.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public String login(@RequestParam String loginid,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model){
        Employee employee = employeeRepository
                .findByLoginidAndActiveTrue(loginid)
                .orElse(null);
        if (employee == null || !passwordEncoder.matches(password, employee.getPassword())){
            model.addAttribute("error", "IDまたはPWが正しくありません。");
            return "login";
        }
        request.getSession().invalidate();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("employee", employee);
        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/login?msg=logout";
    }
}
