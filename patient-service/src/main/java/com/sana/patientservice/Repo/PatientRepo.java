package com.sana.patientservice.Repo;

import com.sana.patientservice.DTO.Validators.CreatePatientValidationGroup;
import com.sana.patientservice.model.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface PatientRepo extends JpaRepository<Patient, UUID> {

    boolean existsByEmailAndIdNot(@NotNull @Email String email, UUID id);


    boolean existsByEmail( String email);
}
