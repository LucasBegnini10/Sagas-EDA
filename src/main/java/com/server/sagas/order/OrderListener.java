package com.server.sagas.order;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.config.aws.sqs.SqsListener;
import com.server.sagas.ticket.dto.BuyTicketDTO;
import com.server.sagas.ticket.dto.TicketErrorHoldDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderListener {
    @Value("${aws.sqs.request-buy-ticket.queue}")
    private String requestBuyTicketQueue;

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
    public void listenRequestsBuyTicket() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.requestBuyTicketQueue);

            for (Message message : messages) {
                processRequestBuyTicket(message.getBody());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void consumeMessagesTicketError() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.queueTicketErrorHold);

            for (Message message : messages) {
                processTicketErrorHold(message.getBody());
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
