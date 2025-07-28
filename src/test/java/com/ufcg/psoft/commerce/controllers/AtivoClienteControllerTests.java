package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.models.*;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.repositories.ClienteRepository;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Ativos")
public class AtivoClienteControllerTests {

    final String URI_CLIENTES = "/clientes";

    @Autowired
    MockMvc driver;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    TipoDeAtivoRepository tipoDeAtivoRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    List<Ativo> ativos;
    List<AtivoPostPutRequestDTO> ativosPostPutRequestDTO;

    TesouroDireto tesouro;
    CriptoMoeda cripto;
    Acao acao;

    @BeforeEach
    void setup() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();

        ativos = new ArrayList<>();
        ativosPostPutRequestDTO = new ArrayList<>();
        objectMapper.registerModule(new JavaTimeModule());

        tesouro = tipoDeAtivoRepository.save(new TesouroDireto());
        cripto = tipoDeAtivoRepository.save(new CriptoMoeda());
        acao = tipoDeAtivoRepository.save(new Acao());

        for (int i = 1; i <= 3; i++) {
            criarAtivo("Ativo" + i, tesouro, 1.0 * i);
            criarCliente("Cliente" + i, (i%2 == 0 ? TipoPlano.NORMAL : TipoPlano.PREMIUM));
        }
        for (int i = 4; i <= 7; i++) {
            criarAtivo("Ativo" + i, cripto, 1.0 * i);
        }
        for (int i = 8; i <= 10; i++) {
            criarAtivo("Ativo" + i, acao, 1.0 * i);
        }

    }

    private void criarCliente(String nome, TipoPlano tipo) {
        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco("Avenida Paris, 5987, Campina Grande - PB")
                        .codigoAcesso("12345")
                        .plano(tipo)
                        .build());
    }

    private void criarAtivo(String nome, TipoDeAtivo tipo, double valor) {
        Ativo ativo = ativoRepository.save(Ativo.builder()
                .nome(nome)
                .tipo(tipo)
                .descricao(nome)
                .valor(valor)
                .interessados(new ArrayList<>())
                .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                .build()
        );
        ativos.add(ativo);

        AtivoPostPutRequestDTO dto = AtivoPostPutRequestDTO.builder()
                .nome(ativo.getNome())
                .descricao(ativo.getDescricao())
                .valor(ativo.getValor())
                .tipo(ativo.getTipo().getNomeTipo())
                .statusDisponibilidade(ativo.getStatusDisponibilidade())
                .build();
        ativosPostPutRequestDTO.add(dto);
    }

    @AfterEach
    void tearDown() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação de nome")
    class AtivoListagem {

        @Test
        @DisplayName("Quando listamos todos os ativos para conta Premium")
        void quandoListamosTodosOsAtivosParaContraPremium() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            String responseJsonString = driver.perform(
                            get(URI_CLIENTES + "/" + cliente.getId() + "/ativos-disponiveis"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<AtivoResponseDTO> ativosRetornados = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<AtivoResponseDTO>>() {}
            );

            assertEquals(ativos.size(), ativosRetornados.size());

            List<String> nomesEsperados = ativos.stream().map(Ativo::getNome).toList();
            List<String> nomesRetornados = ativosRetornados.stream().map(AtivoResponseDTO::getNome).toList();

            assertTrue(nomesRetornados.containsAll(nomesEsperados));
        }

        @Test
        @DisplayName("Quando listamos ativos para Conta Normal")
        void quandoListamosTodosOsAtivosParaContaNormal() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente2").get(0);
            String responseJsonString = driver.perform(
                            get(URI_CLIENTES + "/" + cliente.getId() + "/ativos-disponiveis"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<AtivoResponseDTO> ativosRetornados = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<AtivoResponseDTO>>() {}
            );

            assertEquals(3, ativosRetornados.size());

            List<String> nomesEsperados = ativos.stream()
                    .filter((ativo) -> ativo.getTipo().getNomeTipo() == TipoAtivo.TESOURO_DIRETO)
                    .map(Ativo::getNome).toList();

            List<String> nomesRetornados = ativosRetornados.stream().map(AtivoResponseDTO::getNome).toList();

            assertTrue(nomesRetornados.containsAll(nomesEsperados));
        }
    }
}