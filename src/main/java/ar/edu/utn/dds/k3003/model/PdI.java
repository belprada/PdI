package ar.edu.utn.dds.k3003.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hechoId;
    private String descripcion;
    private String lugar;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime momento;

    private String contenido;

    @Column(name = "imagen_url")
    private String imagenUrl;

    // Resultado del análisis OCR
    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    // Etiquetas generadas por IA
    @ElementCollection
    @CollectionTable(name = "pdi_etiquetas_ia", joinColumns = @JoinColumn(name = "pdi_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetasIA;

    // Campo deprecado - mantenido por compatibilidad
    @ElementCollection
    @CollectionTable(name = "pdi_etiquetas_legacy", joinColumns = @JoinColumn(name = "pdi_id"))
    @Column(name = "etiqueta")
    @Deprecated
    private List<String> etiquetas;

    // Estado del procesamiento
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_procesamiento")
    private EstadoProcesamiento estadoProcesamiento;

    // Timestamps de procesamiento
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    public PdI(String hechoId, String descripcion, String lugar, LocalDateTime momento,
               String contenido, List<String> etiquetas) {
        this.hechoId = hechoId;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.momento = momento;
        this.contenido = contenido;
        this.etiquetas = etiquetas;
        this.estadoProcesamiento = EstadoProcesamiento.PENDIENTE;
    }

    public PdI(String hechoId, String descripcion, String lugar, LocalDateTime momento,
               String contenido, String imagenUrl) {
        this.hechoId = hechoId;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.momento = momento;
        this.contenido = contenido;
        this.imagenUrl = imagenUrl;
        this.estadoProcesamiento = EstadoProcesamiento.PENDIENTE;
    }

    public enum EstadoProcesamiento {
        PENDIENTE,
        PROCESANDO,
        COMPLETADO,
        ERROR
    }
}