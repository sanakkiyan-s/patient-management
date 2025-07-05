package com.sana.authservice.Config;

import com.sana.authservice.Model.AppUser;
import com.sana.authservice.Repo.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public  class AppUserDetailsService implements UserDetailsService {


    private final UserRepo userRepo;
    public AppUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }



    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser  user= userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        return new AppUserDetails(user);
    }
}
