package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.models.usuario.Administrador;
import com.ufcg.psoft.commerce.repositories.AdministradorRepository;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do AtivoController")
public class AtivoControllerTests {

    final String URI_BASE = "/ativos";
    final String URI_ATIVOS = "/ativos";
    final String CODIGO_ACESSO_VALIDO = "123456";
    final String CODIGO_ACESSO_INVALIDO = "000000";

    @Autowired
    MockMvc driver;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    AdministradorRepository administradorRepository;

    @Autowired
    TipoDeAtivoRepository tipoDeAtivoRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    List<Ativo> ativos;
    TesouroDireto tesouro;
    CriptoMoeda cripto;
    Acao acao;

    @BeforeEach
    void setup() {

        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();

        administradorRepository.save(Administrador.builder()
                .nome("Admin")
                .codigoAcesso("123456")
                .build());


        ativos = new ArrayList<>();
        objectMapper.registerModule(new JavaTimeModule());

        tesouro = tipoDeAtivoRepository.save(new TesouroDireto());
        cripto = tipoDeAtivoRepository.save(new CriptoMoeda());
        acao = tipoDeAtivoRepository.save(new Acao());

        // Criar alguns ativos de teste
        criarAtivo("Tesouro SELIC 2030", tesouro, 100.0, StatusDisponibilidade.DISPONIVEL);
        criarAtivo("Bitcoin", cripto, 50000.0, StatusDisponibilidade.DISPONIVEL);
        criarAtivo("PETR4", acao, 25.50, StatusDisponibilidade.DISPONIVEL);
    }

    private void criarAtivo(String nome, TipoDeAtivo tipo, double valor, StatusDisponibilidade status) {
        Ativo ativo = ativoRepository.save(Ativo.builder()
                .nome(nome)
                .tipo(tipo)
                .descricao("Descrição do " + nome)
                .valor(valor)
                .interessadosCotacao(new ArrayList<>())
                .interessadosDisponibilidade(new ArrayList<>())
                .statusDisponibilidade(status)
                .build()
        );
        ativos.add(ativo);
    }

    @AfterEach
    void tearDown() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();

    }

    @Nested
    @DisplayName("GET /ativos/{id} - Recuperar ativo")
    class RecuperarAtivo {

        @Test
        @DisplayName("Deve recuperar ativo existente com sucesso")
        void deveRecuperarAtivoExistente() throws Exception {
            Ativo ativo = ativos.get(0);

            String responseJsonString = driver.perform(
                            get(URI_ATIVOS + "/" + ativo.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO ativoRetornado = objectMapper.readValue(
                    responseJsonString, AtivoResponseDTO.class);

            assertEquals(ativo.getNome(), ativoRetornado.getNome());
            assertEquals(ativo.getValor(), ativoRetornado.getValor());
            assertEquals(ativo.getTipo().getNomeTipo(), TipoAtivo.TESOURO_DIRETO);
        }

        @Test
        @DisplayName("Deve retornar 400 para ativo inexistente")
        void deveRetornar400ParaAtivoInexistente() throws Exception {
            driver.perform(get(URI_ATIVOS + "/999999"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /ativos - Listar todos os ativos")
    class ListarAtivos {

        @Test
        @DisplayName("Deve listar todos os ativos")
        void deveListarTodosOsAtivos() throws Exception {
            String responseJsonString = driver.perform(get(URI_ATIVOS))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<AtivoResponseDTO> ativosRetornados = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<AtivoResponseDTO>>() {}
            );

            assertEquals(ativos.size(), ativosRetornados.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há ativos")
        void deveRetornarListaVaziaQuandoNaoHaAtivos() throws Exception {
            ativoRepository.deleteAll();

            String responseJsonString = driver.perform(get(URI_ATIVOS))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<AtivoResponseDTO> ativosRetornados = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<AtivoResponseDTO>>() {}
            );

            assertTrue(ativosRetornados.isEmpty());
        }
    }

}