package com.wondertek.mybatis.service;

import com.wondertek.mybatis.domain.Employee;
import com.wondertek.mybatis.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    EmployeeMapper employeeMapper;

    @RequestMapping("getAll")
    public List<Employee> getAll() {
        return employeeMapper.getAll();
    }
}
