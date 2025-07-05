package com.sana.patientservice.Mapper;

import com.sana.patientservice.DTO.PatientResponseDTO;
import com.sana.patientservice.model.Patient;
import org.apache.el.lang.FunctionMapperImpl;
import org.springframework.stereotype.Service;

import java.util.function.Function;
@Service
public class PatientMapper implements Function<Patient, PatientResponseDTO> {
    @Override
    public PatientResponseDTO apply(Patient patient) {
        return new PatientResponseDTO(patient.getId().toString(),
                patient.getName(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getDateOfBirth().toString());
    }
}
