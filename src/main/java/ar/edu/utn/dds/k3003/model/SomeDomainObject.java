package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.model.exceptions.SomeDomainException;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class SomeDomainObject {
    private String anAttribute;
    private Long otherAttribute;

    public SomeDomainObject sum(SomeDomainObject other) {
        if (!Objects.isNull(other.getAnAttribute())) {
            return new SomeDomainObject(
                    anAttribute + other.getAnAttribute(), otherAttribute + other.getOtherAttribute());
        }
        throw new SomeDomainException("anAttribute is null", other);
    }
}

