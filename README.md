# Reactive Programming with SpringWebFlux-WebClient 

In this tutorial you are going to learn how to go Reactive with Spring Webflux and WebClient.
### Show some :heart: and :star: the repo to support the project

## 1. Introduction
Reactive Programming is a programming paradim that centers around asyncronous data streams.
This means that a system is reactive if it is responsive ,resilient , elastic and event driven.
Spring Webflux is a fully non-blocking web framework that fully centers around reactive programming. Spring Weblfux archives this by
using Project Reactor that supports reactive streams back pressure and runs on non-blocking servers such as Netty,
Undertow and Servlet 3.1+ containers. Non-blocking servers are generally based on the event loop model which uses only a small number of threads
handling requests.
When talking about non-blocking or asynchronous request processing, it means no thread is in a waiting state. 
Essentially, threads are able to complete their task without waiting for previous tasks to be completed.

## 1. Spring Boot  (Initializer)
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
As mentioned in the introduction section Spring WebFlux uses the Project Reactor for its reactive asyncrounous programming.
Spring Webflux uses ***Flux*** and ***Mono*** for its data publishers.
* *Flux* publishes a stream of elements from 0..N 
* *Mono* published a stream of elements from 0..1


