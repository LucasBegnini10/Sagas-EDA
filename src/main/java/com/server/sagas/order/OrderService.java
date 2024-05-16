package com.server.sagas.order;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.ticket.dto.BuyTicketDTO;
import com.server.sagas.ticket.dto.PaymentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final AmazonSNS snsClient;
    @Value("${aws.sns.topic.order-created.arn}")
    private String orderCreatedTopicArn;


    @Autowired
    public OrderService(AmazonSNS amazonSNS){
        this.snsClient = amazonSNS;
    }

    public void createOrderByRequestBuyTicket(BuyTicketDTO buyTicketDTO){
        OrderModel orderCreated = createOrder(buyTicketDTO);
        System.out.println("Create order => " + orderCreated.getOrderId());
        this.sendEventCreatedOrder(orderCreated);
    }

    private OrderModel createOrder(BuyTicketDTO buyTicketDTO){
        String userId = buyTicketDTO.userId();
        String eventId = buyTicketDTO.eventId();
        PaymentDTO payment = buyTicketDTO.payment();

        //persist order

        return new OrderModel(userId, eventId, payment);
    }

    private void sendEventCreatedOrder(OrderModel order){
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(order);

            Topic topic = new Topic().withTopicArn(this.orderCreatedTopicArn);
            this.snsClient.publish(topic.getTopicArn(), jsonStr);

        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    public void cancelOrder(OrderModel order){
        System.out.println("Cancel order => " + order.getOrderId());
        //cancel order
    }
}
