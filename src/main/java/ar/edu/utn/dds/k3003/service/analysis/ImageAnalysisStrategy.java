package ar.edu.utn.dds.k3003.service.analysis;

import ar.edu.utn.dds.k3003.service.ImageAnalysisService;

public interface ImageAnalysisStrategy {
    ImageAnalysisService.ImageAnalysisResult analyze(String imageUrl);
}
