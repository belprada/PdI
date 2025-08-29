package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.client.SolicitudesProxy;
import ar.edu.utn.dds.k3003.dto.PdIDTO2;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class Fachada {

  private final PdIRepository pdiRepository;
  private Integer pdiID = 0;
  @Setter
  private FachadaSolicitudes fachadaSolicitudes;
  private ObjectMapper objectMapper;

  public Fachada(PdIRepository pdiRepository) {
    this.pdiRepository = pdiRepository;
    this.objectMapper = new ObjectMapper();
    this.fachadaSolicitudes = new SolicitudesProxy(objectMapper);
  }

  public PdIDTO2 procesar(PdIDTO2 var1) throws IllegalStateException {

    if(fachadaSolicitudes.estaActivo(var1.getHechoId())) {
      if(buscarPorHecho(var1.getHechoId()).isEmpty()) {
        PdI pdiNuevo = new PdI(var1.getId(), var1.getHechoId());
        //pdiID++;
        this.pdiRepository.save(pdiNuevo);
        return new PdIDTO2(pdiNuevo.getId(), pdiNuevo.getHecho());
      }
      else {
        return buscarPorHecho(var1.getHechoId()).get(0);
      }
    } else {
      throw new IllegalStateException("No esta activo");
    }
  }


  public PdIDTO2 buscarPdIPorId(String var1) throws NoSuchElementException {
    Optional<PdI> pdIOptional = this.pdiRepository.findById(var1);
    if (pdIOptional.isEmpty()) {
      throw new NoSuchElementException(pdIOptional + " no existe");
    }

    PdI pdi = pdIOptional.get();

    return new PdIDTO2(pdi.getId(), pdi.getHecho());
  }


  public List<PdIDTO2> buscarPorHecho(String var1) throws NoSuchElementException {

    List<PdI> pdIList = this.pdiRepository.findByHecho(var1);

    return pdIList.stream().map(pdi -> new PdIDTO2(pdi.getId(), pdi.getHecho())).toList();
  }

  public List<PdIDTO2> buscarTodos() {
    List<PdI> pdIList = this.pdiRepository.findAll();

    return pdIList.stream().map(pdi -> new PdIDTO2(pdi.getId(), pdi.getHecho())).toList();

  }


}