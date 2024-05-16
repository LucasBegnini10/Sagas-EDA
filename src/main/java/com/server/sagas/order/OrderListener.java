package com.server.sagas.order;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.config.aws.sqs.SqsListener;
import com.server.sagas.ticket.dto.BuyTicketDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderListener {
    @Value("${aws.sqs.order-create.queue}")
    private String orderCreateQueue;

    @Value("${aws.sqs.order-cancel-queue}")
    private String orderCancelQueue;

    private final AmazonSQS amazonSQSClient;
    private final OrderService orderService;

    @Autowired
    public OrderListener(AmazonSQS amazonSQSClient, OrderService orderService) {
        this.amazonSQSClient = amazonSQSClient;
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 2000)
    public void listenOrderCreate() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.orderCreateQueue);

            for (Message message : messages) {
                createOrderByMessage(message.getBody());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void createOrderByMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BuyTicketDTO buyTicketDTO = objectMapper.readValue(message, BuyTicketDTO.class);
            this.orderService.createOrderByRequestBuyTicket(buyTicketDTO);
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void listenOrderCancel() {
        try {
            List<Message> messages = SqsListener.listen(this.amazonSQSClient, this.orderCancelQueue);

            for (Message message : messages) {
                processOrderCancel(message.getBody());
            }

        } catch (Exception e) {
            System.out.println("Error receiving message => " + e);
        }
    }

    private void processOrderCancel(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            OrderModel order = objectMapper.readValue(message, OrderModel.class);
            this.orderService.cancelOrder(order);
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

}
