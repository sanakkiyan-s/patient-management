package com.sana.patientservice.DTO;

import com.sana.patientservice.DTO.Validators.CreatePatientValidationGroup;
import com.sana.patientservice.DTO.Validators.UpdatePatientValidationGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor

public class PatientRequestDTO {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;
    @NotBlank(message = "Email is required",groups =  {UpdatePatientValidationGroup.class})
    private String email;
    @NotBlank(message = "Address is required")
    private String address;
    @NotBlank(message = "Date of Birth is required")
    private String dateOfBirth;
    @NotNull(message = "Registered Date is required",groups =  {CreatePatientValidationGroup.class})
    private String registeredDate;
}
