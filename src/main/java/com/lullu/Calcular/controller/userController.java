package com.lullu.Calcular.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lullu.Calcular.model.userModel;
import com.lullu.Calcular.service.userService;

@RestController
@RequestMapping("/api/users")  // IMPORTANTE: adicione isso
public class userController {

    @Autowired
    private final userService userservice;

     public userController (userService userservice) {
        this.userservice = userservice;
    }   

    @GetMapping
    public List<userModel> getAll() {
        return userservice.getAll();
    }

    @PostMapping
    public userModel create(@RequestBody userModel user) {
        return userservice.save(user);
    }

    @DeleteMapping("/{id}")
    public void delete (@PathVariable Long id) {
        userservice.delete(id);
    }
}