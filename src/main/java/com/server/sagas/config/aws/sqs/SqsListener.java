package com.server.sagas.config.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import java.util.ArrayList;
import java.util.List;

public class SqsListener {

    public static List<Message> listen(AmazonSQS sqsClient, String queueName){
        List<Message> messages = new ArrayList<>();

        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                .withWaitTimeSeconds(5)
                .withMaxNumberOfMessages(10);

        ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest);

        for (Message message : receiveMessageResult.getMessages()) {
            messages.add(message);
            sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
        }

        return messages;
    }

}
