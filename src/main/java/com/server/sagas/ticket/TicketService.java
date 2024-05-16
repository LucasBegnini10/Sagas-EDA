package com.server.sagas.ticket;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.order.OrderModel;
import com.server.sagas.ticket.dto.BuyTicketDTO;
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

    @Value("${aws.sns.topic.ticket-hold-error.arn}")
    private String ticketErrorHoldTopicArn;

    @Value("${aws.sns.topic.ticket-confirm-error.arn}")
    private String ticketConfirmErrorTopicArn;


    @Autowired
    public TicketService(AmazonSNS snsClient){
        this.snsClient = snsClient;
    }

    public void buyTicket(BuyTicketDTO buyTicketDTO)  {
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(buyTicketDTO);

            Topic topic = new Topic().withTopicArn(requestBuyTicketTopicArn);

            System.out.println("Requested buy ticket!");
            this.snsClient.publish(topic.getTopicArn(), jsonStr);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    public void holdTicketByOrder(OrderModel order){
        if(!eventHasTicketsAvailable(order.getEventId())){
            sendEventErrorHoldTicket(order);
        }else{
            try {
                holdTicket(order);
                sendEventTicketHeld(order);
            } catch (Exception ex){
                sendEventErrorHoldTicket(order);
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

    private void sendEventErrorHoldTicket(OrderModel order){
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(order);

            Topic topic = new Topic().withTopicArn(ticketErrorHoldTopicArn);

            this.snsClient.publish(topic.getTopicArn(), jsonStr);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    public void confirmHoldTicker(OrderModel order){
        try {
            confirmTicket(order);
            //Finish flow :)
        } catch (Exception ex){
            sendEventTicketConfirmError(order);
        }
    }

    private void confirmTicket(OrderModel order) throws Exception {
        //confirm
        throw new Exception("Error confirm ticket");
    }

    private void sendEventTicketConfirmError(OrderModel order){
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(order);

            Topic topic = new Topic().withTopicArn(ticketConfirmErrorTopicArn);

            this.snsClient.publish(topic.getTopicArn(), jsonStr);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    public void cancelHold(OrderModel orderModel){
        //cancel
    }

}
