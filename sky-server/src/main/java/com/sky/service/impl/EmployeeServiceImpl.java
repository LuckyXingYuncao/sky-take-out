package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.exception.UsernameAlreadyExistsException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        log.info("员工登录：username={}", username);

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            log.warn("登录失败，账号不存在：{}", username);
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对（前端传过来的明文密码进行MD5加密后，再和数据库中的密文比对）
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            log.warn("登录失败，密码错误：{}", username);
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            log.warn("登录失败，账号被锁定：{}", username);
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        log.info("员工登录成功：username={}", username);
        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        log.info("新增员工：username={}", employeeDTO.getUsername());

        //检查用户名是否已存在
        Employee existing = employeeMapper.getByUsername(employeeDTO.getUsername());
        if (existing != null) {
            log.warn("新增员工失败，用户名已存在：{}", employeeDTO.getUsername());
            throw new UsernameAlreadyExistsException(MessageConstant.USERNAME_ALREADY_EXISTS);
        }

        Employee employee = new Employee();
        //将DTO中的属性值赋值给Employee对象
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置账号的状态 默认为1 （正常） 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);
        //设置账号的默认密码123456 在MD5加密后的密码值
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employeeMapper.insert(employee);
        log.info("新增员工成功：username={}", employee.getUsername());
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询：{}", employeePageQueryDTO);
        int page = employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize();
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        PageHelper.startPage(page, pageSize);
        List<Employee> employeeList = employeeMapper.pageQuery(employeePageQueryDTO);
        Page<Employee> p = (Page<Employee>) employeeList;
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        log.info("启用/禁用员工：id={}, status={}", id, status);
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id) {
        log.info("根据ID查询员工：id={}", id);
        Employee employee = employeeMapper.getById(id);
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        log.info("修改员工信息：{}", employeeDTO);
        // 检查用户名是否已被其他员工占用
        Employee existing = employeeMapper.getByUsername(employeeDTO.getUsername());
        if (existing != null && !existing.getId().equals(employeeDTO.getId())) {
            log.warn("修改员工失败，用户名已存在：{}", employeeDTO.getUsername());
            throw new UsernameAlreadyExistsException(MessageConstant.USERNAME_ALREADY_EXISTS);
        }
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);
    }

    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        log.info("修改密码：empId={}", passwordEditDTO.getEmpId());
        Employee employee = employeeMapper.getById(passwordEditDTO.getEmpId());
        if (employee == null) {
            log.warn("修改密码失败，员工不存在：id={}", passwordEditDTO.getEmpId());
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        String oldPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(employee.getPassword())) {
            log.warn("修改密码失败，旧密码错误：empId={}", passwordEditDTO.getEmpId());
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
        String newPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());
        Employee updateEmployee = Employee.builder()
                .id(passwordEditDTO.getEmpId())
                .password(newPassword)
                .build();
        employeeMapper.update(updateEmployee);
        log.info("修改密码成功：empId={}", passwordEditDTO.getEmpId());
    }
}