package org.qrkanri.repository;

import org.qrkanri.entity.Attendance;
import org.qrkanri.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
    List<Attendance> findByEmployee_Id(Long employeeId);
    List<Attendance> findByEmployeeOrderByWorkDateDesc(Employee employee);
    List<Attendance> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate start, LocalDate end);
    List<Attendance> findByCheckOutIsNull();
    List<Attendance> findByWorkDateAndEmployeeActiveTrue(LocalDate workDate);
    int countByWorkDateAndEmployeeActiveTrue(LocalDate workDate);
}