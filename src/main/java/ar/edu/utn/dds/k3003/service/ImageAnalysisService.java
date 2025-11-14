package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.service.analysis.ImageAnalysisStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ImageAnalysisService {

    private final List<ImageAnalysisStrategy> strategies;

    public ImageAnalysisService(List<ImageAnalysisStrategy> strategies) {
        this.strategies = strategies;
    }

    public ImageAnalysisResult processImage(String imageUrl) {
        log.info("Iniciando análisis completo de imagen: {}", imageUrl);

        String ocrText = "";
        List<String> labels = new ArrayList<>();

        for (ImageAnalysisStrategy strategy : strategies) {
            ImageAnalysisResult result = strategy.analyze(imageUrl);
            if (result.ocrText() != null && !result.ocrText().isEmpty())
                ocrText = result.ocrText();
            if (result.labels() != null)
                labels.addAll(result.labels());
        }

        return new ImageAnalysisResult(ocrText, labels);
    }


        public record ImageAnalysisResult(String ocrText, List<String> labels) {

    }
}
