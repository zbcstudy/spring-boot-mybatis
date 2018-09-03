package com.wondertek.mybatis.common;

import com.wondertek.mybatis.domain.Department;
import com.wondertek.mybatis.domain.Employee;
import com.wondertek.mybatis.mapper.DepartmentMapper;
import com.wondertek.mybatis.mapper.EmployeeMapper;
import com.wondertek.mybatis.mapper.EmployeeMapperPlus;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mybatis初使用
 *  SqlSession 代表和数据库的一次会话，用完必须关闭
 *  SqlSession和connection一样都是非线程安全的，每次使用都应该去获取对象
 *  mapper接口没有实现，mybatis会为接口生成一个代理对象（将接口和xml绑定）
 *  两个重要的文件：
 *      Mybatis全局配置文件，包含数据库连接池信息，事务管理器。。。系统运行环境
 *      sql映射文件：保存每一个sql语句的映射语句
 */
public class MybatisTest {

    public SqlSessionFactory getSqlSessionFactory() {
        String resourcePath = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        return sqlSessionFactory;
    }

    /**
     * mybatis hello-world使用
     * @throws IOException
     */
    @Test
    public void test1() throws IOException {
//        String resourcePath = "mybatis-config.xml";
//        InputStream inputStream = Resources.getResourceAsStream(resourcePath);
//
//        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();

        //获取sqlSession对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            Employee employee = sqlSession.selectOne("org.mybatis.example.EmployeeMapper.selectEmp", 1);
            System.out.println(employee);
        } finally {
            sqlSession.close();
        }


    }


    @Test
    public void test2() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //获取sqlSession对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            //获取接口的实现类对象
            //mybatis会为接口创建一个代理对象,代理对象执行增删改查
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            Employee employee = mapper.getEmpById(1);
            System.out.println(mapper);
            System.out.println(employee);
        } finally {
            sqlSession.close();
        }

    }

    /**
     * 1 mybatis允许一下返回值
     * Integer Long Boolean void
     * 2 需要手动提交数据
     */
    @Test
    public void testInsert() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //sqlSession不会自动提交
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
            //测试添加
            Employee employee = new Employee(null, "tom", "1", "jerry@qq.com");
            mapper.addEmp(employee);
            System.out.println(employee.getId());

            //测试更新
//            Employee employee = new Employee(1, "jerry", "1", "jerry@qq.com");
//            Boolean aBoolean = mapper.updateEmp(employee);
//            System.out.println(aBoolean);

            //测试删除
//            mapper.deleteEmp(2);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }

    }

    @Test
    public void testMulti() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //获取sqlSession对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

//            Employee employee = mapper.getEmpByIdAndLastName(5,"tom");
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", 5);
            paramMap.put("lastName", "tom");
            Employee employee = mapper.getEmpByMap(paramMap);
            System.out.println(mapper);
            System.out.println(employee);
        } finally {
            sqlSession.close();
        }

    }

    @Test
    public void testForMore() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //获取sqlSession对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

//            Employee employee = mapper.getEmpByIdAndLastName(5,"tom");
            List<Employee> list = mapper.getEmpByLastNameLike("%o%");
            for (Employee employee : list) {
                System.out.println(employee);
            }
//
//            Map<String, Object> map = mapper.getEmpByIdReturnMap(1);
//            System.out.println(map);
            Map<Integer, Employee> returnMap = mapper.getEmpByLastNameLikeReturnMap("%r%");
            System.out.println(returnMap);
        } finally {
            sqlSession.close();
        }

    }

    @Test
    public void testPlus() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            EmployeeMapperPlus mapper = sqlSession.getMapper(EmployeeMapperPlus.class);
            Employee empById = mapper.getEmpById(1);
            System.out.println(empById);
        }finally {
            sqlSession.close();
        }
    }

    /**
     * 级联查询测试
     * getEmpAndTemp
     */
    @Test
    public void testPlus1() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            EmployeeMapperPlus mapper = sqlSession.getMapper(EmployeeMapperPlus.class);
            Employee empAndTemp = mapper.getEmpAndTemp(1);
            System.out.println(empAndTemp.getDepartment());
            System.out.println(empAndTemp);
        }finally {
            sqlSession.close();
        }
    }

    @Test
    public void testPlus2() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            DepartmentMapper mapper = sqlSession.getMapper(DepartmentMapper.class);
//            Department department = mapper.getDeptByIdPlus(1);
//            System.out.println(department);
//            System.out.println(department.getEmps());
            Department deptByIdStep = mapper.getDeptByIdStep(1);
            System.out.println(deptByIdStep.getDepartmentName());
            System.out.println(deptByIdStep.getEmps());
        }finally {
            sqlSession.close();
        }
    }
}
