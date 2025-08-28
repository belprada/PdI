package ar.edu.utn.dds.k3003.model.exceptions;

import ar.edu.utn.dds.k3003.model.SomeDomainObject;
import lombok.Getter;

@Getter
public class SomeDomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private SomeDomainObject anAttribute;

    public SomeDomainException(String message, SomeDomainObject anAttribute) {
        super(message);
        this.anAttribute = anAttribute;
    }
}
