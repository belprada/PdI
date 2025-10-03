package ar.edu.utn.dds.k3003.service.analysis;

import ar.edu.utn.dds.k3003.dto.OcrResponseDTO;
import ar.edu.utn.dds.k3003.service.ImageAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
@Slf4j
public class OcrAnalysisStrategy implements ImageAnalysisStrategy {

    private final RestTemplate restTemplate;

    @Value("${ocr.api.key}")
    private String ocrApiKey;

    @Value("${ocr.api.url}")
    private String ocrApiUrl;

    public OcrAnalysisStrategy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ImageAnalysisService.ImageAnalysisResult analyze(String imageUrl) {
        try {
            String cleanUrl = cleanImageUrl(imageUrl);
            log.info("Procesando OCR para imagen: {}", cleanUrl);

            String url = UriComponentsBuilder.fromHttpUrl(ocrApiUrl)
                    .queryParam("apikey", ocrApiKey)
                    .queryParam("url", cleanUrl)
                    .queryParam("language", "eng")
                    .queryParam("isOverlayRequired", "false")
                    .queryParam("scale", "true")
                    .queryParam("OCREngine", "2")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<OcrResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OcrResponseDTO.class
            );

            if (response.getBody() != null && response.getBody().getParsedResults() != null
                    && !response.getBody().getParsedResults().isEmpty()) {

                String extractedText = response.getBody().getParsedResults().get(0).getParsedText();
                return new ImageAnalysisService.ImageAnalysisResult(
                        extractedText != null ? extractedText.trim() : "",
                        null
                );
            }

        } catch (Exception e) {
            log.error("Error procesando OCR para imagen {}: {}", imageUrl, e.getMessage());
        }
        return new ImageAnalysisService.ImageAnalysisResult("", null);
    }

    private String cleanImageUrl(String imageUrl) {
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
