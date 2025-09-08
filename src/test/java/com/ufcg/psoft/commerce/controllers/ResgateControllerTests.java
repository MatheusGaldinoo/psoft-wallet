package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.resgate.AtualizarStatusResgateDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
import com.ufcg.psoft.commerce.enums.DecisaoAdministrador;
import com.ufcg.psoft.commerce.enums.EstadoResgate;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.models.carteira.AtivoCarteira;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.usuario.Administrador;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.*;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc


@DisplayName("Testes do controlador de Resgate")
public class ResgateControllerTests {

    final String URI_CLIENTES = "/clientes";
    final String URI_RESGATES = "/resgates";
    final String CODIGO_ACESSO_VALIDO = "123456";

    @Autowired
    MockMvc driver;

    @Autowired
    ResgateRepository resgateRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    TipoDeAtivoRepository tipoDeAtivoRepository;

    @Autowired
    AdministradorRepository administradorRepository;

    @Autowired
    ModelMapper modelMapper;

    ObjectMapper objectMapper = new ObjectMapper();

    List<Ativo> ativos;
    List<Cliente> clientes;
    List<AtivoPostPutRequestDTO> ativosPostPutRequestDTO;

    TesouroDireto tesouro;
    CriptoMoeda cripto;
    Acao acao;

    @BeforeEach
    void setup() {
        resgateRepository.deleteAll();
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        clienteRepository.deleteAll();
        administradorRepository.deleteAll();

        clientes = new ArrayList<>();
        ativos = new ArrayList<>();
        ativosPostPutRequestDTO = new ArrayList<>();

        objectMapper.registerModule(new JavaTimeModule());

        administradorRepository.save(
                Administrador.builder()
                        .nome("Admin")
                        .codigoAcesso(CODIGO_ACESSO_VALIDO)
                        .build()
        );

        tesouro = tipoDeAtivoRepository.save(new TesouroDireto());
        cripto = tipoDeAtivoRepository.save(new CriptoMoeda());
        acao = tipoDeAtivoRepository.save(new Acao());

        for (int i = 1; i <= 3; i++) {
            criarAtivo("Ativo" + i, tesouro, 1.0 * i, StatusDisponibilidade.DISPONIVEL);
            criarCliente("Cliente" + i, (i % 2 == 0 ? TipoPlano.NORMAL : TipoPlano.PREMIUM));
        }
        for (int i = 4; i <= 7; i++) {
            criarAtivo("Ativo" + i, cripto, 1.0 * i, StatusDisponibilidade.DISPONIVEL);
        }
        for (int i = 8; i <= 10; i++) {
            criarAtivo("Ativo" + i, acao, 1.0 * i, StatusDisponibilidade.INDISPONIVEL);
        }
    }

    private void criarCliente(String nome, TipoPlano tipo) {
        Carteira carteira = Carteira.builder()
                .balanco(200.0)
                .build();

        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco("Avenida Paris, 5987, Campina Grande - PB")
                        .codigoAcesso(CODIGO_ACESSO_VALIDO)
                        .plano(tipo)
                        .carteira(carteira)
                        .build()
        );

