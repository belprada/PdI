package ar.edu.utn.dds.k3003.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class PdIDTO2 {
    private String id;
    private String hechoId;
    private String descripcion;
    private String lugar;
    private LocalDateTime momento;
    private String contenido;
    private List<String> etiquetas;

    public PdIDTO2(String id, String hecho) {
        this.id = id;
        this.hechoId = hecho;
    }

}