# Reactive Programming with SpringWebFlux-WebClient 

In this tutorial you are going to learn how to go Reactive with Spring Webflux and WebClient.
### Show some :heart: and :star: the repo to support the project

## 1a. Introduction
Reactive Programming is a programming paradim that centers around asyncronous data streams.
This means that a system is reactive if it is responsive ,resilient , elastic and event driven.
Spring Webflux is a fully non-blocking web framework that fully centers around reactive programming. Spring Weblfux archives this by
using Project Reactor that supports reactive streams back pressure and runs on non-blocking servers such as Netty,
Undertow and Servlet 3.1+ containers. Non-blocking servers are generally based on the event loop model which uses only a small number of threads
handling requests.
When talking about non-blocking or asynchronous request processing, it means no thread is in a waiting state. 
Essentially, threads are able to complete their task without waiting for previous tasks to be completed.

## 1b. Spring Boot  (Initializer)
To start off with you can use Spring Initializr  to get the Spring Boot project structure for you, and this can be found [here](https://start.spring.io/)

Once you get there in this case im using Maven , and that's my personal preference over Gradle since it gives a nice xml layout for your setup , in terms of installing dependency , plugins etc. its essentially a tool for your project management, just like package.json for those who are familiar with node js.

You also need to add a couple of dependencies which are:
* Spring Reactive Web - Build reactive web applications with Spring WebFlux and Netty.
* Spring Data R2DBC - Provides Spring Reactive database connectivity
* r2dbc-postgresql - Postgres reactive database driver
* Lombok - Java annotation library which helps reduce boilerplate code

## 2. Database Configuration
Unlike Spring MVC which support JPA , Spring Webflux dosent have ORMs like Hibernate ,then schema management would have to do it manually,
which is a bit of a trade off , when you chose to go with Spring Webflux.

### a) DBConfig
Create a Database Configuration class
```java
package com.stackdev.springwebflux;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
public class DBConfig extends AbstractR2dbcConfiguration {
    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:postgresql://localhost:5433/testdb");
    }


    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory){
        var initializer =  new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        var databasePopulator = new CompositeDatabasePopulator();
        databasePopulator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        databasePopulator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("data.sql")));
        initializer.setDatabasePopulator(databasePopulator);
        return initializer;
    }
}
```
This class is going to be annotated with @Configuration, meaning everytime we boot up out application Spring  will have to run this class.
Notice we are extending the AbstractR2dbcConfiguration for our reactive database connection.
The connection factory method is for connection our local  postgres database testdb that is running on port 5433, note in your case you might be using the default postgres port 5432.
We have also Injected a *Bean* initializer in this method, we are going to initialize our database schema.
Like i mentioned earlier this is a bit of a trade off that Spring Webflux has
because you would have to manually manage your database schemas since it dosen't support ORMs like JPA - Hibernate.

Now let's create the schemas.

### b) Creating SQL resource Schemas
Go to ***resources*** and create the files schema.sql and data.sql.

* schema.sql

Let's create table called users
```sql 
DROP TABLE IF EXISTS users;
create table users(id serial primary key, name varchar(255),surname varchar(255),email varchar(255),username varchar(255),password varchar(255))
```
* data.sql

Lets populate our users table
```roomsql
insert into users(name,surname,username,email,password) values('Daniel', 'Marcus','user12','user@gmail.com','password123')
insert into users(name,surname,username,email,password) values('Daniel2', 'Marcus1','user12','user@gmail.com','password123')
insert into users(name,surname,username,email,password) values('Daniel3', 'Marcus2','user12','user@gmail.com','password123')
insert into users(name,surname,username,email,password) values('Daniel4', 'Marcus4','user12','user@gmail.com','password123')
insert into users(name,surname,username,email,password) values('Daniel5', 'Marcus5','user12','user@gmail.com','password123')
```

## 3. Creating the Rest API
Now let's  create the Blue print to our Webflux Rest APi
### a) Models
Inside your root project, create a package called ***models***. In this package we are going to create our data class called **Users**
```java
package com.stackdev.springwebflux.models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
}

```
This is the data class that is going to represent our users table , that we manually created with the **schema.sql** file.
Now since we installed Lombok we dont have to manually implement things like accessor method , and constructors.
@Data from lombok annotation is going to inject our access methods ie getter and setters.
@NoArgsConstructor - is for our default constructor
@AllArgsConstructor - is for our non-default constructor

### b) Repository
Inside your root project, create a package called ***repositories***. In this package we are going to create an interface class called **UserRepository**.

```java
package com.stackdev.springwebflux.repositories;
import com.stackdev.springwebflux.models.Users;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveCrudRepository<Users, Long> {
}
```
You are probably noticing similarities with Spring MVC, instead on extending JPARepository like in Spring MVC , here we extend our interface with ReactiveCrudRepository.

### c) Service
Inside your root project, create a package called ***services***. In this package we are going to create an interface class called **UserService**.
```java
package com.stackdev.springwebflux.services;
import com.stackdev.springwebflux.models.Users;
import com.stackdev.springwebflux.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    //Mono 0 - 1 //single
    //Flux 0 - N //reactive sequence of items

    public Mono<Users> getUserById(Long id){
        return userRepository.findById(id);
    }

    public Flux<Users> getUsers(){
        return  userRepository.findAll()
                .delayElements(
                        Duration.ofSeconds(2)
                );
    }


    public void addUser(Users users){
        userRepository.save(users).subscribe();
    }

    public Mono<Users> updateUser(Users user){
        return userRepository.findById(user.getId())
                .switchIfEmpty(Mono.error(new Exception("User Not Found")))
                .map(olderUser ->{
                    if(user.getSurname() != null) olderUser.setSurname(user.getSurname());
                    if(user.getUsername() != null) olderUser.setUsername(user.getUsername());
                    if(user.getName() != null) olderUser.setName(user.getName());
                    if(user.getEmail() != null) olderUser.setEmail(user.getEmail());
                    return  olderUser;
                })
                .flatMap(userRepository::save);
    }

    public Mono<Void> deleteUser(Long id){
        return userRepository.deleteById(id)
                .switchIfEmpty(Mono.error(new Exception("User Not found")));
    }
}

```
First im going to start by injecting the UserRepository by annotating it with @Autowired

As mentioned in the introduction section Spring WebFlux uses the Project Reactor for its reactive asyncrounous programming.
Spring Webflux uses ***Flux*** and ***Mono*** for its data publishers.
* *Flux* publishes a stream of elements from 0..N 
* *Mono* published a stream of elements from 0..1

For methods that will return a single stream instance we are going to wrap them with Mono as the return type.
For method that will return multiple stream of elements we will put return type Flux.

* Notice these return types are also enforced from  our ReactiveCrudRepository. eg ***userRepository.findById(id)*** it returns a single instance of the user as Mono.
* Notice For the save user we are subscribing with empty method to indicate the termination of the stream **userRepository.save(users).subscribe()**
  
* Notice in the getUsers method i have introduced a delay of 2 seconds. This is to demonstrate the non-blocking pattern of Spring Webflux. When you test this method you will probably notice that
the data will be returned as a stream, the client won't  have to wait for the whole response to be returned the client will subscribe to the data as it comes ,which is pretty cool in my opinion :)

### d) Controller
Inside your root project, create a package called ***controller***. In this package we are going to create an interface class called **UserController**.
```java
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

    @DeleteMapping("/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    public  Mono<Void> deleteUser(@PathVariable Long id){return  userService.deleteUser(id);}

}

```
In the controller we are injecting the ***UserService*** we just created. Notice the return type of our endpoints they are just the same as the ones in the Service Class.
I hope at this point everything else is now clear, you now have a full rest api running on Netty(Reactive Server) with Spring MVC.

# 4 Spring WebClient

Now that we have created our Spring WebFlux API its probably time we jump to Spring WebClient. 
So what is Spring WebClient?
I quote from Spring Documentation:
```txt
Simply put, WebClient is an interface representing the main entry point for performing web requests.

It was created as part of the Spring Web Reactive module and will be replacing the classic RestTemplate in these scenarios. In addition, the new client is a reactive, non-blocking solution that works over the HTTP/1.1 protocol.

It's important to note that even though it is, in fact, a non-blocking client and it belongs to the spring-webflux library, the solution offers support for both synchronous and asynchronous operations, making it suitable also for applications running on a Servlet Stack.
```
* I guess the above defination is self explanatory :) Now let jump into creating our service.
* So we are going to create a Seperate spring project that is going to query our Spring Webflux service that we created above.

