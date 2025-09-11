package ar.edu.utn.dds.k3003.rest_client;

import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class FuenteRestClient {

    private final RestClient restClient;

    public FuenteRestClient() {
        var env = System.getenv();
        String endpoint = env.getOrDefault("DDS_FUENTE", "http://localhost:8082");

        this.restClient = RestClient.builder()
                .baseUrl(endpoint)
                .build();
    }

    public HechoDTO findHechoById(String id) {
        return restClient.get()
                .uri("/api/hecho/{id}", id)
                .retrieve()
                .body(HechoDTO.class);
    }

}
