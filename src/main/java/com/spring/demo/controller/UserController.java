package com.spring.demo.controller;

import com.spring.demo.model.User;
import com.spring.demo.servicio.redis.BD.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private User[] users;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping()
    public ArrayList<?> listUser() {
        ArrayList users = new ArrayList();
        User user1 = new User(1L, "Daniela");
        User user2 = new User(2L, "Karla");
        users.add(user1);
        users.add(user2);
        return users;
    }

}
