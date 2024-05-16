package com.server.sagas.payment;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sagas.order.OrderModel;
import com.server.sagas.ticket.dto.PaymentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final AmazonSNS snsClient;

    @Value("${aws.sns.topic.payment-processed.arn}")
    private String paymentProcessedTopic;

    @Value("${aws.sns.topic.payment-process-error.arn}")
    private String paymentProcessErrorTopic;

    @Autowired
    public PaymentService(AmazonSNS amazonSNS){
        this.snsClient = amazonSNS;
    }

    public void processPaymentByOrder(OrderModel order){
        if(paymentAlreadyProcessed(order)) return;

        try {
            processPayment(order.getPaymentDTO());
            sendEventPaymentProcessed(order);
        } catch (Exception ex){
            sendEventPaymentProcessError(order);
        }
    }

    private boolean paymentAlreadyProcessed(OrderModel orderModel){
        return false;
    }

    private void processPayment(PaymentDTO paymentDTO) throws Exception {
        //process payment
        //throw new Exception("Unauthorized");
    }


    private void sendEventPaymentProcessed(OrderModel order){
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(order);

            Topic topic = new Topic().withTopicArn(this.paymentProcessedTopic);
            this.snsClient.publish(topic.getTopicArn(), jsonStr);

        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    private void sendEventPaymentProcessError(OrderModel order){
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(order);

            Topic topic = new Topic().withTopicArn(this.paymentProcessErrorTopic);
            this.snsClient.publish(topic.getTopicArn(), jsonStr);

        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
    }

    public void processPaymentRefund(OrderModel order){
        //refund payment
    }

}
