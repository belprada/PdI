package ar.edu.utn.dds.k3003.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrResponseDTO {

    @JsonProperty("ParsedResults")
    private List<ParsedResult> parsedResults;

    @JsonProperty("OCRExitCode")
    private Integer ocrExitCode;

    @JsonProperty("IsErroredOnProcessing")
    private Boolean isErroredOnProcessing;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @JsonProperty("ErrorDetails")
    private String errorDetails;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedResult {

        @JsonProperty("TextOverlay")
        private TextOverlay textOverlay;

        @JsonProperty("TextOrientation")
        private String textOrientation;

        @JsonProperty("FileParseExitCode")
        private Integer fileParseExitCode;

        @JsonProperty("ParsedText")
        private String parsedText;

        @JsonProperty("ErrorMessage")
        private String errorMessage;

        @JsonProperty("ErrorDetails")
        private String errorDetails;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextOverlay {
        @JsonProperty("Lines")
        private List<Line> lines;

        @JsonProperty("HasOverlay")
        private Boolean hasOverlay;

        @JsonProperty("Message")
        private String message;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Line {
        @JsonProperty("LineText")
        private String lineText;

        @JsonProperty("Words")
        private List<Word> words;

        @JsonProperty("MaxHeight")
        private Integer maxHeight;

        @JsonProperty("MinTop")
        private Integer minTop;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Word {
        @JsonProperty("WordText")
        private String wordText;

        @JsonProperty("Left")
        private Integer left;

        @JsonProperty("Top")
        private Integer top;

        @JsonProperty("Height")
        private Integer height;

        @JsonProperty("Width")
        private Integer width;
    }
}