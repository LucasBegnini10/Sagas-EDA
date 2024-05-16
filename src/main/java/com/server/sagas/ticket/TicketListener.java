package com.server.sagas.ticket;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.order.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TicketListener {
    @Value("${aws.sqs.order-created.queue}")
    private String queueName;

    private final AmazonSQS amazonSQSClient;
    private final TicketService ticketService;

    @Autowired
    public TicketListener(AmazonSQS amazonSQSClient, TicketService ticketService) {
        this.amazonSQSClient = amazonSQSClient;
        this.ticketService = ticketService;
    }

    @Scheduled(fixedDelay = 2000)
    public void consumeMessages() {
        try {
            String queueUrl = amazonSQSClient.getQueueUrl(queueName).getQueueUrl();

            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                    .withWaitTimeSeconds(5)
                    .withMaxNumberOfMessages(10);

            ReceiveMessageResult receiveMessageResult = amazonSQSClient.receiveMessage(receiveMessageRequest);

            for (Message message : receiveMessageResult.getMessages()) {
                processHoldTicketByOrder(message.getBody());
                amazonSQSClient.deleteMessage(queueUrl, message.getReceiptHandle());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void processHoldTicketByOrder(String message){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            OrderModel order = objectMapper.readValue(message, OrderModel.class);

            System.out.println("Hold ticket by order => " + order.getOrderId());

            ticketService.holdTicketByOrder(order);
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }
}
