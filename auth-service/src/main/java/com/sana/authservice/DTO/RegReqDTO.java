package com.sana.authservice.DTO;

import com.sana.authservice.Model.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;



@Getter
@Setter
@Data
public class RegReqDTO {

    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private Set<String> roles;
}
