package com.stackdev.springwebflux.repositories;

import com.stackdev.springwebflux.models.Users;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveCrudRepository<Users, Long> {
}
