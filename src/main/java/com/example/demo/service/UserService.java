package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> getAllUsers(){
        return  userMapper.getAllUsers();
    }


    public int addUser(User user){
        return userMapper.addUser(user);
    }

    public int updUserById(User user){
        return userMapper.updUserById(user);
    }

    public User findUserById(int id){
        return userMapper.findUserById(id);
    }

    public int delUserById(int id){
        return userMapper.delUserById(id);
    }
}
