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
