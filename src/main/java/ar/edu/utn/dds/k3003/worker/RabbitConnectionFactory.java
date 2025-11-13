package ar.edu.utn.dds.k3003.worker;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Logger;

@Component
public class RabbitConnectionFactory {

    private static final Logger log = Logger.getLogger(RabbitConnectionFactory.class.getName());

    public Connection createConnection() throws Exception {
        Map<String, String> env = System.getenv();
        ConnectionFactory factory = new ConnectionFactory();

        if (env.containsKey("CLOUDAMQP_URL")) {
            factory.setUri(env.get("CLOUDAMQP_URL"));
        } else {
            factory.setHost(env.get("QUEUE_HOST"));
            factory.setUsername(env.get("QUEUE_USERNAME"));
            factory.setPassword(env.get("QUEUE_PASSWORD"));
            factory.setVirtualHost(env.getOrDefault("QUEUE_VHOST", "/"));
        }

        factory.setAutomaticRecoveryEnabled(true);
        return factory.newConnection();
    }
}