        clientes.add(cliente);
    }

    private void criarAtivo(String nome, TipoDeAtivo tipo, double valor, StatusDisponibilidade status) {
        Ativo ativo = ativoRepository.save(
                Ativo.builder()
                        .nome(nome)
                        .descricao(nome) // obrigatório para não quebrar integridade
                        .tipo(tipo)
                        .valor(valor)
                        .statusDisponibilidade(status)
                        .interessadosCotacao(new ArrayList<>())
                        .interessadosDisponibilidade(new ArrayList<>())
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

    private void inicializarCarteiraComAtivo(Cliente cliente, Ativo ativo, double quantidade) {
        if (cliente.getCarteira().getAtivos() == null) {
            cliente.getCarteira().setAtivos(new HashMap<>());
        }

        AtivoCarteira ativoCarteira = AtivoCarteira.builder()
                .quantidade(quantidade)
                .valorAcumulado(ativo.getValor())
                .quantidadeAcumulada(quantidade)
                .build();

        cliente.getCarteira().getAtivos().put(ativo.getId(), ativoCarteira);
        clienteRepository.save(cliente);
    }

    @AfterEach
    void tearDown() {
        resgateRepository.deleteAll();
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        clienteRepository.deleteAll();
        administradorRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /clientes/{idCliente}/resgates - Cliente acompanha resgates")
    class AcompanharResgates {

        @Test
        @DisplayName("Cliente sem resgates")
        void clienteSemResgates() throws Exception {
            Cliente cliente = clientes.get(0);

            String response = driver.perform(
                            get(URI_CLIENTES + "/" + cliente.getId() + "/resgates"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<ResgateResponseDTO> resgates = objectMapper.readValue(
                    response,
                    new TypeReference<List<ResgateResponseDTO>>() {}
            );

            assertEquals(0, resgates.size());
        }

        @Test
        @DisplayName("Cliente consulta resgate inexistente retorna 404")
        void consultaResgateInexistente() throws Exception {
            driver.perform(get(URI_RESGATES + "/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Cliente com resgate solicitado")
        void clienteComResgateSolicitado() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(2)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());

            String response = driver.perform(get(URI_CLIENTES + "/" + cliente.getId() + "/resgates"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<ResgateResponseDTO> resgates = objectMapper.readValue(
                    response,
                    new TypeReference<List<ResgateResponseDTO>>() {}
            );

            assertEquals(1, resgates.size());
            ResgateResponseDTO resgate = resgates.get(0);

            assertEquals(cliente.getId(), resgate.getIdCliente());
            assertEquals(ativo.getId(), resgate.getIdAtivo());
            assertEquals(2, resgate.getQuantidade());
            assertEquals(EstadoResgate.SOLICITADO, resgate.getEstado());
            assertNotNull(resgate.getDataSolicitacao());
            assertEquals(0.16, resgate.getImposto(), 0.0001);
        }

        @Test
        @DisplayName("Admin aprova resgate")
        void adminAprovaResgate() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(2)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            String resgateJson = driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ResgateResponseDTO resgate = objectMapper.readValue(resgateJson, ResgateResponseDTO.class);

            AtualizarStatusResgateDTO aprovacao = AtualizarStatusResgateDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(patch(URI_RESGATES + "/" + resgate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(aprovacaoJson))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.estado_atual").value(EstadoResgate.CONFIRMADO.name()));
        }

        @Test
        @DisplayName("Admin rejeita resgate")
        void adminRejeitaResgate() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(2)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            String resgateJson = driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ResgateResponseDTO resgate = objectMapper.readValue(resgateJson, ResgateResponseDTO.class);

            AtualizarStatusResgateDTO rejeicao = AtualizarStatusResgateDTO.builder()
                    .estado(DecisaoAdministrador.RECUSADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String rejeicaoJson = objectMapper.writeValueAsString(rejeicao);

            driver.perform(patch(URI_RESGATES + "/" + resgate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(rejeicaoJson))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Cliente consulta resgate específico")
        void clienteConsultaResgate() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(3)
                    .build();

            String json = objectMapper.writeValueAsString(dto);

            String resgateJson = driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ResgateResponseDTO resgate = objectMapper.readValue(resgateJson, ResgateResponseDTO.class);

            String consultaJson = driver.perform(get(URI_RESGATES + "/" + resgate.getId()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            ResgateResponseDTO resgateConsultado = objectMapper.readValue(consultaJson, ResgateResponseDTO.class);

            assertEquals(resgate.getId(), resgateConsultado.getId());
            assertEquals(resgate.getIdCliente(), resgateConsultado.getIdCliente());
            assertEquals(resgate.getEstado(), resgateConsultado.getEstado());
            assertEquals(resgate.getQuantidade(), resgateConsultado.getQuantidade());
            assertEquals(resgate.getIdAtivo(), resgateConsultado.getIdAtivo());
        }

        @Test
        @DisplayName("Fluxo completo: Solicitar → Aprovar → Executar → Em conta")
        void fluxoCompletoResgate() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(2)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            String resgateJson = driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ResgateResponseDTO resgate = objectMapper.readValue(resgateJson, ResgateResponseDTO.class);

            AtualizarStatusResgateDTO aprovacao = AtualizarStatusResgateDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();
            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(patch(URI_RESGATES + "/" + resgate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(aprovacaoJson))
                    .andExpect(status().isOk());

            driver.perform(post(URI_RESGATES + "/" + resgate.getId() + "/executar")
                            .param("idCliente", String.valueOf(cliente.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado_atual").value(EstadoResgate.EM_CONTA.name()))
                    .andExpect(jsonPath("$.data_finalizacao").exists());
        }


        @Test
        @DisplayName("Tentativa de executar resgate de outro cliente lança exceção")
        void executarResgateClienteErrado() throws Exception {
            Cliente cliente1 = clientes.get(0);
            Cliente cliente2 = clientes.get(1);
            Ativo ativo = ativos.get(0);

            inicializarCarteiraComAtivo(cliente1, ativo, 5);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            String resgateJson = driver.perform(post(URI_CLIENTES + "/" + cliente1.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ResgateResponseDTO resgate = objectMapper.readValue(resgateJson, ResgateResponseDTO.class);

            AtualizarStatusResgateDTO aprovacao = AtualizarStatusResgateDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();
            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(patch(URI_RESGATES + "/" + resgate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(aprovacaoJson))
                    .andExpect(status().isOk());

            driver.perform(post(URI_RESGATES + "/" + resgate.getId() + "/executar")
                            .param("idCliente", String.valueOf(cliente2.getId())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Tentativa de executar resgate não confirmado lança exceção")
        void executarResgateNaoConfirmado() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            inicializarCarteiraComAtivo(cliente, ativo, 5);

            ResgatePostPutRequestDTO dto = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            String resgateJson = driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            ResgateResponseDTO resgate = objectMapper.readValue(resgateJson, ResgateResponseDTO.class);

            driver.perform(post(URI_RESGATES + "/" + resgate.getId() + "/executar")
                            .param("idCliente", String.valueOf(cliente.getId())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Cliente lista múltiplos resgates com diferentes estados")
        void listarMultiplosResgates() throws Exception {
            Cliente cliente = clientes.get(0);
            Ativo ativo1 = ativos.get(0);
            Ativo ativo2 = ativos.get(1);

            inicializarCarteiraComAtivo(cliente, ativo1, 5);
            inicializarCarteiraComAtivo(cliente, ativo2, 5);

            ResgatePostPutRequestDTO dto1 = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo1.getId())
                    .quantidade(1)
                    .build();
            driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto1)))
                    .andExpect(status().isCreated());

            ResgatePostPutRequestDTO dto2 = ResgatePostPutRequestDTO.builder()
                    .idAtivo(ativo2.getId())
                    .quantidade(2)
                    .build();
            driver.perform(post(URI_CLIENTES + "/" + cliente.getId() + "/resgate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto2)))
                    .andExpect(status().isCreated());

            String response = driver.perform(get(URI_CLIENTES + "/" + cliente.getId() + "/resgates"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<ResgateResponseDTO> resgates = objectMapper.readValue(
                    response,
                    new TypeReference<List<ResgateResponseDTO>>() {}
            );

            assertEquals(2, resgates.size());
            assertEquals(EstadoResgate.SOLICITADO, resgates.get(0).getEstado());
            assertEquals(EstadoResgate.SOLICITADO, resgates.get(1).getEstado());
        }
    }
}