package ar.edu.utn.dds.k3003.service.analysis;

import ar.edu.utn.dds.k3003.dto.ImageLabelingResponseDTO;
import ar.edu.utn.dds.k3003.service.ImageAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageLabelingStrategy implements ImageAnalysisStrategy {

    private final RestTemplate restTemplate;

    @Value("${image.labeling.api.key}")
    private String apiKey;

    @Value("${image.labeling.api.url}")
    private String apiUrl;

    public ImageLabelingStrategy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ImageAnalysisService.ImageAnalysisResult analyze(String imageUrl) {
        try {
            String cleanUrl = cleanImageUrl(imageUrl);
            log.info("Procesando etiquetado para imagen: {}", cleanUrl);

            String fullUrl = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("url", cleanUrl)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ImageLabelingResponseDTO>> response = restTemplate.exchange(
                    fullUrl, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<>() {}
            );

            List<String> labels = response.getBody() != null
                    ? response.getBody().stream()
                    .filter(l -> l.getConfidence() > 0.5)
                    .map(ImageLabelingResponseDTO::getLabel)
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            return new ImageAnalysisService.ImageAnalysisResult("", labels);

        } catch (Exception e) {
            log.error("Error procesando etiquetado para imagen {}: {}", imageUrl, e.getMessage());
            return new ImageAnalysisService.ImageAnalysisResult("", Collections.emptyList());
        }
    }

    private String cleanImageUrl(String imageUrl) {
        // Misma lógica de limpieza que en OCR
        try {
            if (imageUrl.contains(".com")) {
                int queryIndex = imageUrl.indexOf('?');
                if (queryIndex > 0) return imageUrl.substring(0, queryIndex);
            }
            return imageUrl.replaceAll("[&?]_gl=[^&]*", "")
                    .replaceAll("[&?]utm_[^&]*=[^&]*", "");
        } catch (Exception e) {
            log.warn("Error limpiando URL {}: {}", imageUrl, e.getMessage());
            return imageUrl;
        }
    }
}
