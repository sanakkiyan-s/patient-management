package com.sana.patientservice.GRPC;

import billing.BillingServiceGrpc;
import billing.CreateBillingAccountRequest;
import billing.CreateBillingAccountResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceCilent {

    private BillingServiceGrpc.BillingServiceBlockingStub stub;

    private static final Logger log =  LoggerFactory.getLogger(
            BillingServiceCilent.class);

    public BillingServiceCilent(@Value("${billing.service.address:localhost}") String serverAddress,
                                @Value("${billing.service.grpc.port:9001}") int serverPort) {


        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,
                serverPort).usePlaintext().build();
        stub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public CreateBillingAccountResponse createBillingAccount(String patientId, String name,
                                                             String email) {
        CreateBillingAccountRequest request = CreateBillingAccountRequest.newBuilder().setPatientId(patientId).setName(name).setEmail(email).build();
        CreateBillingAccountResponse response = stub.createBillingAccount(request);

        log.info("Billing account created for patient ID: {}, Name: {}",
                response.getAccountId() ,response.getStatus());
        return response;
    }

}
