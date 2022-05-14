package com.stackdev.springwebclient.controllers;

import com.stackdev.springwebclient.dto.Users;
import com.stackdev.springwebclient.services.WebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/web")
public class WebClientController {

    @Autowired
    WebClientService webClientService;

    @GetMapping("/user/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Users> getUserById(@PathVariable Long id){
        return webClientService.findUserById(id);
    }

    @GetMapping(value = "/users",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Flux<Users> findAllUsers(){
        return webClientService.findUsers();
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveUser(@RequestBody Users users){webClientService.saveUser(users);}


    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Users> updateUser(@RequestBody Users user){return webClientService.updateUser(user);}

    @DeleteMapping("/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> deleteUser(@PathVariable Long id){return webClientService.deleteUser(id);}

}
