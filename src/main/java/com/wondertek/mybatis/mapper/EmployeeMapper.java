package com.wondertek.mybatis.mapper;

import com.wondertek.mybatis.domain.Employee;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface EmployeeMapper {

    /**
     * 查询出多条数据封装成map map的key是逐渐的值，value是employee对象
     * 使用@MapKey("id")注解指定map的key的值是id 或者是employee中的其他属性
     * @param lastName
     * @return
     */
    @MapKey("id")
    Map<Integer, Employee> getEmpByLastNameLikeReturnMap(String lastName);

    /**
     * 查询出单条数据，将JavaBean封装到map中
     *
     * @param id
     * @return
     */
    Map<String, Object> getEmpByIdReturnMap(Integer id);

    /**
     * 查询出的数据封装成一个集合
     *
     * @param lastName
     * @return
     */
    public List<Employee> getEmpByLastNameLike(String lastName);

    Employee getEmpByMap(Map<String, Object> paramMap);

    Employee getEmpByIdAndLastName(@Param("id") Integer id, @Param("lastName") String lastName);

    public Employee getEmpById(Integer id);

    public void addEmp(Employee employee);

    public Boolean updateEmp(Employee employee);

    public void deleteEmp(Integer id);

}
