package ar.edu.utn.dds.k3003.worker;

import ar.edu.utn.dds.k3003.app.Fachada;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class WorkerStarter {

    private static final Logger log = Logger.getLogger(WorkerStarter.class.getName());

    private Connection connection;

    @Autowired
    private Fachada ff;

    @Autowired
    private RabbitConnectionFactory connectionFactory;

    @Autowired
    private QueueInitializer queueInitializer;

    @PostConstruct
    public void startWorker() throws Exception {
        log.info("Starting worker");

        try {
            this.connection = connectionFactory.createConnection();
            Channel channel = connection.createChannel();

            //QUEUE NAME NEEDS TO BE CONFIGURED TO TAKE PDI NAME. ON RENDER OR IN WHATEVER.
            String queueName = System.getenv().getOrDefault("QUEUE_NAME", "hechos");
            queueInitializer.ensureQueueExists(channel, queueName);

            procesarWorker worker = new procesarWorker(channel, queueName, ff);
            worker.init();

            log.info("Worker initialized and consuming messages!");
        } catch (Exception e) {
            log.severe("Failed to start worker: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        if (connection != null && connection.isOpen()) connection.close();
    }
}