package ar.edu.utn.dds.k3003.worker;

import com.rabbitmq.client.Channel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class QueueInitializer {

    private static final Logger log = Logger.getLogger(QueueInitializer.class.getName());

    public void ensureQueueExists(Channel channel, String queueName) throws IOException {
        try {
            // Try to check if it exists
            channel.queueDeclarePassive(queueName);
            log.info("Queue already exists: " + queueName);
        } catch (IOException e) {
            log.warning("Queue " + queueName + " does not exist, creating it...");
            channel.queueDeclare(queueName, true, false, false, null);
            log.info("Queue created: " + queueName);
        }
    }
}