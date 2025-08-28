package ar.edu.utn.dds.k3003.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ar.edu.utn.dds.k3003.model.SomeDomainObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SomeDomainModelTest {

    SomeDomainObject someDomainObject1;

    SomeDomainObject someDomainObject2;

    @BeforeEach
    void setUp() {
        someDomainObject1 = new SomeDomainObject("a", 1L);

        someDomainObject2 = new SomeDomainObject("b", 2L);
    }

    @Test
    void testSum() {
        var result = someDomainObject1.sum(someDomainObject2);
        assertEquals("ab", result.getAnAttribute());
        assertEquals(3L, result.getOtherAttribute());
    }
}
