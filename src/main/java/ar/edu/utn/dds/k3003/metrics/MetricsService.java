package ar.edu.utn.dds.k3003.metrics;

import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetricsService {

    private final StatsDClient statsd;


    public MetricsService() {

        this.statsd = new NonBlockingStatsDClientBuilder()
                .prefix("pdi-service")     // prefijo de métricas
                .hostname("localhost")     // host del agente Datadog (Render debería mapearlo)
                .port(8125)                // puerto por defecto DogStatsD
                .build();
    }


    public void incrementSuccess() {
        log.info("Incrementando métrica de éxito");
        statsd.incrementCounter("pdi.success");
    }


    public void incrementError() {
        log.info("Incrementando métrica de error");
        statsd.incrementCounter("pdi.error");
    }


    public void recordErrorRatio(int errores, int exitos) {
        if (exitos == 0) {
            statsd.recordGaugeValue("pdi.error.ratio", 0.0);
            return;
        }
        double ratio = (double) errores / (double) exitos;
        log.info("Registrando ratio de errores: {}", ratio);
        statsd.recordGaugeValue("pdi.error.ratio", ratio);
    }
}
