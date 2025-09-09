package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.AtualizarStatusTransacaoDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.compra.CompraResponseDTO;
import com.ufcg.psoft.commerce.enums.*;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.models.carteira.AtivoCarteira;
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras"))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras"))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk());

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras"))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk());

            driver.perform(patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/" + compraResponseDTOSolicitacao.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras"))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraSolicitacaoJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk());

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras"))
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

            driver.perform(post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraDisponivelJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTODisponivel = objectMapper.readValue(responseJsonStringDisponivel, CompraResponseDTO.class);

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTODisponivel.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk());

            String responseJsonStringSolicitacao = driver.perform(
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(compraJson))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk());

            driver.perform(patch(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/" + compraResponseDTOSolicitacao.getId()))
                    .andExpect(status().isOk())
                    .andDo(print());

            String responseJsonString = driver.perform(
                            get(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras"))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            String responseJsonStringAprovacao = driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.estadoAtual").value(EstadoCompra.DISPONIVEL.name()))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            CompraResponseDTO compraResponseDTOAprovacao = objectMapper.readValue(responseJsonStringAprovacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.DISPONIVEL, compraResponseDTOAprovacao.getEstado());
            assertEquals(2.0, compraResponseDTOAprovacao.getValorTotal());
            String output = outContent.toString();
            assertTrue(output.contains(
                    String.format("User: %s%nAlerta: Sua compra do ativo '%s' foi aprovada",
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_INVALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk());

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
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

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.APROVADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.RECUSADO)
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            String responseJsonStringAprovacao = driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.estadoAtual").value(EstadoCompra.SOLICITADO.name()))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            CompraResponseDTO compraResponseDTOAprovacao = objectMapper.readValue(responseJsonStringAprovacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOAprovacao.getEstado());
            assertEquals(2.0, compraResponseDTOAprovacao.getValorTotal());
            String output = outContent.toString();
            assertTrue(output.contains(
                    String.format("User: %s%nAlerta: Sua compra do ativo '%s' foi recusada",
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO compraResponseDTOSolicitacao = objectMapper.readValue(responseJsonStringSolicitacao, CompraResponseDTO.class);

            assertEquals(EstadoCompra.SOLICITADO, compraResponseDTOSolicitacao.getEstado());
            assertEquals(2.0, compraResponseDTOSolicitacao.getValorTotal());

            AtualizarStatusTransacaoDTO aprovacao = AtualizarStatusTransacaoDTO.builder()
                    .estado(DecisaoAdministrador.RECUSADO)
                    .codigoAcesso("000000")
                    .build();

            String aprovacaoJson = objectMapper.writeValueAsString(aprovacao);

            driver.perform(
                            patch(URI_COMPRAS + "/" + compraResponseDTOSolicitacao.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(aprovacaoJson))
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
                            post(URI_COMPRAS_CLIENTES + "/" + cliente.getId() + "/compras")
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

    @Nested
    class ConfirmarCompra {

        @Test
        @DisplayName("Deve confirmar compra com sucesso e mover estado de DISPONIVEL → COMPRADO → EM_CARTEIRA")
        void confirmarCompraComSucesso() throws Exception {
            Cliente cliente = clientes.get(0);
            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(2.0)
                            .precoUnitario(10.0)
                            .valorTotal(20.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/" + compra.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.estadoAtual").value("EM_CARTEIRA"));
        }

        @Test
        @DisplayName("Não deve confirmar compra inexistente")
        void confirmarCompraInexistente() throws Exception {
            Cliente cliente = clientes.get(0);

            driver.perform(patch("/clientes/" + cliente.getId() + "/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Não deve confirmar compra que não pertence ao cliente")
        void confirmarCompraOutroCliente() throws Exception {
            Cliente cliente1 = clientes.get(0);
            Cliente cliente2 = clientes.get(1);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente2.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(1.0)
                            .precoUnitario(5.0)
                            .valorTotal(5.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente1.getId() + "/" + compra.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não deve confirmar compra em estado inválido (ex.: já comprada)")
        void confirmarCompraEstadoInvalido() throws Exception {
            Cliente cliente = clientes.get(0);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(1.0)
                            .precoUnitario(5.0)
                            .valorTotal(5.0)
                            .estadoAtual(EstadoCompra.COMPRADO)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/" + compra.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve confirmar compra se o saldo for exatamente igual ao valor da compra")
        void confirmarCompraSaldoExato() throws Exception {
            Cliente cliente = clientes.get(0);
            cliente.getCarteira().setBalanco(20.0);
            clienteRepository.save(cliente);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(2.0)
                            .precoUnitario(10.0)
                            .valorTotal(20.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/" + compra.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estadoAtual").value("EM_CARTEIRA"));
        }

        @Test
        @DisplayName("Não deve confirmar compra se o cliente não tiver saldo suficiente")
        void confirmarCompraSaldoInsuficiente() throws Exception {
            Cliente cliente = clientes.get(0);
            cliente.getCarteira().setBalanco(1.0);
            clienteRepository.save(cliente);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(10.0)
                            .precoUnitario(100.0)
                            .valorTotal(1000.0)
                            .estadoAtual(EstadoCompra.DISPONIVEL)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/" + compra.getId()))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Não deve confirmar compra em estado SOLICITADO")
        void confirmarCompraSolicitado() throws Exception {
            Cliente cliente = clientes.get(0);

            Compra compra = compraRepository.save(
                    Compra.builder()
                            .idCliente(cliente.getId())
                            .idAtivo(ativos.get(0).getId())
                            .quantidade(1.0)
                            .precoUnitario(4.0)
                            .valorTotal(4.0)
                            .estadoAtual(EstadoCompra.SOLICITADO)
                            .build()
            );

            driver.perform(patch("/clientes/" + cliente.getId() + "/" + compra.getId()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testes para visualizar carteira")
    class VisualizarCarteira {

        @Test
        @DisplayName("Deve retornar a carteira do cliente com ativos")
        void quandoCarteiraPossuiAtivos() throws Exception {
            Long idCliente = clientes.get(0).getId();

            String responseJsonString = driver.perform(get("/clientes/" + idCliente + "/carteira"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<AtivoCarteiraResponseDTO> resultado = objectMapper
                    .readValue(responseJsonString, objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, AtivoCarteiraResponseDTO.class));

            Carteira carteira = clientes.get(0).getCarteira();

            assertAll(
                    () -> assertEquals(carteira.getAtivos().size(), resultado.size()),
                    () -> resultado.forEach(dto -> {
                        Ativo ativo = ativoRepository.findById(dto.getIdAtivo()).orElseThrow();
                        AtivoCarteira ativoCarteira = carteira.getAtivos().get(dto.getIdAtivo());

                        assertAll(
                                () -> assertEquals(ativo.getId(), dto.getIdAtivo()),
                                () -> assertEquals(ativo.getTipo().getNomeTipo(), dto.getTipo()),
                                () -> assertEquals(ativoCarteira.getQuantidade(), dto.getQuantidade()),
                                () -> assertEquals(ativoCarteira.getValorAcumulado(), dto.getValorAquisicaoAcumulado()),
                                () -> assertEquals(ativo.getValor(), dto.getValorAtualUnitario()),
                                () -> assertEquals(Math.round((ativo.getValor() * ativoCarteira.getQuantidade() - ativoCarteira.getValorAcumulado()) * 100.0)/100.0,
                                        dto.getDesempenho())
                        );
                    })
            );
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando carteira estiver sem ativos")
        void quandoCarteiraEstaVazia() throws Exception {
            Long idCliente = clientes.get(1).getId();

            // Limpa a carteira
            Carteira carteiraVazia = Carteira.builder().balanco(200.0).build();
            clientes.get(1).setCarteira(carteiraVazia);
            clienteRepository.save(clientes.get(1));

            String responseJsonString = driver.perform(get("/clientes/" + idCliente + "/carteira"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<AtivoCarteiraResponseDTO> resultado = objectMapper
                    .readValue(responseJsonString, objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, AtivoCarteiraResponseDTO.class));

            assertEquals(0, resultado.size());
        }

        @Test
        @DisplayName("Deve retornar 400 quando cliente não existir")
        void quandoClienteNaoExiste() throws Exception {
            Long idCliente = 99999L;

            driver.perform(get("/clientes/" + idCliente + "/carteira"))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("Testes para solicitação de compra")
    class SolicitarCompra {

        @Test
        @DisplayName("Deve solicitar a compra com sucesso para Cliente Premium")
        void compraValidaParaPlanoPremium() throws Exception {
            Long idCliente = clientes.get(2).getId(); // Premium
            Long idAtivo = ativos.get(4).getId(); // Criptomoeda

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .idAtivo(idAtivo)
                    .quantidade(2.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            String responseJsonString = driver.perform(post("/clientes/" + idCliente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO resultado = objectMapper
                    .readValue(responseJsonString, CompraResponseDTO.class);

            assertAll(
                    () -> assertEquals(idCliente, resultado.getIdCliente()),
                    () -> assertEquals(idAtivo, resultado.getIdAtivo()),
                    () -> assertEquals(2.0, resultado.getQuantidade())
            );
        }

        @Test
        @DisplayName("Deve solicitar a compra com sucesso para Cliente Normal")
        void compraValidaParaPlanoNormal() throws Exception {
            Long idCliente = clientes.get(1).getId(); // Normal
            Long idAtivo = ativos.get(1).getId(); // TesouroDireto

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .idAtivo(idAtivo)
                    .quantidade(2.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            String responseJsonString = driver.perform(post("/clientes/" + idCliente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CompraResponseDTO resultado = objectMapper
                    .readValue(responseJsonString, CompraResponseDTO.class);

            assertAll(
                    () -> assertEquals(idCliente, resultado.getIdCliente()),
                    () -> assertEquals(idAtivo, resultado.getIdAtivo()),
                    () -> assertEquals(2.0, resultado.getQuantidade())
            );
        }

        @Test
        @DisplayName("Deve retornar 400 quando cliente não existir")
        void quandoClienteNaoExiste() throws Exception {
            Long idClienteInexistente = 99999L;
            Long idAtivo = ativos.get(1).getId();

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .idAtivo(idAtivo)
                    .quantidade(2.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + idClienteInexistente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("Deve retornar 400 quando código de acesso for inválido")
        void quandoCodigoDeAcessoInvalido() throws Exception {
            Long idCliente = clientes.get(1).getId();
            Long idAtivo = ativos.get(1).getId();

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_INVALIDO)
                    .idAtivo(idAtivo)
                    .quantidade(1.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + idCliente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Deve retornar 400 quando ativo não existir")
        void quandoAtivoNaoExiste() throws Exception {
            Long idCliente = clientes.get(1).getId();
            Long idAtivoInexistente = 99999L;

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .idAtivo(idAtivoInexistente)
                    .quantidade(1.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + idCliente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("O ativo consultado nao existe!")));
        }

        @Test
        @DisplayName("Deve lançar exceção")
        void quandoPlanoNaoPermitirCompra() throws Exception {
            Long idCliente = clientes.get(1).getId(); // Normal
            Long idAtivo = ativos.get(4).getId(); // CriptoMoeda

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .idAtivo(idAtivo)
                    .quantidade(1.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + idCliente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Plano do cliente nao permite marcar interesse!")));
        }


        @Test
        @DisplayName("Deve lançar exceção")
        void quandoAtivoIndisponivel() throws Exception {
            Long idCliente = clientes.get(2).getId(); // Premium
            Long idAtivo = ativos.get(9).getId(); // Acao Indisponivel

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .idAtivo(idAtivo)
                    .quantidade(1.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + idCliente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve lançar exceção")
        void quandoSaldoInsuficiente() throws Exception {
            Long idCliente = clientes.get(2).getId();
            Long idAtivo = ativos.get(1).getId();

            Ativo ativo = ativos.get(1);
            ativo.setValor(500.0);
            ativoRepository.save(ativo);

            CompraPostPutRequestDTO dto = CompraPostPutRequestDTO.builder()
                    .codigoAcesso(CODIGO_ACESSO_VALIDO)
                    .idAtivo(idAtivo)
                    .quantidade(1.0)
                    .build();
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(post("/clientes/" + idCliente + "/compras")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnprocessableEntity());

        }
    }



}
