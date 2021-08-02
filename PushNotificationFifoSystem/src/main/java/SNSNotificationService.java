package service;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;

public class SNSNotificationService {
    static final LambdaLogger logger = getLogger();
    private final static SnsClient snsClient;
    static {
        snsClient = SnsClient.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .httpClient(ApacheHttpClient.builder().build())
                .build();
    }

    private static boolean pubFifoTopic(String message, String topicArn) {
        logger.log("SNSNotificationService.pubTopic - Start "+ topicArn);
        try {
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .messageStructure("json")
                    .message(message).build();
            logger.log("SNSNotificationService.pubTopic - Sending" );
            PublishResponse result = snsClient.publish(request);
            logger.log(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());
            Consumer<ListSubscriptionsByTopicRequest.Builder> listSubscriptionsByTopicRequest = builder -> builder.topicArn(topicArn);
            int msg_qty = snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest).subscriptions().size();
            logger.log("Topic pushMsg_QTY: " + msg_qty);

            HashMap<String, Object> response = new HashMap<>();
            response.put("msg_id", result.messageId());
            response.put("msg_qty", msg_qty);
            return true;
        } catch (SnsException e) {
            logger.log("\npubTopic Error: " + e.getMessage() + "\n");
            return false;
        }
    }

}
