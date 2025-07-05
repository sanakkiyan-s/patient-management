package com.sana.patientservice.Mapper;

import com.sana.patientservice.DTO.PatientRequestDTO;
import com.sana.patientservice.DTO.PatientResponseDTO;
import com.sana.patientservice.model.Patient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.function.Function;
@Service
public class PatientDTOToPatient implements Function<PatientRequestDTO, Patient> {
    @Override
    public Patient apply(PatientRequestDTO patientRequestDTO) {
        return new Patient(patientRequestDTO.getName(),
                patientRequestDTO.getEmail(),
                patientRequestDTO.getAddress(),
                LocalDate.parse(patientRequestDTO.getDateOfBirth()),
               LocalDate.parse( patientRequestDTO.getRegisteredDate()));
    }
}
