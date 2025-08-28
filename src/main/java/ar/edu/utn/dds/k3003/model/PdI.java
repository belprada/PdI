package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@Entity
public class PdI {
    @Id
    private String id;
    private String hecho;

    public PdI(String id, String hecho){
        this.id = id;
        this.hecho = hecho;
    }

    public PdI() {

    }
}
