package ar.edu.utn.dds.k3003.mappers;

import ar.edu.utn.dds.k3003.dto.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PdIMapper {

    public PdI toEntity(PdIDTO dto) {
        if (dto == null) {
            return null;
        }

        PdI pdi;
        if (dto.imagenUrl() != null && !dto.imagenUrl().isBlank()) {
            // Constructor para PdI con imagen
            pdi = new PdI(
                    dto.hechoId(),
                    dto.descripcion(),
                    dto.lugar(),
                    dto.momento(),
                    dto.contenido(),
                    dto.imagenUrl()
            );
        } else {
            // Constructor para PdI con etiquetas
            pdi = new PdI(
                    dto.hechoId(),
                    dto.descripcion(),
                    dto.lugar(),
                    dto.momento(),
                    dto.contenido(),
                    dto.etiquetas()
            );
        }

        return pdi;
    }

    public PdIDTO toDto(PdI entity) {
        if (entity == null) {
            return null;
        }
        return PdIDTO.fromProcessedEntity(entity);
    }

    public void updateEntityFromDto(PdIDTO dto, PdI entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setHechoId(dto.hechoId());
        entity.setDescripcion(dto.descripcion());
        entity.setLugar(dto.lugar());
        entity.setMomento(dto.momento());
        entity.setContenido(dto.contenido());
        entity.setImagenUrl(dto.imagenUrl());

        // Si cambia la imagen, se limpia el procesamiento
        if (dto.imagenUrl() != null && !dto.imagenUrl().equals(entity.getImagenUrl())) {
            entity.setEstadoProcesamiento(PdI.EstadoProcesamiento.PENDIENTE);
            entity.setOcrText(null);
            entity.setEtiquetasIA(null);
            entity.setFechaProcesamiento((LocalDateTime) null);
        }
    }
}
