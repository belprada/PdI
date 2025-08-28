package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.config.JacksonConfig;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Import(JacksonConfig.class)
public class PdIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    public void listarColecciones_DeberiaRetornarListaVacia() throws Exception {
        mockMvc.perform(get("/api/PdIs"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @Order(2)
    public void crearPdI_DeberiaRetornarPdIcreados() throws Exception {
        PdIDTO nuevoPdI = new PdIDTO("testId", "hecho1");
        PdIDTO nuevoPdI2 = new PdIDTO("testId2", "hecho2");

        mockMvc.perform(post("/api/PdIs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoPdI)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testId"))
                .andExpect(jsonPath("$.hecho_id").value("hecho1"));

        mockMvc.perform(post("/api/PdIs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoPdI2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testId2"))
                .andExpect(jsonPath("$.hecho_id").value("hecho2"));

        // Verificar que el PdI fue creado
        mockMvc.perform(get("/api/PdIs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("testId"));

        mockMvc.perform(get("/api/PdIs")
                        .queryParam("hecho_id", "hecho1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hecho_id").value("hecho1"));
    }
    @Test
    @Order(3)
    public void listarPdIsCreadosAnteriormente() throws Exception {
        mockMvc.perform(get("/api/PdIs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("testId"))
                .andExpect(jsonPath("$[1].id").value("testId2"));
    }
}
