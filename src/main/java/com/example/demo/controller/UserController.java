package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<User> getAllUsers(){
        return  userService.getAllUsers();
    }

    @PostMapping("/add")
    public int addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    @PutMapping("/upd")
    public int updUserById(@RequestBody User user){
        return userService.updUserById(user);
    }

    @GetMapping("/find/{id}")
    public User findUserById(@PathVariable("id") int id){
        return userService.findUserById(id);
    }

    @DeleteMapping("/delete/{id}")
    public int delUserById(@PathVariable("id") int id){
        return userService.delUserById(id);
    }
}
