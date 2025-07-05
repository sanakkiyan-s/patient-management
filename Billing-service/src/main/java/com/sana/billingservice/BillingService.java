package com.sana.billingservice;

import billing.BillingServiceGrpc;
import billing.CreateBillingAccountRequest;
import billing.CreateBillingAccountResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.logging.Logger;

@GrpcService
public class BillingService extends BillingServiceGrpc.BillingServiceImplBase {

    Logger logger = Logger.getLogger(BillingService.class.getName());

    @Override
    public void createBillingAccount(CreateBillingAccountRequest request, StreamObserver<CreateBillingAccountResponse> responseObserver) {
      logger.info("createBillingAccount - start");
        logger.info("Received request: " + request);
        // Simulate billing account creation logic
        CreateBillingAccountResponse response = CreateBillingAccountResponse.newBuilder()
                .setAccountId("12345").setStatus("Account created successfully").build();

        logger.info("createBillingAccount - end");
        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
