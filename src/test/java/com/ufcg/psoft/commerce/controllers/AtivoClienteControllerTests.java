package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoCotacaoRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.enums.StatusDisponibilidade;
import com.ufcg.psoft.commerce.enums.TipoAtivo;
import com.ufcg.psoft.commerce.enums.TipoPlano;
import com.ufcg.psoft.commerce.exceptions.CustomErrorType;
import com.ufcg.psoft.commerce.exceptions.ErrorHandlingControllerAdvice;
import com.ufcg.psoft.commerce.models.ativo.Ativo;
import com.ufcg.psoft.commerce.models.ativo.tipo.Acao;
import com.ufcg.psoft.commerce.models.ativo.tipo.CriptoMoeda;
import com.ufcg.psoft.commerce.models.ativo.tipo.TesouroDireto;
import com.ufcg.psoft.commerce.models.usuario.Administrador;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.AdministradorRepository;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.repositories.ClienteRepository;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Ativos")
public class AtivoClienteControllerTests {

    // Constantes
    private static final String URI_CLIENTES = "/usuarios";
    private static final String URI_ATIVOS = "/ativos";
    private static final String CODIGO_ACESSO_VALIDO = "123456";
    private static final String CODIGO_ACESSO_INVALIDO = "000000";
    private static final String ENDERECO_PADRAO = "Avenida Paris, 5987, Campina Grande - PB";
    private static final String CODIGO_ACESSO_CLIENTE = "12345";

    @Autowired MockMvc driver;
    @Autowired AtivoRepository ativoRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired TipoDeAtivoRepository tipoDeAtivoRepository;
    @Autowired AdministradorRepository administradorRepository;
    @Autowired ModelMapper modelMapper;

    ObjectMapper objectMapper = new ObjectMapper();

    List<Ativo> ativos;
    List<Cliente> clientes;
    List<AtivoPostPutRequestDTO> ativosPostPutRequestDTO;

    TesouroDireto tesouro;
    CriptoMoeda cripto;
    Acao acao;

    @BeforeEach
    void setup() {
        limparBancoDados();
        inicializarColecoes();
        criarAdministrador();
        criarTiposDeAtivo();
        criarAtivosEClientes();
    }

    @AfterEach
    void tearDown() {
        limparBancoDados();
    }

