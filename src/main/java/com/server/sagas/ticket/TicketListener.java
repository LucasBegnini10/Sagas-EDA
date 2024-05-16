package com.server.sagas.ticket;

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
public class TicketListener {
    @Value("${aws.sqs.ticket-hold-queue}")
    private String ticketHoldQueue;

    @Value("${aws.sqs.ticket-confirm-queue}")
    private String ticketConfirmQueue;

    @Value("${aws.sqs.ticket-hold-cancel-queue}")
    private String ticketHoldCancelQueue;

    private final AmazonSQS amazonSQSClient;
    private final TicketService ticketService;

    @Autowired
    public TicketListener(AmazonSQS amazonSQSClient, TicketService ticketService) {
        this.amazonSQSClient = amazonSQSClient;
        this.ticketService = ticketService;
    }

    @Scheduled(fixedDelay = 2000)
    public void listenTicketHoldRequests() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.ticketHoldQueue);

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

            System.out.println("Hold ticket => " + order.getOrderId());

            ticketService.holdTicketByOrder(order);
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void listenTicketConfirmQueue() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.ticketConfirmQueue);

            for (Message message : messages) {
                processTicketConfirm(message.getBody());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void processTicketConfirm(String message){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            OrderModel order = objectMapper.readValue(message, OrderModel.class);

            System.out.println("Confirm hold ticket => " + order.getOrderId());

            ticketService.confirmHoldTicker(order);
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void listenTicketHoldCancelQueue() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.ticketHoldCancelQueue);

            for (Message message : messages) {
                processTicketHoldCancel(message.getBody());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void processTicketHoldCancel(String message){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            OrderModel order = objectMapper.readValue(message, OrderModel.class);

            System.out.println("Cancel hold ticket => " + order.getOrderId());

            ticketService.cancelHold(order);
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }
}
