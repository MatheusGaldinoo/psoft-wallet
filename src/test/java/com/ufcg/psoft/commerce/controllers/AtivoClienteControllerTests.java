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
import com.ufcg.psoft.commerce.exceptions.CustomErrorType;
import com.ufcg.psoft.commerce.models.*;
import com.ufcg.psoft.commerce.repositories.AdministradorRepository;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.repositories.ClienteRepository;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Ativos")
public class AtivoClienteControllerTests {

    final String URI_CLIENTES = "/usuario";
    final String CODIGO_ACESSO_VALIDO = "123456";
    final String CODIGO_ACESSO_INVALIDO = "000000";

    @Autowired
    MockMvc driver;

    @Autowired
    AtivoRepository ativoRepository;

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
                .endereco("Rua nada")
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
        Cliente cliente = clienteRepository.save(
                Cliente.builder()
                        .nome(nome)
                        .endereco("Avenida Paris, 5987, Campina Grande - PB")
                        .codigoAcesso("12345")
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

    @AfterEach
    void tearDown() {
        ativoRepository.deleteAll();
        tipoDeAtivoRepository.deleteAll();
        administradorRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /ativos - Criar ativo")
    class CriarAtivo {

        @Test
        @DisplayName("Deve criar ativo com dados válidos")
        void deveCriarAtivoComDadosValidos() throws Exception {

            AtivoPostPutRequestDTO ativo = AtivoPostPutRequestDTO.builder()
                    .nome("Novo Ativo")
                    .descricao("Descrição do novo ativo")
                    .valor(150.0)
                    .tipo(tesouro.getNomeTipo())
                    .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativo);

            //TODO: create an exception for inexistent administrator
            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);

            String responseJsonString = driver.perform(
                            post(String.format("/usuario/%d/criar-ativo", admin.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(ativoJson))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO ativoCriado = objectMapper.readValue(
                    responseJsonString, AtivoResponseDTO.class);

            assertEquals(ativo.getNome(), ativoCriado.getNome());
            assertEquals(ativo.getValor(), ativoCriado.getValor());
        }

        @Test
        @DisplayName("Deve retornar erro 400 com dados inválidos")
        void deveRetornarErro400ComDadosInvalidos() throws Exception {
            AtivoPostPutRequestDTO ativoInvalido = AtivoPostPutRequestDTO.builder()
                    .nome("") // Nome vazio
                    .descricao("Descrição")
                    .valor(-10.0) // Valor negativo
                    .tipo(tesouro.getNomeTipo())
                    .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativoInvalido);
            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);

            driver.perform(
                            post(String.format("/usuario/%d/criar-ativo", admin.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(ativoJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Deve retornar erro 400 com código de acesso inválido")
        void deveRetornarErro400ComCodigoAcessoInvalido() throws Exception {
            AtivoPostPutRequestDTO novoAtivo = AtivoPostPutRequestDTO.builder()
                    .nome("Novo Ativo")
                    .descricao("Descrição")
                    .valor(150.0)
                    .tipo(tesouro.getNomeTipo())
                    .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                    .build();

            String ativoJson = objectMapper.writeValueAsString(novoAtivo);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);

            driver.perform(
                            post(String.format("/usuario/%d/criar-ativo", admin.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_INVALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(ativoJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

    }

    @Nested
    @DisplayName("PUT /ativos/{id} - Atualizar ativo")
    class AtualizarAtivo {

        @Test
        @DisplayName("Deve atualizar ativo existente com dados válidos")
        void deveAtualizarAtivoExistente() throws Exception {
            Ativo ativo = ativos.get(0);

            AtivoPostPutRequestDTO ativoAtualizado = AtivoPostPutRequestDTO.builder()
                    .nome("Nome Atualizado")
                    .descricao("Descrição Atualizada")
                    .valor(200.0)
                    .tipo(ativo.getTipo().getNomeTipo())
                    .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativoAtualizado);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);

            String responseJsonString = driver.perform(
                            put(String.format("/usuario/%d/atualizar-ativo/%s", admin.getId(), ativo.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(ativoJson))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO ativoRetornado = objectMapper.readValue(
                    responseJsonString, AtivoResponseDTO.class);

            assertEquals(ativoAtualizado.getNome(), ativoRetornado.getNome());
            assertEquals(ativoAtualizado.getValor(), ativoRetornado.getValor());
        }

        @Test
        @DisplayName("Deve retornar 400 ao tentar atualizar ativo inexistente")
        void deveRetornar400AoTentarAtualizarAtivoInexistente() throws Exception {
            AtivoPostPutRequestDTO ativoAtualizado = AtivoPostPutRequestDTO.builder()
                    .nome("Nome Atualizado")
                    .descricao("Descrição Atualizada")
                    .valor(200.0)
                    .tipo(tesouro.getNomeTipo())
                    .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativoAtualizado);
            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);

            driver.perform(
                            put(String.format("/usuario/%d/atualizar-ativo/%s", admin.getId(), "999999"))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(ativoJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("DELETE /ativos/{id} - Excluir ativo")
    class ExcluirAtivo {

        @Test
        @DisplayName("Deve excluir ativo existente")
        void deveExcluirAtivoExistente() throws Exception {
            Ativo ativo = ativos.get(0);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);

            driver.perform(
                            delete(String.format("/usuario/%d/excluir-ativo/%d", admin.getId(), ativo.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            assertFalse(ativoRepository.existsById(ativo.getId()));
        }

        @Test
        @DisplayName("Deve retornar 400 ao tentar excluir ativo inexistente")
        void deveRetornar400AoTentarExcluirAtivoInexistente() throws Exception {

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(
                            delete(String.format("/usuario/%d/excluir-ativo/%d", admin.getId(), 999999))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Deve retornar erro 400 com código de acesso inválido")
        void deveRetornarErro400ComCodigoAcessoInvalido() throws Exception {
            Ativo ativo = ativos.get(0);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(
                            delete(String.format("/usuario/%d/excluir-ativo/%d", admin.getId(), ativo.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_INVALIDO))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
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
                    new TypeReference<List<AtivoResponseDTO>>() {
                    }
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
                    new TypeReference<List<AtivoResponseDTO>>() {
                    }
            );

            assertEquals(3, ativosRetornados.size());

            List<String> nomesEsperados = ativos.stream()
                    .filter((ativo) -> ativo.getTipo().getNomeTipo() == TipoAtivo.TESOURO_DIRETO)
                    .map(Ativo::getNome).toList();

            List<String> nomesRetornados = ativosRetornados.stream().map(AtivoResponseDTO::getNome).toList();

            assertTrue(nomesRetornados.containsAll(nomesEsperados));
        }
    }

    @Nested
    @DisplayName("Testes para atualização da cotação do ativo")
    class AtualizacaoCotacao {

        @Test
        @DisplayName("Quando o código de acesso do administrador for inválido")
        void codigoAcessoInvalido() throws Exception {

            Ativo ativo = ativos.get(0);
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);

            String json = objectMapper.writeValueAsString(ativoPostPutRequestDTO);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(patch("/usuario/" + admin.getId() + "/atualizar-cotacao/" + ativo.getId())
                            .param("codigoAcesso", "codigoErrado")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Codigo de acesso invalido!")));
        }

        @Test
        @DisplayName("Quando o ativo não for encontrado")
        void ativoNaoEncontrado() throws Exception {

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);

            Ativo ativo = ativos.get(0);
            AtivoPostPutRequestDTO ativoPostPutRequestDTO = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);

            String json = objectMapper.writeValueAsString(ativoPostPutRequestDTO);

            driver.perform(patch("/usuario/" + admin.getId() + "/atualizar-cotacao/" + 999999)
                            .param("codigoAcesso", "123456")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("O ativo consultado nao existe!")));
        }

        @Test
        @DisplayName("Quando o tipo do ativo não permitir atualização de cotação")
        void tipoAtivoNaoPermiteAtualizacao() throws Exception {

            Ativo ativo = ativos.get(0);

            AtivoPostPutRequestDTO ativoPostPutRequestDTO = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);
            String json = objectMapper.writeValueAsString(ativoPostPutRequestDTO);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(patch("/usuario/" + admin.getId() + "/atualizar-cotacao/" + ativo.getId())
                            .param("codigoAcesso", "123456")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Somente ativos do tipo Ação ou Criptomoeda podem ter a cotação atualizada!")));
        }

        @Test
        @DisplayName("Quando a nova cotação variar menos que 1% em relação à atual")
        void cotacaoAbaixoDoMinimoPermitido() throws Exception {

            Ativo ativo = ativos.get(3);

            AtivoPostPutRequestDTO ativoPostPutRequestDTO = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);
            ativoPostPutRequestDTO.setValor(ativo.getValor() * 1.009);
            String json = objectMapper.writeValueAsString(ativoPostPutRequestDTO);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(patch("/usuario/" + admin.getId() + "/atualizar-cotacao/" + ativo.getId())
                            .param("codigoAcesso", "123456")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("A variação da cotação deve ser de no mínimo 1%.")));
        }

        @Test
        @DisplayName("Quando a cotação for atualizada com sucesso")
        void atualizacaoCotacaoSucesso() throws Exception {

            Ativo ativo = ativos.get(3);

            AtivoPostPutRequestDTO ativoPostPutRequestDTO = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);
            ativoPostPutRequestDTO.setValor(110.0);

            String json = objectMapper.writeValueAsString(ativoPostPutRequestDTO);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            String responseJsonString = driver.perform(patch("/usuario/" + admin.getId() + "/atualizar-cotacao/" + ativo.getId())
                            .param("codigoAcesso", "123456")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO resultado = objectMapper.readValue(responseJsonString, AtivoResponseDTO.class);

            assertAll(
                    () -> assertEquals(ativo.getId(), resultado.getId()),
                    () -> assertEquals(110.0, resultado.getValor()),
                    () -> assertEquals(ativo.getNome(), resultado.getNome())
            );
        }
    }

    @Nested
    @DisplayName("Testes para atualização do Status de Disponibilidade")
    class AtualizarStatusDisponibilidade {

        @Test
        @DisplayName("Quando o código de acesso do administrador for inválido")
        void codigoAcessoInvalido() throws Exception {

            Ativo ativo = ativos.get(0);
            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(patch("/usuario/" + admin.getId() + "/ativar-desativar/" + ativo.getId())
                            .param("codigoAcesso", "codigoErrado")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Codigo de acesso invalido!")));
        }

        @Test
        @DisplayName("Quando o ativo não for encontrado")
        void ativoNaoEncontrado() throws Exception {
            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(patch("/usuario/" + admin.getId() + "/ativar-desativar/" + 999999)
                            .param("codigoAcesso", "123456")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("O ativo consultado nao existe!")));
        }

        @Test
        @DisplayName("Quando indisponibilizamos o ativo")
        void quandoIndisponibilizamosOAtivo() throws Exception {

            criarAtivo("AtivoAtivo", tesouro, 100, StatusDisponibilidade.DISPONIVEL);
            Ativo ativo = ativos.get(ativos.size() - 1);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            String responseJsonString = driver.perform(patch("/usuario/" + admin.getId() + "/ativar-desativar/" + ativo.getId())
                            .param("codigoAcesso", "123456")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO ativoRetornado = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<AtivoResponseDTO>() {
                    }
            );

            assertEquals(StatusDisponibilidade.INDISPONIVEL, ativoRetornado.getStatusDisponibilidade());
        }

        @Test
        @DisplayName("Quando disponibilizamos o ativo")
        void quandoDisponibilizamosOAtivo() throws Exception {

            criarAtivo("AtivoInativo", tesouro, 100, StatusDisponibilidade.INDISPONIVEL);
            Ativo ativo = ativos.get(ativos.size() - 1);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            String responseJsonString = driver.perform(patch("/usuario/" + admin.getId() + "/ativar-desativar/" + ativo.getId())
                            .param("codigoAcesso", "123456")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO ativoRetornado = objectMapper.readValue(
                    responseJsonString,
                    new TypeReference<AtivoResponseDTO>() {
                    }
            );

            assertEquals(StatusDisponibilidade.DISPONIVEL, ativoRetornado.getStatusDisponibilidade());
        }
    }

    @Nested
    @DisplayName("Testes de validação de entrada")
    class ValidacaoEntrada {

        @Test
        @DisplayName("Deve retornar erro 400 quando parâmetro obrigatório está ausente")
        void deveRetornarErro400QuandoParametroObrigatorioAusente() throws Exception {
            AtivoPostPutRequestDTO novoAtivo = AtivoPostPutRequestDTO.builder()
                    .nome("Novo Ativo")
                    .descricao("Descrição")
                    .valor(150.0)
                    .tipo(tesouro.getNomeTipo())
                    .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                    .build();

            String ativoJson = objectMapper.writeValueAsString(novoAtivo);

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(
                            post(String.format("/usuario/%d/criar-ativo", admin.getId()))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(ativoJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Deve retornar erro 400 com JSON malformado")
        void deveRetornarErro400ComJsonMalformado() throws Exception {
            String jsonMalformado = "{ nome: 'teste', valor: }";

            Administrador admin = administradorRepository.findByNome("Admin").orElseThrow(Exception::new);
            driver.perform(
                            post(String.format("/usuario/%d/criar-ativo", admin.getId()))
                                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMalformado))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("Testes para adição de cliente interessado em ativo")
    class AdicionarInteressado {

        @Test
        @Transactional
        @DisplayName("Adicionando Interessado com sucesso!")
        void adicionandoInteressadoComSucesso() throws Exception {

            Ativo ativo = ativos.get(3);
            Cliente cliente = clientes.get(2); // Cliente com plano premium

            String json = objectMapper.writeValueAsString(ativo);

            driver.perform(
                    patch(String.format("/usuario/%d/marcar-interesse/%d", cliente.getId(), ativo.getId()))
                            .param("codigoAcesso", "123456")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)).andExpect(status().isOk());

            Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow(Exception::new);
            assertEquals(1, ativoAtualizado.getInteressadosCotacao().size());

        }
    }

    @Nested
    @DisplayName("Testes de notificação de interessados")
    class NotificacaoInteressados {

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

        private String ativarAtivo(Long idUser, Long idAtivo, boolean imprimir) throws Exception {
            var perform = driver.perform(patch("/usuario/" + idUser + "/ativar-desativar/" + idAtivo)
                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                    .contentType(MediaType.APPLICATION_JSON));

            if (imprimir) {
                perform.andDo(print());
            }

            perform.andExpect(status().isOk());

            return outContent.toString();
        }

        @Test
        @Transactional
        @DisplayName("Deve imprimir notificação para interessados quando ativo fica disponível e limpar a lista")
        void deveImprimirNotificacaoParaInteressadosELimparAListaParaNotificarUnicaVez() throws Exception {
            String output = ativarAtivo(ativoComInteressados.getId(), ativoComInteressados.getId(), true);

            assertTrue(output.contains(cliente.getNome()), "Deve conter o nome do cliente na notificação");
            assertTrue(output.contains(ativoComInteressados.getNome()), "Deve conter o nome do ativo na notificação");
            assertTrue(output.contains("disponível"), "Deve indicar que o ativo está disponível");
            assertEquals(0, ativoComInteressados.getInteressadosDisponibilidade().size());
        }

        @Test
        @Transactional
        @DisplayName("Não deve imprimir nada se não houver interessados")
        void naoDeveImprimirNotificacaoSemInteressados() throws Exception {
            String output = ativarAtivo(cliente.getId(), ativoSemInteressados.getId(), false);

            assertFalse(output.contains("Notificação para:"), "Não deve imprimir notificação quando não há interessados");
            assertEquals(0, ativoSemInteressados.getInteressadosDisponibilidade().size());
        }

        @Test
        @Transactional
        @DisplayName("Não deve imprimir notificação nem limpar interessados ao desativar ativo")
        void naoDeveNotificarNemLimparAoDesativarAtivo() throws Exception {
            ativoComInteressados.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
            ativoRepository.save(ativoComInteressados);

            assertEquals(1, ativoComInteressados.getInteressadosDisponibilidade().size(), "Lista de interessados deve continuar intacta ao desativar");

            var perform = driver.perform(patch("/usuario/" + ativoComInteressados.getId() + "/ativar-desativar/" + ativoComInteressados.getId())
                    .param("codigoAcesso", CODIGO_ACESSO_VALIDO)
                    .contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk());

            Ativo ativoAtualizado = ativoRepository.findById(ativoComInteressados.getId()).orElseThrow();
            assertEquals(1, ativoAtualizado.getInteressadosDisponibilidade().size(), "Lista de interessados deve continuar intacta ao desativar");
        }

        @Test
        @Transactional
        @DisplayName("Quando o cliente não tem plano premium")
        void planoNaoPermiteManifestarInteresse() throws Exception {

            Ativo ativo = ativos.get(3);
            Cliente cliente = clientes.get(1); // Cliente com plano normal

            String json = objectMapper.writeValueAsString(ativo);

            String responseJsonString = driver.perform(
                            patch(String.format("/usuario/%d/marcar-interesse/%d", cliente.getId(), ativo.getId()))
                                    .param("codigoAcesso", "123456")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isForbidden())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("Plano do cliente nao permite marcar interesse!", resultado.getMessage());

            Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow(Exception::new);
            assertFalse(ativoAtualizado.getInteressados().contains(cliente.getId()));
        }

        @Test
        @DisplayName("Quando tentar adicionar um interessado em um ativo que não existe")
        void adicionandoInteressadoEmAtivoInexistente() throws Exception {

            Ativo ativo = ativos.get(3);
            Cliente cliente = clientes.get(2);

            AtivoPostPutRequestDTO ativoPostPutRequestDTO = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);

            String json = objectMapper.writeValueAsString(ativoPostPutRequestDTO);

            String responseJsonString = driver.perform(
                            patch(String.format("/usuario/%d/marcar-interesse/%d", cliente.getId(), 999999))
                                    .param("codigoAcesso", "123456")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("O ativo consultado nao existe!", resultado.getMessage());
        }

        @Test
        @DisplayName("Quando tentar adicionar um interessado não cadastrado em um ativo")
        void adicionandoInteressadoInexistenteEmAtivo() throws Exception {

            Ativo ativo = ativos.get(3);

            AtivoPostPutRequestDTO ativoPostPutRequestDTO = modelMapper.map(ativo, AtivoPostPutRequestDTO.class);

            String json = objectMapper.writeValueAsString(ativoPostPutRequestDTO);

            String responseJsonString = driver.perform(
                            patch(String.format("/usuario/%d/marcar-interesse/%d", 999999, ativo.getId()))
                                    .param("codigoAcesso", "123456")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertEquals("O cliente consultado nao existe!", resultado.getMessage());
        }
    }
}