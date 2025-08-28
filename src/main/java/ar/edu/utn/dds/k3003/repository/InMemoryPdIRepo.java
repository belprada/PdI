package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.PdI;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Repository
@Profile("test")
public class InMemoryPdIRepo implements PdIRepository{
  private List<PdI> pdis;

  public InMemoryPdIRepo() {
    this.pdis = new ArrayList<>();
  }
  @Override
  public Optional<PdI> findById(String id) {
    return this.pdis.stream().filter(pdI -> pdI.getId().equals(id)).findFirst();
  }
  @Override
  public List<PdI> findByHecho(String hecho) {
    return this.pdis.stream().filter(pdI -> pdI.getHecho().equals(hecho)).toList();
  }
  @Override
  public List<PdI> findAll() {
    return this.pdis;
  }
  @Override
  public PdI save(PdI pdi) {
    this.pdis.add(pdi);
    return pdi;
  }
}
