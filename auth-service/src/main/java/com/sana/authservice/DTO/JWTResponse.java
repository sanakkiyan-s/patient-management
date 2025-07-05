package com.sana.authservice.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Data
public class JWTResponse {

    private String token;
    private String type = "Bearer";
    private String email;
    private List<String> roles;

    public JWTResponse(String token, String email, List<String> roles) {
        this.token = token;
        this.email = email;
        this.roles = roles;
    }



}
