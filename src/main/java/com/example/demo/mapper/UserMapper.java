package com.example.demo.mapper;

import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> getAllUsers();

    int addUser(User user);

    int updUserById(User user);

    User findUserById(@Param("id") int id);

    int delUserById(@Param("id") int id);
}
