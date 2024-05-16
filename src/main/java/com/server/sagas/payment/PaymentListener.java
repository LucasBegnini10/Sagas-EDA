package com.server.sagas.payment;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.config.aws.sqs.SqsListener;
import com.server.sagas.order.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentListener {

    @Value("${aws.sqs.process-payment-queue}")
    private String processPaymentQueue;

    @Value("${aws.sqs.payment-refund-queue}")
    private String processPaymentRefundQueue;

    private final AmazonSQS amazonSQSClient;

    private final PaymentService paymentService;

    @Autowired
    public PaymentListener(AmazonSQS amazonSQSClient, PaymentService paymentService) {
        this.amazonSQSClient = amazonSQSClient;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelay = 2000)
    public void listenProcessPaymentQueue() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.processPaymentQueue);

            for (Message message : messages) {
                processHoldTicketByOrder(message.getBody());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void processHoldTicketByOrder(String message){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            OrderModel order = objectMapper.readValue(message, OrderModel.class);

            System.out.println("Process payment => " + order.getOrderId());

            this.paymentService.processPaymentByOrder(order);

        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void listenProcessPaymentRefundQueue() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.processPaymentRefundQueue);

            for (Message message : messages) {
                processPaymentRefundQueue(message.getBody());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void processPaymentRefundQueue(String message){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            OrderModel order = objectMapper.readValue(message, OrderModel.class);

            System.out.println("Process refund => " + order.getOrderId());

            this.paymentService.processPaymentRefund(order);

        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }
}
