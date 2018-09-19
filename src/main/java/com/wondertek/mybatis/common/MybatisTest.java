package com.wondertek.mybatis.common;

import com.wondertek.mybatis.domain.Department;
import com.wondertek.mybatis.domain.Employee;
import com.wondertek.mybatis.mapper.DepartmentMapper;
import com.wondertek.mybatis.mapper.EmployeeMapper;
import com.wondertek.mybatis.mapper.EmployeeMapperDynamicSQL;
import com.wondertek.mybatis.mapper.EmployeeMapperPlus;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
//            System.out.println(mapper);
            System.out.println(employee);
            Employee employee1 = mapper.getEmpById(1);
            System.out.println(employee == employee1);
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

    @Test
    public void testDynamicSQL() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            EmployeeMapperDynamicSQL mapper = sqlSession.getMapper(EmployeeMapperDynamicSQL.class);
            Employee employee = new Employee(null, null, null, null);
//            List<Employee> empsByConditionIf = mapper.getEmpsByConditionIf(employee);
//            for (Employee employee1 : empsByConditionIf) {
//                System.out.println(employee1);
//            }

//            List<Employee> empsByConditionByTrim = mapper.getEmpsByConditionByTrim(employee);
//            for (Employee employee1 : empsByConditionByTrim) {
//                System.out.println(employee1);
//            }

            List<Employee> empsByConditionByChoose = mapper.getEmpsByConditionByChoose(employee);
            for (Employee employee1 : empsByConditionByChoose) {
                System.out.println(employee1);
            }
        }finally {
            sqlSession.close();
        }
    }

    @Test
    public void testDynamicUp() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            EmployeeMapperDynamicSQL mapper = sqlSession.getMapper(EmployeeMapperDynamicSQL.class);
            Employee employee = new Employee(4, "jerry1", "0", null);
            mapper.updateEmp(employee);
            sqlSession.commit();
        }finally {
            sqlSession.close();
        }
    }

    /**
     * 测试动态sql的foreach功能
     */
    @Test
    public void testDynamicForeach() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            EmployeeMapperDynamicSQL mapper = sqlSession.getMapper(EmployeeMapperDynamicSQL.class);
            List<Employee> employees = mapper.getEmpByConditionByForeach(Arrays.asList(1, 2, 4, 5));
            for (Employee employee : employees) {
                System.out.println(employee);
            }
        }finally {
            sqlSession.close();
        }
    }

    /**
     * 测试批量插入
     */
    @Test
    public void testDynamicBatch() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            EmployeeMapperDynamicSQL mapper = sqlSession.getMapper(EmployeeMapperDynamicSQL.class);
            List<Employee> employees = new ArrayList<>();
            employees.add(new Employee(null, "tom", "1", "tom@qq.com", new Department(1)));
            employees.add(new Employee(null, "tom2", "0", "tom2@qq.com", new Department(2)));
            mapper.addEmps(employees);
            sqlSession.commit();
        }finally {
            sqlSession.close();
        }
    }

    /**
     * 测试mybatis的一级缓存（本地缓存）
     * 一级缓存失效的4种情况
     *  1.两次查询不在一个sqlSession中
     *  2.sqlSession相同，查询条件不同
     *  3.sqlSession相同，两次查询之间进行了增删改操作（本次操作有可能修改了数据）
     *  4.sqlSession相同，SQLSession被手动清空（手动清除一级缓存）
     *
     * 二级缓存（全局缓存）：基于nameSpace级别的缓存，一个namespace对应一个二级缓存
     * 工作机制：
     *  1.一个会话，查询一条数据，这个数据就会被放在当前会话的一级缓存
     *  2、若会话关闭，一级缓存中的数据会被保存在二级缓存中，新的会话查询信息，就可以参照二级缓存
     *  3.不同的namespace查出的数据会放在自己对应的缓存中（map）
     * 使用：
     *  1.开启二级缓存全局配置
     *  2.在mapper.xml中配置
     *      <cache></cache>
     *  3.POJO需要实现序列化接口
     *
     * 缓存相关的属性/设置 <setting name="cacheEnabled" value="true"/>
     *      cacheEnabled="true" 只能关闭二级缓存（一级缓存一直可用）
     *      每个查询语句中的 useCache="true"
     *                  false:不使用缓存（一级缓存一直可用）
     *      每个增删改标签：flushCache="ture" 每次进行增删改都会刷新缓存（一级，二级都会清空）
     *          查询标签默认：flushCache="false"
     *       sqlSession.clearCache(): 只会清除当前session的一级缓存
     *
     *       localCacheScope ：本地缓存作用域（一级缓存SESSION）：当前会话的所有数据保存在缓存中
     *                                         STATEMENT:可以禁用一级缓存
     *
     */

    @Test
    public void testSecondLevelCache() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //获取sqlSession对象
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        try {
            EmployeeMapper mapper = sqlSession1.getMapper(EmployeeMapper.class);
            EmployeeMapper mapper1 = sqlSession2.getMapper(EmployeeMapper.class);
            Employee emp = mapper.getEmpById(1);
            System.out.println(emp);
            sqlSession1.close();
            Employee emp1 = mapper1.getEmpById(1);
            System.out.println(emp1);
        }finally {
            sqlSession2.close();
        }
    }
    @Test
    public void testFirstLevelCache() {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //获取sqlSession对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            Employee employee = mapper.getEmpById(1);
            System.out.println(employee);
            mapper.updateEmp(new Employee(4, "tomUp", "1", "tomUp@qq.com", new Department(1)));
            Employee employee1 = mapper.getEmpById(1);
            System.out.println(employee == employee1);
        } finally {
            sqlSession.close();
        }
    }


    /**
     * 插件原理
     * 在4大对象创建的时候
     * 每个对象创建的时候不是直接返回，而是
     * interceptorChain.pluginAll(parameterHandler)
     * 获取所有的Interceptor(拦截器) （插件需要实现的接口）
     * 调用interceptor.plugin(target);返回target包装后的对象
     * 插件机制：我们可以为目标对象创建一个代理对象：AOP（面向切面）
     * 我们的插件可以为四大对象创建出代理对象
     * 代理对象就可以拦截到四大对象的每一个执行
     * InterceptorChain 类
     * public Object pluginAll(Object target) {
     *      for (Interceptor interceptor : interceptors) {
     *          target = interceptor.plugin(target);
     *      }
     *      return target;
     * }
     *   插件实现
     *      1.编写Interceptor的实现类
     *      2.插件签名 @intercepts
     *      3.在全局配置中注册插件
     *
     */
    @Test
    public void testPlugin() {

    }































}
