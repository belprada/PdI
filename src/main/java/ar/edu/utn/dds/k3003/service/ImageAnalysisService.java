package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.dto.ImageLabelingResponseDTO;
import ar.edu.utn.dds.k3003.dto.OcrResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageAnalysisService {

    private final RestTemplate restTemplate;

    @Value("${ocr.api.key}")
    private String ocrApiKey;

    @Value("${image.labeling.api.key}")
    private String imageLabelingApiKey;

    @Value("${ocr.api.url:https://api.ocr.space/parse/imageurl}")
    private String ocrApiUrl;

    @Value("${image.labeling.api.url:https://api.apilayer.com/image_labeling/url}")
    private String imageLabelingApiUrl;

    public ImageAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Limpia la URL de la imagen removiendo parámetros problemáticos
     */
    private String cleanImageUrl(String imageUrl) {
        try {
            // Si es una URL de Pexels, limpiarla
            if (imageUrl.contains("pexels.com")) {
                // Extraer solo la parte hasta el primer '?'
                int queryIndex = imageUrl.indexOf('?');
                if (queryIndex > 0) {
                    String cleanUrl = imageUrl.substring(0, queryIndex);
                    log.info("URL limpiada de {} a {}", imageUrl, cleanUrl);
                    return cleanUrl;
                }
            }

            // Para otras URLs, remover parámetros problemáticos comunes
            String cleaned = imageUrl.replaceAll("[&?]_gl=[^&]*", "")
                    .replaceAll("[&?]utm_[^&]*=[^&]*", "");

            if (!cleaned.equals(imageUrl)) {
                log.info("URL limpiada de {} a {}", imageUrl, cleaned);
            }

            return cleaned;
        } catch (Exception e) {
            log.warn("Error limpiando URL {}: {}", imageUrl, e.getMessage());
            return imageUrl;
        }
    }

    /**
     * Procesa una imagen usando OCR para extraer texto
     */
    public String processOCR(String imageUrl) {
        try {
            String cleanUrl = cleanImageUrl(imageUrl);
            log.info("Procesando OCR para imagen: {}", cleanUrl);

            String url = UriComponentsBuilder
                    .fromHttpUrl(ocrApiUrl)
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
                    url,
                    HttpMethod.GET,
                    entity,
                    OcrResponseDTO.class
            );

            if (response.getBody() != null) {
                if (Boolean.TRUE.equals(response.getBody().getIsErroredOnProcessing())) {
                    log.error("Error en OCR API: {}", response.getBody().getErrorMessage());
                    return "";
                }

                if (response.getBody().getParsedResults() != null &&
                        !response.getBody().getParsedResults().isEmpty()) {

                    String extractedText = response.getBody()
                            .getParsedResults()
                            .get(0)
                            .getParsedText();

                    log.info("Texto extraído exitosamente de la imagen");
                    return extractedText != null ? extractedText.trim() : "";
                }
            }

            log.warn("No se pudo extraer texto de la imagen");
            return "";

        } catch (Exception e) {
            log.error("Error procesando OCR para imagen {}: {}", imageUrl, e.getMessage());
            return "";
        }
    }

    /**
     * Procesa una imagen para obtener etiquetas/labels
     */
    public List<String> processImageLabeling(String imageUrl) {
        try {
            String cleanUrl = cleanImageUrl(imageUrl);
            log.info("Procesando etiquetado para imagen: {}", cleanUrl);

            String fullUrl = UriComponentsBuilder
                    .fromHttpUrl(imageLabelingApiUrl)
                    .queryParam("url", cleanUrl)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", imageLabelingApiKey);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ImageLabelingResponseDTO>> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<ImageLabelingResponseDTO>>() {}
            );

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                List<String> labels = response.getBody().stream()
                        .filter(label -> label.getConfidence() > 0.5)
                        .map(ImageLabelingResponseDTO::getLabel)
                        .collect(Collectors.toList());
                log.info("Labels extracted: {}", labels);
                return labels;
            } else {
                log.info("No labels found in response");
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String body = ex.getResponseBodyAsString();
            if (body != null && body.contains("message")) {
                log.warn("Image labeling API message: {}", body);
            } else {
                log.error("Image labeling API error: {}", ex.getMessage());
            }
            return Collections.emptyList();
        }
    }

    /**
     * Procesa una imagen completa: OCR + Etiquetado
     */
    public ImageAnalysisResult processImage(String imageUrl) {
        log.info("Iniciando análisis completo de imagen: {}", imageUrl);

        String ocrText = processOCR(imageUrl);
        List<String> labels = processImageLabeling(imageUrl);

        return new ImageAnalysisResult(ocrText, labels);
    }

    /**
     * Método para probar URLs de imagen válidas
     */
    public boolean isImageUrlValid(String imageUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    cleanImageUrl(imageUrl),
                    HttpMethod.HEAD,
                    entity,
                    byte[].class
            );

            String contentType = response.getHeaders().getContentType() != null ?
                    response.getHeaders().getContentType().toString() : "";

            boolean isValid = response.getStatusCode().is2xxSuccessful() &&
                    contentType.startsWith("image/");

            log.info("URL {} es válida: {}", imageUrl, isValid);
            return isValid;

        } catch (Exception e) {
            log.warn("URL {} no es válida: {}", imageUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Clase para encapsular el resultado del análisis de imagen
     */
    public static class ImageAnalysisResult {
        private final String ocrText;
        private final List<String> labels;

        public ImageAnalysisResult(String ocrText, List<String> labels) {
            this.ocrText = ocrText;
            this.labels = labels;
        }

        public String getOcrText() {
            return ocrText;
        }

        public List<String> getLabels() {
            return labels;
        }
    }
}