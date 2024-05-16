package com.server.sagas.ticket;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.order.OrderModel;
import com.server.sagas.ticket.dto.BuyTicketDTO;
import com.server.sagas.ticket.dto.TicketErrorHoldDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final AmazonSNS snsClient;
    @Value("${aws.sns.topic.request-buy-ticket.arn}")
    private String requestBuyTicketTopicArn;

    @Value("${aws.sns.topic.ticket-held.arn}")
    private String ticketHeldTopicArn;

    @Value("${aws.sns.topic.ticket-error-hold.arn}")
    private String tickedErrorHoldTopicArn;


    @Autowired
    public TicketService(AmazonSNS snsClient){
        this.snsClient = snsClient;
    }

    public void buyTicket(BuyTicketDTO buyTicketDTO)  {
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(buyTicketDTO);

            Topic topic = new Topic().withTopicArn(requestBuyTicketTopicArn);

            this.snsClient.publish(topic.getTopicArn(), jsonStr);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    public void holdTicketByOrder(OrderModel order){
        if(!eventHasTicketsAvailable(order.getEventId())){
            sendEventErrorHoldTicket(order, "UNAVAILABLE");
        }else{
            try {
                holdTicket(order);
                sendEventTicketHeld(order);
            } catch (Exception ex){
                sendEventErrorHoldTicket(order, ex.getMessage());
            }
        }
    }

    private Boolean eventHasTicketsAvailable(String eventId){
        return true; //set false to cancel order
    }

    private void holdTicket(OrderModel order) {
        //hold
    }

    private void sendEventTicketHeld(OrderModel order){
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(order);

            Topic topic = new Topic().withTopicArn(ticketHeldTopicArn);

            this.snsClient.publish(topic.getTopicArn(), jsonStr);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    private void sendEventErrorHoldTicket(OrderModel order, String message){
        try {

            TicketErrorHoldDTO payload = new TicketErrorHoldDTO(message, order);

            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(payload);

            Topic topic = new Topic().withTopicArn(tickedErrorHoldTopicArn);

            this.snsClient.publish(topic.getTopicArn(), jsonStr);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex){
            ex.printStackTrace();
        }
    }
}
