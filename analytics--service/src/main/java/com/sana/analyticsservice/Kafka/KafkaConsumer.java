package com.sana.analyticsservice.Kafka;

import ch.qos.logback.core.util.Loader;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.nio.ByteBuffer;

@Service
public class KafkaConsumer {

    Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "patient-topic",groupId = "patient_group")
    public void getMessage(byte[] message) {
        PatientEvent patientEvent = null;
        try {
            patientEvent = PatientEvent.parseFrom(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        logger.info("Received a patient event");
        // Logic to process the incoming message
        System.out.println("Received message: " + patientEvent.getEmail());
        // Additional processing can be added here
    }
}
