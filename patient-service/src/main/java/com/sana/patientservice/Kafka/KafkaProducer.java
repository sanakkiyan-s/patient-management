package com.sana.patientservice.Kafka;

import com.sana.patientservice.model.Patient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaProducer {

    private final KafkaTemplate <String,byte[]> kafkaTemplate;

    Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void send(String topic, Patient patient){

        PatientEvent patientEvent= PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("CREATE")
                .build();

        try {
            logger.info("Kafka going to message sent successfully for patient event: {}", patientEvent.getEmail());
            kafkaTemplate.send(topic, patientEvent.toByteArray());

            logger.info("Kafka message sent successfully for patient event: {}", patientEvent.getEmail());
        } catch (Exception e) {

            logger.info("Error sending kafka message patient event");
            ;
        }


    }

}
