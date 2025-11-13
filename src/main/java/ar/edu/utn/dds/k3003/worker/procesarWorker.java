package ar.edu.utn.dds.k3003.worker;
import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dto.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class procesarWorker extends DefaultConsumer {



    private final String queueName;
    private final Fachada ff;


    private static final Logger log = Logger.getLogger(String.valueOf(procesarWorker.class));

    protected procesarWorker(Channel channel, String queueName, Fachada ff) {
        super(channel);
        this.queueName = queueName;
        this.ff = ff;
    }

    public void init() throws IOException {

        /*this.getChannel().queueDeclare(queueName, false, false, false, null);
        log.info("queue Declared");*/

        //this.getChannel().queueBind(queueName, queueName, "");

        this.getChannel().basicConsume(queueName, false, this);
        log.info("basic consumer initialized");
    }



    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) throws IOException {

        //var em = emf.createEntityManager();

        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            log.info("Attempting to process message");

            String json = new String(body, StandardCharsets.UTF_8);
            PdIDTO dto = mapper.readValue(json, PdIDTO.class);


            //em.getTransaction().begin();

            ff.procesar(dto);

            //em.getTransaction().commit();

            log.info("message processed and done");
            this.getChannel().basicAck(envelope.getDeliveryTag(), false);
        } catch (Exception e) {

            log.info("error processing message: " + e.toString());
            this.getChannel().basicNack(envelope.getDeliveryTag(), false, false);
        }


    }

}