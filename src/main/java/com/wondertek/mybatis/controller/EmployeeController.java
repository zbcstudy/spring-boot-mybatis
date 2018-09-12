package com.wondertek.mybatis.controller;

import com.wondertek.mybatis.domain.Employee;
import com.wondertek.mybatis.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/emp")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @RequestMapping("/getAll")
    @ResponseBody
    public List<Employee> getAll() {
        return employeeService.getAll();
    }

    @RequestMapping("/")
    public String getA() {
        return "success";
    }
}
