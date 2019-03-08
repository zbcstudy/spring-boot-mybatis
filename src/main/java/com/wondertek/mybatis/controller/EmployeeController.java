package com.wondertek.mybatis.controller;

import com.wondertek.mybatis.domain.Employee;
import com.wondertek.mybatis.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @RequestMapping("/getById/{id}")
    public Map getById(@PathVariable Integer id) {
        return employeeService.getEmployeeById(id);
    }


    @RequestMapping("/")
    public String getA() {
        return "success";
    }
}