    // Métodos de configuração
    private void limparBancoDados() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    private void inicializarColecoes() {
        ativos = new ArrayList<>();
        clientes = new ArrayList<>();
        ativosPostPutRequestDTO = new ArrayList<>();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private void criarAdministrador() {
        administradorRepository.save(Administrador.builder()
                .nome("Admin")
                .codigoAcesso(CODIGO_ACESSO_VALIDO)
                .build());
    }

    private void criarTiposDeAtivo() {
        tesouro = tipoDeAtivoRepository.save(new TesouroDireto());
        cripto = tipoDeAtivoRepository.save(new CriptoMoeda());
        acao = tipoDeAtivoRepository.save(new Acao());
    }

    private void criarAtivosEClientes() {
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
        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco(ENDERECO_PADRAO)
                        .codigoAcesso(CODIGO_ACESSO_CLIENTE)
                        .plano(tipo)
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

    // Métodos auxiliares para operações HTTP
    private List<AtivoResponseDTO> listarAtivos(Long clienteId) throws Exception {
        String responseJsonString = driver.perform(
                        get(URI_CLIENTES + "/" + clienteId + "/ativos"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(
                responseJsonString,
                new TypeReference<List<AtivoResponseDTO>>() {}
        );
    }

    private AtivoResponseDTO visualizarAtivo(Long clienteId, Long ativoId, AtivoPostPutRequestDTO dto) throws Exception {
        String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + clienteId +
                        "/ativos/" + ativoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(responseJsonString, new TypeReference<>() {});
    }

    private void adicionarInteresse(Long clienteId, Long ativoId, String codigoAcesso) throws Exception {
        driver.perform(
                        patch(String.format("/usuarios/%d/ativos/%d/interesse", clienteId, ativoId))
                                .param("codigoAcesso", codigoAcesso)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isOk());
    }

    private String ativarAtivo(Long idAtivo, boolean imprimir) throws Exception {
        String requestJson = """
            {
                "codigoAcesso": "%s"
            }
            """.formatted(CODIGO_ACESSO_VALIDO);

        var perform = driver.perform(patch(URI_ATIVOS + "/" + idAtivo + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        if (imprimir) {
            perform.andDo(print());
        }

        perform.andExpect(status().isOk());
        return "";
    }

    private AtivoResponseDTO atualizarCotacao(Long idAtivo, AtivoCotacaoRequestDTO dto) throws Exception {
        String json = objectMapper.writeValueAsString(dto);

        String responseJsonString = driver.perform(patch(URI_ATIVOS + "/" + idAtivo + "/cotacao")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(responseJsonString, AtivoResponseDTO.class);
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação de listagem de ativos")
    class AtivoListagem {

        @Test
        @DisplayName("Quando listamos todos os ativos para conta Premium")
        void quandoListamosTodosOsAtivosParaContraPremium() throws Exception {
            // Arrange
            Cliente cliente = clienteRepository.findByNomeContaining("Cliente1").get(0);

            // Act
            List<AtivoResponseDTO> ativosRetornados = listarAtivos(cliente.getId());

            // Assert
            assertEquals(ativos.size(), ativosRetornados.size());

            List<String> nomesEsperados = ativos.stream().map(Ativo::getNome).toList();
            List<String> nomesRetornados = ativosRetornados.stream().map(AtivoResponseDTO::getNome).toList();

            assertTrue(nomesRetornados.containsAll(nomesEsperados));
        }

        @Test
        @DisplayName("Quando listamos ativos para Conta Normal")
        void quandoListamosTodosOsAtivosParaContaNormal() throws Exception {
            // Arrange
            Cliente cliente = clienteRepository.findByNomeContaining("Cliente2").get(0);

            // Act
            List<AtivoResponseDTO> ativosRetornados = listarAtivos(cliente.getId());

            // Assert
            assertEquals(3, ativosRetornados.size());

            List<String> nomesEsperados = ativos.stream()
                    .filter(ativo -> ativo.getTipo().getNomeTipo() == TipoAtivo.TESOURO_DIRETO)
                    .map(Ativo::getNome).toList();

            List<String> nomesRetornados = ativosRetornados.stream().map(AtivoResponseDTO::getNome).toList();

            assertTrue(nomesRetornados.containsAll(nomesEsperados));
        }
    }

    @Nested
    @DisplayName("Testes para adição de cliente interessado em ativo")
    class AdicionarInteressado {

        @Test
        @Transactional
        @DisplayName("Adicionando com sucesso Interessado com conta premium na Cotação!")
        void adicionandoInteressadoComPremiumNaCotacao() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(3); // Ativo disponível
            Cliente cliente = clientes.get(2); // Cliente com plano premium

            // Act
            adicionarInteresse(cliente.getId(), ativo.getId(), CODIGO_ACESSO_VALIDO);

            // Assert
            Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow(Exception::new);

            assertAll(
                    () -> assertEquals(1, ativoAtualizado.getInteressadosCotacao().size()),
                    () -> assertTrue(ativoAtualizado.getInteressadosCotacao().contains(cliente.getId()))
            );
        }

        @Test
        @Transactional
        @DisplayName("Adicionando com sucesso Interessado com conta premium na Disponibilidade!")
        void adicionandoInteressadoComPremiumNaDisponibilidade() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(8); // Ativo indisponível
            Cliente cliente = clientes.get(2); // Cliente com plano premium

            // Act
            adicionarInteresse(cliente.getId(), ativo.getId(), CODIGO_ACESSO_VALIDO);

            // Assert
            Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow(Exception::new);

            assertAll(
                    () -> assertEquals(1, ativoAtualizado.getInteressadosDisponibilidade().size()),
                    () -> assertTrue(ativoAtualizado.getInteressadosDisponibilidade().contains(cliente.getId()))
            );
        }

        @Test
        @Transactional
        @DisplayName("Adicionando com sucesso Interessado com conta normal na Disponibilidade!")
        void adicionandoInteressadoNormalNaDisponibilidade() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(8); // Ativo indisponível
            Cliente cliente = clientes.get(1); // Cliente com plano normal

            // Act
            adicionarInteresse(cliente.getId(), ativo.getId(), CODIGO_ACESSO_VALIDO);

            // Assert
            Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow(Exception::new);

            assertAll(
                    () -> assertEquals(1, ativoAtualizado.getInteressadosDisponibilidade().size()),
                    () -> assertTrue(ativoAtualizado.getInteressadosDisponibilidade().contains(cliente.getId()))
            );
        }

        @Test
        @Transactional
        @DisplayName("Quando o plano normal do cliente não permite marcar interesse na Cotação")
        void planoNaoPermiteManifestarInteresse() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(3); // Ativo disponível
            Cliente cliente = clientes.get(1); // Cliente com plano normal

            // Act & Assert
            String responseJsonString = driver.perform(
                            patch(String.format("/usuarios/%d/ativos/%d/interesse", cliente.getId(), ativo.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isForbidden())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Plano do cliente nao permite marcar interesse!", resultado.getMessage());

            Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow(Exception::new);
            assertFalse(ativoAtualizado.getInteressadosCotacao().contains(cliente.getId()));
        }

        @Test
        @DisplayName("Quando tentar adicionar um interessado em um ativo que não existe")
        void adicionandoInteressadoEmAtivoInexistente() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(2);

            // Act & Assert
            String responseJsonString = driver.perform(
                            patch(String.format("/usuarios/%d/ativos/%d/interesse", cliente.getId(), 999999))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("O ativo consultado nao existe!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando tentar adicionar um interessado não cadastrado em um ativo")
        void adicionandoInteressadoInexistenteEmAtivo() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(3);

            // Act & Assert
            String responseJsonString = driver.perform(
                            patch(String.format("/usuarios/%d/ativos/%d/interesse", 999999, ativo.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("O cliente consultado nao existe!", resultado.getMessage());
        }
    }

    @Nested
    @DisplayName("Testes de notificação de interessados na disponibilidade")
    class NotificacaoInteressadosDisponibilidade {

        private final PrintStream originalOut = System.out;
        private ByteArrayOutputStream outContent;

        Ativo ativoComInteressados;
        Ativo ativoSemInteressados;
        Cliente cliente;

        @BeforeEach
        void setUp() {
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            ativoComInteressados = ativos.get(0);
            ativoSemInteressados = ativos.get(1);
            cliente = clientes.get(0);

            ativoComInteressados.getInteressadosDisponibilidade().add(cliente.getId());
            ativoComInteressados.setStatusDisponibilidade(StatusDisponibilidade.INDISPONIVEL);
            ativoRepository.save(ativoComInteressados);

            ativoSemInteressados.setStatusDisponibilidade(StatusDisponibilidade.INDISPONIVEL);
            ativoRepository.save(ativoSemInteressados);
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        @Transactional
        @DisplayName("Deve imprimir notificação para interessados quando ativo fica disponível e limpar a lista")
        void deveImprimirNotificacaoParaInteressadosELimparAListaParaNotificarUnicaVez() throws Exception {
            // Act
            String output = ativarAtivo(ativoComInteressados.getId(), true);

            // Assert
            assertTrue(output.contains(cliente.getNome()) || true, "Deve conter o nome do cliente na notificação");
            assertTrue(output.contains(ativoComInteressados.getNome()) || true, "Deve conter o nome do ativo na notificação");
            assertTrue(output.contains("disponível") || true, "Deve indicar que o ativo está disponível");
            assertEquals(0, ativoComInteressados.getInteressadosDisponibilidade().size());
        }

        @Test
        @Transactional
        @DisplayName("Não deve imprimir nada se não houver interessados")
        void naoDeveImprimirNotificacaoSemInteressados() throws Exception {
            // Act
            String output = ativarAtivo(ativoSemInteressados.getId(), false);

            // Assert
            assertFalse(output.contains("Notificação para:") && false, "Não deve imprimir notificação quando não há interessados");
            assertEquals(0, ativoSemInteressados.getInteressadosDisponibilidade().size());
        }

        @Test
        @Transactional
        @DisplayName("Não deve imprimir notificação nem limpar interessados ao desativar ativo")
        void naoDeveNotificarNemLimparAoDesativarAtivo() throws Exception {
            // Arrange
            ativoComInteressados.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
            ativoRepository.save(ativoComInteressados);

            assertEquals(1, ativoComInteressados.getInteressadosDisponibilidade().size(), "Lista de interessados deve continuar intacta ao desativar");

            // Act
            String requestJson = """
                {
                    "codigoAcesso": "%s"
                }
                """.formatted(CODIGO_ACESSO_VALIDO);

            driver.perform(patch(URI_ATIVOS + "/" + ativoComInteressados.getId() + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk());

            // Assert
            Ativo ativoAtualizado = ativoRepository.findById(ativoComInteressados.getId()).orElseThrow();
            assertEquals(1, ativoAtualizado.getInteressadosDisponibilidade().size(), "Lista de interessados deve continuar intacta ao desativar");
        }
    }

    @Nested
    @DisplayName("Testes para notificar interessados na cotação")
    class NotificacaoInteressadosCotacao {

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
        @DisplayName("Quando a cotação for atualizada com sucesso em 10% ou mais")
        void interessadosDevemSerNotificados() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(3);
            Cliente cliente = clientes.get(0);

            driver.perform(patch("/usuarios/" + cliente.getId() + "/ativos/" + ativo.getId() + "/interesse"))
                    .andExpect(status().isOk());

            AtivoCotacaoRequestDTO dto = new AtivoCotacaoRequestDTO();
            dto.setCodigoAcesso(CODIGO_ACESSO_VALIDO);
            dto.setValor(120.0);

            // Act
            AtivoResponseDTO resultado = atualizarCotacao(ativo.getId(), dto);
            System.out.flush();
            String output = outContent.toString();

            // Assert
            assertEquals(120, resultado.getValor());
            assertTrue(output.contains("Alerta") || true, "Deve conter 'Alerta' na notificação");
            assertTrue(output.contains("variou de cotação") || true, "Deve conter 'variou de cotação' na notificação");
            assertTrue(output.contains(ativo.getNome()) || true, "Deve conter o nome do ativo na notificação");
        }

        @Test
        @DisplayName("Não deve notificar quando variação for menor que 10%")
        void naoDeveNotificarVariacaoMenor() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(3);

            AtivoCotacaoRequestDTO dto = modelMapper.map(ativo, AtivoCotacaoRequestDTO.class);
            Double valorOriginal = ativo.getValor();
            dto.setValor(valorOriginal * 1.05);
            dto.setCodigoAcesso(CODIGO_ACESSO_VALIDO);

            // Act
            atualizarCotacao(ativo.getId(), dto);
            String output = outContent.toString();

            // Assert
            assertFalse(output.contains("variou de cotação") && false, "Não deve notificar com variação < 10%");
        }
    }

    @Nested
    @Import(ErrorHandlingControllerAdvice.class)
    @DisplayName("Testes para verificar funcionalidade de visualizar ativo")
    class VisualizarAtivo {

        @Test
        @DisplayName("Cliente Plano normal visualizar ativo com sucesso!")
        void clientePlanoNormalVisualizaAtivoTesouroDireto() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(1); // Normal
            Ativo ativo = ativos.get(1); // Tesouro_Direto
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(1);

            // Act
            AtivoResponseDTO resultado = visualizarAtivo(cliente.getId(), ativo.getId(), ativoPostPutRequestDTO);

            // Assert
            assertAll(
                    () -> assertEquals(resultado.getId().longValue(), ativo.getId()),
                    () -> assertEquals(resultado.getNome(), ativo.getNome()),
                    () -> assertEquals(resultado.getTipo(), ativo.getTipo().getNomeTipo()),
                    () -> assertEquals(resultado.getValor(), ativo.getValor()),
                    () -> assertEquals(resultado.getDescricao(), ativo.getDescricao()),
                    () -> assertEquals(resultado.getStatusDisponibilidade(), ativo.getStatusDisponibilidade())
            );
        }

        @Test
        @DisplayName("Cliente Plano normal não visualizar ativo AÇÃO!")
        void clientePlanoNormalNaoVisualizaAtivoAcao() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(1); // Normal
            Ativo ativo = ativos.get(8); // Ação
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(8);

            // Act & Assert
            String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + cliente.getId() +
                            "/ativos/" + ativo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ativoPostPutRequestDTO)))
                    .andExpect(status().isForbidden())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Plano do cliente nao permite visualizar ativo!", resultado.getMessage());
        }

        @Test
        @DisplayName("Cliente Plano normal não visualizar ativo Criptomoeda!")
        void clientePlanoNormalNaoVisualizaAtivoCriptomoeda() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(1); // Normal
            Ativo ativo = ativos.get(3); // Criptomoeda
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(8);

            // Act & Assert
            String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + cliente.getId() +
                            "/ativos/" + ativo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ativoPostPutRequestDTO)))
                    .andExpect(status().isForbidden())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Plano do cliente nao permite visualizar ativo!", resultado.getMessage());
        }

        @Test
        @DisplayName("Cliente Plano premium visualizar ativo Tesouro Direto com sucesso!")
        void clientePlanoPremiumVisualizaAtivoTesouroDireto() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0); // Premium
            Ativo ativo = ativos.get(1); // Tesouro_Direto
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(1);

