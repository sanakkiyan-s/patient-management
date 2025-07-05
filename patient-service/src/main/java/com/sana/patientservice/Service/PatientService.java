package com.sana.patientservice.Service;

import billing.BillingServiceGrpc;
import com.sana.patientservice.DTO.PatientRequestDTO;
import com.sana.patientservice.DTO.PatientResponseDTO;
import com.sana.patientservice.Expection.EmailAlreadyExists;
import com.sana.patientservice.Expection.PaitentNotFound;
import com.sana.patientservice.GRPC.BillingServiceCilent;
import com.sana.patientservice.Kafka.KafkaProducer;
import com.sana.patientservice.Mapper.PatientDTOToPatient;
import com.sana.patientservice.Mapper.PatientMapper;
import com.sana.patientservice.Repo.PatientRepo;
import com.sana.patientservice.model.Patient;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PatientService {
private BillingServiceCilent billingServiceCilent;
    private PatientRepo patientRepo;
    private PatientMapper patientMapper;
    private PatientDTOToPatient patientDTOToPatient;
    private KafkaProducer kafkaProducer;


    public List<PatientResponseDTO> getAllPatients() {
        return patientRepo.findAll()
                .stream()
                .map(patientMapper)
                .toList();
    }


    public PatientResponseDTO createPatient(PatientRequestDTO patient) {

        if (patientRepo.existsByEmail(patient.getEmail())) {
            throw new EmailAlreadyExists("Email already exists: " + patient.getEmail());
        }

        var patientEntity = patientDTOToPatient.apply(patient);


        Patient savedPatient = patientRepo.save(patientEntity);

        billingServiceCilent.createBillingAccount(savedPatient.getId().toString(),
                savedPatient.getName(), savedPatient.getEmail());
        kafkaProducer.send("patient-topic", savedPatient);



        return patientMapper.apply(savedPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, @Valid PatientRequestDTO patient) {

        Patient existingPatient = patientRepo.findById(id)
                .orElseThrow(() -> new PaitentNotFound("Patient not found with id: " + id));

        if(patientRepo.existsByEmailAndIdNot(patient.getEmail(), id)){

            throw new EmailAlreadyExists("Email already exists: " + patient.getEmail());

        }

        // Update the existing patient with new values
        existingPatient.setName(patient.getName());
        existingPatient.setEmail(patient.getEmail());
        existingPatient.setAddress(patient.getAddress());
        existingPatient.setDateOfBirth(LocalDate.parse(patient.getDateOfBirth()));

        Patient updatedPatient = patientRepo.save(existingPatient);
        return patientMapper.apply(updatedPatient);




    }

    public void deletePatient(UUID id) {
        if (!patientRepo.existsById(id)) {
            throw new PaitentNotFound("Patient not found with id: " + id);
        }
        patientRepo.deleteById(id);
    }
}
