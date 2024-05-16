package com.server.sagas.order;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.ticket.dto.BuyTicketDTO;
import com.server.sagas.ticket.dto.TicketErrorHoldDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderListener {
    @Value("${aws.sqs.request-buy-ticket.queue}")
    private String queueName;

    @Value("${aws.sqs.ticket-error-hold-queue}")
    private String queueTicketErrorHold;

    private final AmazonSQS amazonSQSClient;
    private final OrderService orderService;

    @Autowired
    public OrderListener(AmazonSQS amazonSQSClient, OrderService orderService) {
        this.amazonSQSClient = amazonSQSClient;
        this.orderService = orderService;
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
                processRequestBuyTicket(message.getBody());
                amazonSQSClient.deleteMessage(queueUrl, message.getReceiptHandle());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void consumeMessagesTicketError() {
        try {
            String queueUrl = amazonSQSClient.getQueueUrl(queueTicketErrorHold).getQueueUrl();

            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                    .withWaitTimeSeconds(20)
                    .withMaxNumberOfMessages(10);

            ReceiveMessageResult receiveMessageResult = amazonSQSClient.receiveMessage(receiveMessageRequest);

            for (Message message : receiveMessageResult.getMessages()) {
                processTicketErrorHold(message.getBody());
                amazonSQSClient.deleteMessage(queueUrl, message.getReceiptHandle());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void processRequestBuyTicket(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BuyTicketDTO buyTicketDTO = objectMapper.readValue(message, BuyTicketDTO.class);
            this.orderService.createOrderByRequestBuyTicket(buyTicketDTO);
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    private void processTicketErrorHold(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TicketErrorHoldDTO ticketErrorHoldDTO = objectMapper.readValue(message, TicketErrorHoldDTO.class);
            this.orderService.cancelOrder(ticketErrorHoldDTO.getOrder(), ticketErrorHoldDTO.getMessage());
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

}
