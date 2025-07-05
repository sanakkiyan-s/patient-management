package com.sana.patientservice.Controller;

import com.sana.patientservice.DTO.PatientRequestDTO;
import com.sana.patientservice.DTO.PatientResponseDTO;
import com.sana.patientservice.DTO.Validators.CreatePatientValidationGroup;
import com.sana.patientservice.DTO.Validators.UpdatePatientValidationGroup;
import com.sana.patientservice.Service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@Tag(name = "Patient Management", description = "APIs for managing patients")
@Slf4j
public class PatientController {



    private PatientService patientService;
    @PreAuthorize("hasRole('USER')")
    @GetMapping("api/patients")
    @Operation(summary = "Get all patients", description = "Retrieve a list of all patients")
    public ResponseEntity<List<PatientResponseDTO>> getAll(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        log.info(header);
        log.info("enter the get");

        return new ResponseEntity<>(patientService.getAllPatients(), HttpStatus.OK);
    }

    @PostMapping("api/patients")
    @Operation(summary = "Create a new patient", description = "Create a new patient with the provided details")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class, UpdatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patient) {
        return new ResponseEntity<>(patientService.createPatient(patient), HttpStatus.CREATED);
    }

    @PutMapping("api/patients/{id}")
    @Operation(summary = "Update an existing patient", description = "Update the details of an existing patient by ID")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Validated({Default.class, UpdatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patient) {
        // Assuming there's an update method in the service
        return new ResponseEntity<>(patientService.updatePatient(id, patient), HttpStatus.OK);
    }

    @DeleteMapping("api/patients/{id}")
    @Operation(summary = "Delete a patient", description = "Delete a patient by ID")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
