package com.alan.community.dao;

import com.alan.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
// @Mapper注解是由Mybatis框架中定义的一个描述数据层接口的注解，
// 用于告诉spring框架此接口的实现类由Mybatis负责创建，并将其实现类对象存储到spring容器中。
//Mapper注解的接口的抽象方法的实现有两种方式：1.xml文件实现 2.直接在抽象方法上注解实现
@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);

}
