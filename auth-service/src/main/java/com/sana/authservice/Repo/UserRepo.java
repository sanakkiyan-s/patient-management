package com.sana.authservice.Repo;

import com.sana.authservice.Model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.jaas.JaasPasswordCallbackHandler;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<AppUser,Long> {

    Optional<AppUser> findByEmail(String email);
    Boolean existsByEmail(String email);
}
