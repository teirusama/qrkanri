package org.qrkanri.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.Employee;
import org.qrkanri.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/checkin/{employeeId}")
    public String checkIn(@PathVariable Long employeeId,
                          HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole()) &&
                !loginUser.getId().equals(employeeId)){
            return "権限がありません。";
        }
        try{
            attendanceService.checkIn(employeeId);
            return "出勤登録完了";
        }
        catch (Exception e){
            return e.getMessage();
        }
    }

    @PostMapping("/checkout/{employeeId}")
    public String checkOut(@PathVariable Long employeeId,
                           HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole()) &&
                !loginUser.getId().equals(employeeId)){
            return "権限がありません。";
        }
        try{
            attendanceService.checkOut(employeeId);
            return "退勤登録完了";
        }
        catch (Exception e){
            return e.getMessage();
        }
    }

    @GetMapping("/attendance/{employeeId}")
    public Object getAttendances(@PathVariable Long employeeId,
                                 HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole()) &&
                !loginUser.getId().equals(employeeId)){
            return "管理者権限が必要です。";
        }
        try{
            return attendanceService.getAttendancesByEmployee(employeeId);
        }
        catch(Exception e){
            return e.getMessage();
        }
    }
}
