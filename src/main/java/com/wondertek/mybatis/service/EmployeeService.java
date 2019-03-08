package com.wondertek.mybatis.service;

import com.github.pagehelper.PageHelper;
import com.wondertek.mybatis.domain.Employee;
import com.wondertek.mybatis.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    EmployeeMapper employeeMapper;

    public List<Employee> getAll() {
        PageHelper.startPage(1, 3);
        List<Employee> employees = employeeMapper.getAll();
        for (Employee employee : employees) {
            System.out.println(employee);
        }
        return employees;
    }

    public Map getEmployeeById(Integer id) {
        Map<String, Object> empByIdReturnMap = employeeMapper.getEmpByIdReturnMap(id);
        return empByIdReturnMap;
    }
}
