package org.qrkanri.repository;

import org.qrkanri.entity.Employee;
import org.qrkanri.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByEmployee(Employee employee);
    List<Shift> findByWorkDateAndEmployeeActiveTrue(LocalDate workDate);
    List<Shift> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate start, LocalDate end);
    List<Shift> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
    List<Shift> findByWorkDateBetweenAndEmployeeActiveTrue(LocalDate start, LocalDate end);
    boolean existsByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
    int countByWorkDateAndEmployeeActiveTrue(LocalDate workDate);
}
