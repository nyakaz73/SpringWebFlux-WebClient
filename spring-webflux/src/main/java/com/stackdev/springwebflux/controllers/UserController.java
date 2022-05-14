package com.stackdev.springwebflux.controllers;

import com.stackdev.springwebflux.models.Users;
import com.stackdev.springwebflux.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping(value = "/users",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Flux<Users> findAllUsers(){
        return userService.getUsers();
    }

    @GetMapping("/user/{id}")
    public Mono<Users> findUserById(@PathVariable Long id){return userService.getUserById(id);}

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveUser(@RequestBody Users users){userService.addUser(users);}


    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Users> updateUser(@RequestBody Users user){return userService.updateUser(user);}

}
