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

    @Value("${aws.sqs.ticket-held.queue}")
    private String ticketHeldQueue;

    private final AmazonSQS amazonSQSClient;

    @Autowired
    public PaymentListener(AmazonSQS amazonSQSClient) {
        this.amazonSQSClient = amazonSQSClient;
    }

    @Scheduled(fixedDelay = 2000)
    public void listenTicketHeldQueue() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, ticketHeldQueue);

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

            System.out.println("Process payment by order => " + order.getOrderId());


        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }
}