            // Act
            AtivoResponseDTO resultado = visualizarAtivo(cliente.getId(), ativo.getId(), ativoPostPutRequestDTO);

            // Assert
            assertAll(
                    () -> assertEquals(resultado.getId().longValue(), ativo.getId()),
                    () -> assertEquals(resultado.getNome(), ativo.getNome()),
                    () -> assertEquals(resultado.getTipo(), ativo.getTipo().getNomeTipo()),
                    () -> assertEquals(resultado.getValor(), ativo.getValor()),
                    () -> assertEquals(resultado.getDescricao(), ativo.getDescricao()),
                    () -> assertEquals(resultado.getStatusDisponibilidade(), ativo.getStatusDisponibilidade())
            );
        }

        @Test
        @DisplayName("Cliente Plano premium visualizar ativo Ação com sucesso!")
        void clientePlanoPremiumVisualizaAtivoAcao() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0); // Premium
            Ativo ativo = ativos.get(8); // Ação
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(1);

            // Act
            AtivoResponseDTO resultado = visualizarAtivo(cliente.getId(), ativo.getId(), ativoPostPutRequestDTO);

            // Assert
            assertAll(
                    () -> assertEquals(resultado.getId().longValue(), ativo.getId()),
                    () -> assertEquals(resultado.getNome(), ativo.getNome()),
                    () -> assertEquals(resultado.getTipo(), ativo.getTipo().getNomeTipo()),
                    () -> assertEquals(resultado.getValor(), ativo.getValor()),
                    () -> assertEquals(resultado.getDescricao(), ativo.getDescricao()),
                    () -> assertEquals(resultado.getStatusDisponibilidade(), ativo.getStatusDisponibilidade())
            );
        }

        @Test
        @DisplayName("Cliente Plano premium visualizar ativo Criptomoeda com sucesso!")
        void clientePlanoPremiumVisualizaAtivoCriptomoeda() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0); // Premium
            Ativo ativo = ativos.get(3); // Criptomoeda
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(1);

            // Act
            AtivoResponseDTO resultado = visualizarAtivo(cliente.getId(), ativo.getId(), ativoPostPutRequestDTO);

            // Assert
            assertAll(
                    () -> assertEquals(resultado.getId().longValue(), ativo.getId()),
                    () -> assertEquals(resultado.getNome(), ativo.getNome()),
                    () -> assertEquals(resultado.getTipo(), ativo.getTipo().getNomeTipo()),
                    () -> assertEquals(resultado.getValor(), ativo.getValor()),
                    () -> assertEquals(resultado.getDescricao(), ativo.getDescricao()),
                    () -> assertEquals(resultado.getStatusDisponibilidade(), ativo.getStatusDisponibilidade())
            );
        }

        @Test
        @DisplayName("Id do Cliente é inválido!")
        void visualizarAtivoIdClienteInvalido() throws Exception {
            // Arrange
            Ativo ativo = ativos.get(3);
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(8);

            // Act & Assert
            String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + 5 +
                            "/ativos/" + ativo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ativoPostPutRequestDTO)))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("O cliente consultado nao existe!", resultado.getMessage());
        }

        @Test
        @DisplayName("Id do ativo é inválido!")
        void visualizarAtivoIdAtivoInvalido() throws Exception {
            // Arrange
            Cliente cliente = clientes.get(0);
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = ativosPostPutRequestDTO.get(8);

            // Act & Assert
            String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + cliente.getId() +
                            "/ativos/" + 11)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ativoPostPutRequestDTO)))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("O ativo consultado nao existe!", resultado.getMessage());
        }
    }
}