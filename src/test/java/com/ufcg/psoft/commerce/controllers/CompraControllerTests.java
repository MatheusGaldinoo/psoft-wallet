package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.enums.EstadoCompra;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.models.carteira.Carteira;
import com.ufcg.psoft.commerce.models.transacao.Compra;
import com.ufcg.psoft.commerce.models.usuario.Administrador;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.*;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Compra")
public class CompraControllerTests {

    final String URI_COMPRAS_CLIENTES = "/clientes";

    final String URI_COMPRAS = "/compras";
    final String CODIGO_ACESSO_VALIDO = "123456";
    final String CODIGO_ACESSO_INVALIDO = "000000";

    @Autowired
    MockMvc driver;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    ClienteRepository clienteRepository;

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
    private AtivoService ativoService;

    @BeforeEach
    void setup() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();

        ativos = new ArrayList<>();
        clientes = new ArrayList<>();
        ativosPostPutRequestDTO = new ArrayList<>();
        objectMapper.registerModule(new JavaTimeModule());

        administradorRepository.save(Administrador.builder()
                .nome("Admin")
                .codigoAcesso("123456")
                .build());


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
                .balanco(200.00)
                .build();

        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco("Avenida Paris, 5987, Campina Grande - PB")
                        .codigoAcesso("123456")
                        .plano(tipo)
                        .carteira(carteira)
                        .build());
        clientes.add(cliente);
    }

    private void criarAtivo(String nome, TipoDeAtivo tipo, double valor, StatusDisponibilidade status) {
        Ativo ativo = ativoRepository.save(Ativo.builder()
                .nome(nome)
                .tipo(tipo)
                .descricao(nome)
                .valor(valor)
                .interessadosCotacao(new ArrayList<>())
                .interessadosDisponibilidade(new ArrayList<>())
                .statusDisponibilidade(status)
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
        administradorRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET/compras - Acompanhar Status das compras")
    class AcompanharStatusCompras {

        @Test
        @DisplayName("Cliente sem compras realizadas")
        void clienteSemCompras() throws Exception {

            Cliente cliente = clientes.get(0);

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/acompanhar-status"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<CompraResponseDTO> comprasRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<CompraResponseDTO>>() {
                    }
            );

            assertEquals(0, comprasRetornadas.size());

            List<Compra> comprasEsperadas = compraRepository.findByIdCliente(cliente.getId());

            for (int i = 0; i < comprasEsperadas.size(); i++) {
                Compra compraEsperada = comprasEsperadas.get(i);
                CompraResponseDTO compraRetornada = comprasRetornadas.get(i);

                assertEquals(compraEsperada.getId(), compraRetornada.getId(), "Id da compra divergente");
                assertEquals(compraEsperada.getIdAtivo(), compraRetornada.getIdAtivo(), "Id do ativo divergente");
                assertEquals(compraEsperada.getQuantidade(), compraRetornada.getQuantidade(), "Quantidade divergente");
                assertEquals(compraEsperada.getValorTotal(), compraRetornada.getValorTotal(), "Valor total divergente");
                assertEquals(compraEsperada.getEstadoAtual(), compraRetornada.getEstado(), "Estado divergente");
                assertEquals(compraEsperada.getDataSolicitacao(), compraRetornada.getDataSolicitacao(), "Data de finalização divergente");

            }
        }


        @Test
        @DisplayName("Cliente com compra solicitada")
        void clienteComCompraSolicitada() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/acompanhar-status"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<CompraResponseDTO> comprasRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<CompraResponseDTO>>() {
                    }
            );

            assertEquals(1, comprasRetornadas.size());

            List<Compra> comprasEsperadas = compraRepository.findByIdCliente(cliente.getId());

            for (int i = 0; i < comprasEsperadas.size(); i++) {
                Compra compraEsperada = comprasEsperadas.get(i);
                CompraResponseDTO compraRetornada = comprasRetornadas.get(i);

                assertEquals(compraEsperada.getId(), compraRetornada.getId(), "Id da compra divergente");
                assertEquals(compraEsperada.getIdAtivo(), compraRetornada.getIdAtivo(), "Id do ativo divergente");
                assertEquals(compraEsperada.getQuantidade(), compraRetornada.getQuantidade(), "Quantidade divergente");
                assertEquals(compraEsperada.getValorTotal(), compraRetornada.getValorTotal(), "Valor total divergente");
                assertEquals(compraEsperada.getEstadoAtual(), compraRetornada.getEstado(), "Estado divergente");
                assertEquals(compraEsperada.getDataSolicitacao(), compraRetornada.getDataSolicitacao(), "Data de finalização divergente");

            }
        }

        @Test
        @DisplayName("Cliente com compra disponivel")
        void clienteComCompraDisponivel() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/acompanhar-status"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<CompraResponseDTO> comprasRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<CompraResponseDTO>>() {
                    }
            );

            assertEquals(1, comprasRetornadas.size());

            List<Compra> comprasEsperadas = compraRepository.findByIdCliente(cliente.getId());

            for (int i = 0; i < comprasEsperadas.size(); i++) {
                Compra compraEsperada = comprasEsperadas.get(i);
                CompraResponseDTO compraRetornada = comprasRetornadas.get(i);

                assertEquals(compraEsperada.getId(), compraRetornada.getId(), "Id da compra divergente");
                assertEquals(compraEsperada.getIdAtivo(), compraRetornada.getIdAtivo(), "Id do ativo divergente");
                assertEquals(compraEsperada.getQuantidade(), compraRetornada.getQuantidade(), "Quantidade divergente");
                assertEquals(compraEsperada.getValorTotal(), compraRetornada.getValorTotal(), "Valor total divergente");
                assertEquals(compraEsperada.getEstadoAtual(), compraRetornada.getEstado(), "Estado divergente");
                assertEquals(compraEsperada.getDataSolicitacao(), compraRetornada.getDataSolicitacao(), "Data de finalização divergente");

            }
        }

        @Test
        @DisplayName("Cliente com compra em carteira")
        void clienteComCompraEmCarteira() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            driver.perform(patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraResponseDTOSolicitacao.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/acompanhar-status"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<CompraResponseDTO> comprasRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<CompraResponseDTO>>() {
                    }
            );

            assertEquals(1, comprasRetornadas.size());

            List<Compra> comprasEsperadas = compraRepository.findByIdCliente(cliente.getId());

            for (int i = 0; i < comprasEsperadas.size(); i++) {
                Compra compraEsperada = comprasEsperadas.get(i);
                CompraResponseDTO compraRetornada = comprasRetornadas.get(i);

                assertEquals(compraEsperada.getId(), compraRetornada.getId(), "Id da compra divergente");
                assertEquals(compraEsperada.getIdAtivo(), compraRetornada.getIdAtivo(), "Id do ativo divergente");
                assertEquals(compraEsperada.getQuantidade(), compraRetornada.getQuantidade(), "Quantidade divergente");
                assertEquals(compraEsperada.getValorTotal(), compraRetornada.getValorTotal(), "Valor total divergente");
                assertEquals(compraEsperada.getEstadoAtual(), compraRetornada.getEstado(), "Estado divergente");
                assertEquals(compraEsperada.getDataSolicitacao(), compraRetornada.getDataSolicitacao(), "Data de finalização divergente");

            }
        }

        @Test
        @DisplayName("Cliente com compra solicitada e disponivel")
        void clienteComComprasSolicitadaEDisponivel() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            Ativo ativoSolicitacao = ativos.get(1);

            CompraPostPutRequestDTO compraSolicitacao = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativoSolicitacao.getId())
                    .quantidade(1)
                    .build();

            String compraSolicitacaoJson = objectMapper.writeValueAsString(compraSolicitacao);

            driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraSolicitacaoJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/acompanhar-status"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<CompraResponseDTO> comprasRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<CompraResponseDTO>>() {
                    }
            );

            assertEquals(2, comprasRetornadas.size());

            List<Compra> comprasEsperadas = compraRepository.findByIdCliente(cliente.getId());

            for (int i = 0; i < comprasEsperadas.size(); i++) {
                Compra compraEsperada = comprasEsperadas.get(i);
                CompraResponseDTO compraRetornada = comprasRetornadas.get(i);

                assertEquals(compraEsperada.getId(), compraRetornada.getId(), "Id da compra divergente");
                assertEquals(compraEsperada.getIdAtivo(), compraRetornada.getIdAtivo(), "Id do ativo divergente");
                assertEquals(compraEsperada.getQuantidade(), compraRetornada.getQuantidade(), "Quantidade divergente");
                assertEquals(compraEsperada.getValorTotal(), compraRetornada.getValorTotal(), "Valor total divergente");
                assertEquals(compraEsperada.getEstadoAtual(), compraRetornada.getEstado(), "Estado divergente");
                assertEquals(compraEsperada.getDataSolicitacao(), compraRetornada.getDataSolicitacao(), "Data de finalização divergente");

            }
        }

        @Test
        @DisplayName("Cliente com compra solicitada, disponivel e em carteira")
        void clienteComComprasSolicitaDisponivelEmCarteira() throws Exception {

            Cliente cliente = clientes.get(0);
            Ativo ativo = ativos.get(0);

            CompraPostPutRequestDTO compra = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(1)
                    .build();

            String compraJson = objectMapper.writeValueAsString(compra);

            Ativo ativoSolicitacao = ativos.get(1);

            CompraPostPutRequestDTO compraSolicitacao = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativoSolicitacao.getId())
                    .quantidade(1)
                    .build();

            String compraSolicitacaoJson = objectMapper.writeValueAsString(compraSolicitacao);

            driver.perform(post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(compraSolicitacaoJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Ativo ativoDisponivel = ativos.get(2);

            CompraPostPutRequestDTO compraDisponivel = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativoDisponivel.getId())
                    .quantidade(1)
                    .build();

            String compraDisponivelJson = objectMapper.writeValueAsString(compraDisponivel);

            String responseJsonStringDisponivel = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraDisponivelJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTODisponivel = objectMapper.readValue(responseJsonStringDisponivel, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTODisponivel.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            driver.perform(patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/finalizar/" + compraResponseDTOSolicitacao.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/acompanhar-status"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<CompraResponseDTO> comprasRetornadas = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<List<CompraResponseDTO>>() {
                    }
            );

            assertEquals(3, comprasRetornadas.size());

            List<Compra> comprasEsperadas = compraRepository.findByIdCliente(cliente.getId());

            for (int i = 0; i < comprasEsperadas.size(); i++) {
                Compra compraEsperada = comprasEsperadas.get(i);
                CompraResponseDTO compraRetornada = comprasRetornadas.get(i);

                assertEquals(compraEsperada.getId(), compraRetornada.getId(), "Id da compra divergente");
                assertEquals(compraEsperada.getIdAtivo(), compraRetornada.getIdAtivo(), "Id do ativo divergente");
                assertEquals(compraEsperada.getQuantidade(), compraRetornada.getQuantidade(), "Quantidade divergente");
                assertEquals(compraEsperada.getValorTotal(), compraRetornada.getValorTotal(), "Valor total divergente");
                assertEquals(compraEsperada.getEstadoAtual(), compraRetornada.getEstado(), "Estado divergente");
                assertEquals(compraEsperada.getDataSolicitacao(), compraRetornada.getDataSolicitacao(), "Data de finalização divergente");
            }

        }

    }

    @Nested
    @Transactional
    class AprovarCompra {

        private final PrintStream originalOut = System.out;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        @DisplayName("Quando o admin aprova uma compra com sucesso.")
        void aprovarCompraComSucessoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();

            System.out.println(ativoRepository.findAll());
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            String responseJsonStringAprovacao = driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOAprovacao = objectMapper.readValue(responseJsonStringAprovacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.DISPONIVEL, compraResponseDTOAprovacao.getEstado());
            assertEquals(2.0, compraResponseDTOAprovacao.getValorTotal());
            String output = outContent.toString();
            assertTrue(output.contains(
                    String.format("User: %s\nAlerta: Sua compra do ativo '%s' foi aprovada",
                            cliente.getNome(),
                            ativo.getNome())));

        }

        @Test
        @DisplayName("Quando o admin com acesso invalido tenta aprovar uma compra.")
        void aprovarCompraComAcessoInvalidoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "000000"))
                    .andExpect(status().isBadRequest());

        }

        @Test
        @DisplayName("Quando o admin tenta aprovar uma compra inexistente.")
        void aprovarCompraInexistente() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + 123L + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isNotFound());

        }

        @Test
        @DisplayName("Quando o admin tenta aprovar uma compra que não tem estado SOLICITADO.")
        void aprovarCompraForaDoEstadoSolicitado() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isForbidden());

        }

        @Test
        @DisplayName("Quando o admin tenta aprovar uma compra de um cliente com balanço insuficiente.")
        void aprovarCompraComBalancoInsuficiente() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            cliente.getCarteira().setBalanco(0);
            clienteRepository.save(cliente);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/aprovar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isUnprocessableEntity());

        }

    }

    @Nested
    @Transactional
    class RecusarCompra {

        private final PrintStream originalOut = System.out;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        @DisplayName("Quando o admin recusa uma compra com sucesso.")
        void recusarCompraComSucessoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();

            System.out.println(ativoRepository.findAll());
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            String responseJsonStringAprovacao = driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/recusar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOAprovacao = objectMapper.readValue(responseJsonStringAprovacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOAprovacao.getEstado());
            assertEquals(2.0, compraResponseDTOAprovacao.getValorTotal());
            String output = outContent.toString();
            assertTrue(output.contains(
                    String.format("User: %s\nAlerta: Sua compra do ativo '%s' foi recusada",
                            cliente.getNome(),
                            ativo.getNome())));

        }

        @Test
        @DisplayName("Quando o admin com acesso invalido tenta recusar uma compra.")
        void recusarCompraComAcessoInvalidoTest() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId() + "/recusar")
                                    .param("codigoAcesso", "000000"))
                    .andExpect(status().isBadRequest());

        }

        @Test
        @DisplayName("Quando o admin tenta recusar uma compra inexistente.")
        void recusarCompraInexistente() throws Exception {

            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);
            Ativo ativo = ativoRepository.findByNomeContaining("Ativo1").get(0);

            CompraPostPutRequestDTO compraDTO = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(cliente.getCodigoAcesso())
                    .idAtivo(ativo.getId())
                    .quantidade(2.0).build();
            String json = objectMapper.writeValueAsString(compraDTO);

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/solicitar")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            driver.perform(
                            patch(URI_COMPRAS + "/" + 123L + "/recusar")
                                    .param("codigoAcesso", "123456"))
                    .andExpect(status().isNotFound());

        }

    }


}
