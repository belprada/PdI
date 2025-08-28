package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.tests.TestTP;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
public class FachadaTest implements TestTP<FachadaProcesadorPdI> {
    private static final String UN_HECHO_ID = "unHechoId";
    public static final PdIDTO PDI = new PdIDTO("1", "unHechoId", "una info", "bsas", LocalDateTime.now(), "1234556", List.of());
    private static final String PDI_ID = "unPDIid";
    private FachadaProcesadorPdI instancia;
    private PdIDTO pdi1;
    private PdIDTO pdi2;
    @Mock
    FachadaSolicitudes fachadaSolicitudes;

    @BeforeEach
    void setUp() throws Exception {
        try {
            this.instancia = (FachadaProcesadorPdI)this.instance();
            this.instancia.setFachadaSolicitudes(this.fachadaSolicitudes);
        } catch (Throwable $ex) {
            throw $ex;
        }
    }

    @Test
    @DisplayName("Procesar PdIs")
    void testProcesarPdI() {
        Mockito.when(this.fachadaSolicitudes.estaActivo("unHechoId")).thenReturn(true);
        PdIDTO pdi1 = this.instancia.procesar(PDI);
        this.instancia.procesar(PDI);
        Assertions.assertNotNull(pdi1.id(), "El PdI deberia tener un identificador no nulo");
        Assertions.assertEquals(pdi1.hechoId(), this.instancia.buscarPdIPorId(pdi1.id()).hechoId(), "No se esta recuperando el PdI correctamente");
        Assertions.assertEquals(1, this.instancia.buscarPorHecho("unHechoId").size(), "No se estan sumando correctamente los PdIs");
    }

    @Test
    @DisplayName("Procesar PdI que fue borrado")
    void testProcesarPdICerrado() {
        Mockito.when(this.fachadaSolicitudes.estaActivo("unHechoId")).thenReturn(false);
        Assertions.assertThrows(IllegalStateException.class, () -> this.instancia.procesar(PDI));
    }

    @Test
    @DisplayName("Buscar PdI por hecho")
    void testBuscarPdiPorHecho() {
        Mockito.when(this.fachadaSolicitudes.estaActivo("unHechoId")).thenReturn(true);
        this.instancia.procesar(PDI);
        Assertions.assertEquals(UN_HECHO_ID, instancia.buscarPorHecho(UN_HECHO_ID).get(0).hechoId());
    }

    @Test
    @DisplayName("Buscar PdI por ID")
    void testBuscarPdiPorID() {
        Mockito.when(this.fachadaSolicitudes.estaActivo("unHechoId")).thenReturn(true);
        this.instancia.procesar(PDI);
        Assertions.assertEquals(UN_HECHO_ID, instancia.buscarPdIPorId(PDI.id()).hechoId());
    }

    public String paquete() {
        return "ar.edu.utn.dds.k3003.tests.pdi";
    }

    public Class<FachadaProcesadorPdI> clase() {
        return FachadaProcesadorPdI.class;
    }
}