### 4a. Spring Boot  (Initializer)
To start off with you can use Spring Initializer  to get the Spring Boot project structure for you, and this can be found [here](https://start.spring.io/)

You also need to add a couple of dependencies which are:
* Spring Reactive Web - Build reactive web applications with Spring WebFlux and Netty.
* Lombok - Java annotation library which helps reduce boilerplate code.

As mentioned above Spring React Web dependancy comes the Spring WebClient.

### 4b. WebClient Service
Let's create our ***WebClientService*** just go ahead and create in inside a package called **services**.
```java
package com.stackdev.springwebclient.services;
import com.stackdev.springwebclient.dto.Users;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {
    private final WebClient webClient;

    public WebClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api").build();
    }

    public Flux<Users> findUsers(){
        return this.webClient.get().uri("/users")
                .retrieve()
                .bodyToFlux(Users.class);
    }

    public Mono<Users> findUserById(Long id){
        return this.webClient.get().uri("/user/{id}",id)
                .retrieve()
                .bodyToMono(Users.class);
    }

    public Mono<ClientResponse> saveUser(Users user){
        return this.webClient.post().uri("/save")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(user), Users.class)
                .exchange();
    }

    public Mono<Users> updateUser(Users user){
        return this.webClient.put().uri("/update")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(user), Users.class)
                .retrieve()
                .bodyToMono(Users.class);
    }

    public Mono<Void> deleteUser(Long id){
        return this.webClient.delete().uri("/user/{id}")
                .retrieve()
                .bodyToMono(Void.class);
    }
}

```
* The above service is the main class of our webclient. In this class we are writing our rest endpoint calls to call our webflux service we created earlier on.

```java
    private final WebClient webClient;
    public WebClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api").build();
    }
```
* Firstly i started by initializing the WebClient, in the constructor of our services we are simply injecting our webClient by create a builder that will be able to point our webClient to our Spring WebFlux service running on **http://localhost:8080/api**.

* Now that we have our ***baseUrl*** set for our webClient we then went on ahead and create the CRUD Service calls.

```java
public Flux<Users> findUsers(){
        return this.webClient.get().uri("/users")
                .retrieve()
                .bodyToFlux(Users.class);
    }
```
* Notice how we are using the same conversion we used in our WebFlux ie.
* *Flux* to return 0..N elements of a reactive stream
* *Mono* to return 0..1 0 to single element of a reactive stream

In the above snippet we are using the webClient to run our *GET* request to http://localhost:8080/api/users.
* The **retrieve** method is used to get the response from the webflux service.
* The **bodyToFlux** is used to get response body only of a  flux as a Stream of Users.
* The **Users** class is the DTO i created under the dto package here is how it looks:
```java
package com.stackdev.springwebclient.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private String name;
    private String surname;
    private String email;
    private String username;
    private String password;
}
```
The saveUser we are passing the header to our request by putting the content- type as application/json
```java
public Mono<ClientResponse> saveUser(Users user){
        return this.webClient.post().uri("/save")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(user), Users.class)
                .exchange();
    }
```
* The **body** method is used to pass the Json request as a Mono of type Users
* The **exchange** method is used to get the StatusReposnse  

NOTE: Unlike retrieve(), when using exchange(), it is the responsibility of the application to
consume any response content regardless of the scenario (success, error, unexpected data, etc). Not doing so can cause a memory leak. See ClientResponse for a list of all the available options for consuming the body.

* For the updateUser and deleteUser services, we are just returnig the single instance of our stream as a mono using the bodyToMono , with deleteUser returning a Void.

### 4c. WebClient Controller
For the controller im not going to dwell much on this one , 
Just go ahead and create ***WebClientController*** class inside a new package called **controllers**

```java
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
```
* Inside the controller we are simply going to inject the webservice that we created, and use the same conversation that we used for our webclient.

* That's probably in the last writing i will do a load testing for Spring MVC vs Spring Webflux be sure to watch out for that one.
  
END !!

* If there is anything you feel i should have covered or improve ,Please let me know in the comments section below.

Thank you for taking your time in reading this article.

KINDLY FORK AND STAR THE [REPO](https://github.com/nyakaz73/SpringWebFlux-WebClient) TO SUPPORT THIS PROJECT :)

### Source Code Git repo
The source code of this [repo](https://github.com/nyakaz73/SpringWebFlux-WebClient)
### Pull Requests
I Welcome and i encourage all Pull Requests....
## Created and Maintained by
* Author: [Tafadzwa Lameck Nyamukapa](https://github.com/nyakaz73)
* Email:  [tafadzwalnyamukapa@gmail.com]
* Youtube Channel: [Stack{Dev}](https://www.youtube.com/channel/UCacNBWW7T2j_St593VHvulg)
* Open for collaborations and Remote Work!!
* Happy Coding!!

### License

```
MIT License

Copyright (c) 2022 Tafadzwa Lameck Nyamukapa

```