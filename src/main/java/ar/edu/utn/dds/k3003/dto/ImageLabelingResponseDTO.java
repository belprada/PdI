package ar.edu.utn.dds.k3003.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageLabelingResponseDTO {
    @JsonProperty("label")
    private String label;

    @JsonProperty("confidence")
    private double confidence;
}