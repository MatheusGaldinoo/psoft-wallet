package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.CodigoAcessoDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoCotacaoRequestDTO;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
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

    @Autowired
    ModelMapper modelMapper;

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
                    .codigoAcesso("123456")
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativo);

            String responseJsonString = driver.perform(
                            post("/ativos")
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
                    .codigoAcesso("123456")
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativoInvalido);

            driver.perform(
                            post("/ativos")
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
                    .codigoAcesso("64321")
                    .build();

            String ativoJson = objectMapper.writeValueAsString(novoAtivo);

            driver.perform(
                            post("/ativos")
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
                    .codigoAcesso("123456")
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativoAtualizado);

            String responseJsonString = driver.perform(
                            put(String.format("/ativos/%d", ativo.getId()))
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
                    .codigoAcesso("123456")
                    .build();

            String ativoJson = objectMapper.writeValueAsString(ativoAtualizado);

            driver.perform(
                            put(String.format("/ativos/%s", "999999"))
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

            CodigoAcessoDTO dto = new CodigoAcessoDTO("123456");
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(delete(String.format("/ativos/%d", ativo.getId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            assertFalse(ativoRepository.existsById(ativo.getId()));
        }

        @Test
        @DisplayName("Deve retornar 400 ao tentar excluir ativo inexistente")
        void deveRetornar400AoTentarExcluirAtivoInexistente() throws Exception {

            CodigoAcessoDTO dto = new CodigoAcessoDTO("123456");
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(delete(String.format("/ativos/%d", 999999))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Deve retornar erro 400 com código de acesso inválido")
        void deveRetornarErro400ComCodigoAcessoInvalido() throws Exception {
            Ativo ativo = ativos.get(0);

            CodigoAcessoDTO dto = new CodigoAcessoDTO("654321");
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(delete(String.format("/ativos/%d", ativo.getId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
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
            assertEquals(TipoAtivo.TESOURO_DIRETO, ativo.getTipo().getNomeTipo());
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

    @Nested
    @DisplayName("Testes para atualização da cotação do ativo")
    class AtualizacaoCotacao {

        @Test
        @DisplayName("Quando a cotação for atualizada com sucesso")
        void atualizacaoCotacaoSucesso() throws Exception {

            Ativo ativo = ativos.get(1);

            AtivoCotacaoRequestDTO dto = new AtivoCotacaoRequestDTO();
            dto.setCodigoAcesso("123456");
            dto.setValor(110.0);
            String json = objectMapper.writeValueAsString(dto);

            String responseJsonString = driver.perform(patch("/ativos/" + ativo.getId() + "/cotacao")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO resultado = objectMapper.readValue(responseJsonString, AtivoResponseDTO.class);

            assertAll(
                    () -> assertEquals(ativo.getId(), resultado.getId()),
                    () -> assertEquals(110.0, resultado.getValor())
            );

        }

        @Test
        @DisplayName("Quando o código de acesso do administrador for inválido")
        void codigoAcessoInvalido() throws Exception {

            Ativo ativo = ativos.get(1);
            AtivoCotacaoRequestDTO dto = new AtivoCotacaoRequestDTO();
            dto.setCodigoAcesso("654321");
            dto.setValor(110.0);
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(patch("/ativos/" + ativo.getId() + "/cotacao")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Codigo de acesso invalido!")));
        }

        @Test
        @DisplayName("Quando o ativo não for encontrado")
        void ativoNaoEncontrado() throws Exception {

            AtivoCotacaoRequestDTO dto = new AtivoCotacaoRequestDTO();
            dto.setCodigoAcesso("123456");
            dto.setValor(120.0);
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(patch("/ativos/" + 999999 + "/cotacao")
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

            AtivoCotacaoRequestDTO dto = new AtivoCotacaoRequestDTO();
            dto.setCodigoAcesso("123456");
            dto.setValor(120.0);
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(patch("/ativos/" + ativo.getId() + "/cotacao")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Somente ativos do tipo Ação ou Criptomoeda podem ter a cotação atualizada!")));
        }

        @Test
        @DisplayName("Quando a nova cotação variar menos que 1% em relação à atual")
        void cotacaoAbaixoDoMinimoPermitido() throws Exception {

            Ativo ativo = ativos.get(1);

            AtivoCotacaoRequestDTO dto = new AtivoCotacaoRequestDTO();
            dto.setCodigoAcesso("123456");
            dto.setValor(ativo.getValor() * 1.009);
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(patch("/ativos/" + ativo.getId() + "/cotacao")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("A variação da cotação deve ser de no mínimo 1%.")));
        }

    }

    @Nested
    @DisplayName("Testes para atualização do Status de Disponibilidade")
    class AtualizarStatusDisponibilidade {

        @Test
        @DisplayName("Quando o código de acesso do administrador for inválido")
        void codigoAcessoInvalido() throws Exception {

            Ativo ativo = ativos.get(0);

            AtivoCotacaoRequestDTO dto = new AtivoCotacaoRequestDTO();
            dto.setValor(120.0);
            dto.setCodigoAcesso("654321");
            String json = objectMapper.writeValueAsString(dto);


            driver.perform(patch("/ativos/" + ativo.getId() + "/status")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message", containsString("Codigo de acesso invalido!")));
        }

        @Test
        @DisplayName("Quando o ativo não for encontrado")
        void ativoNaoEncontrado() throws Exception {

            CodigoAcessoDTO dto = new CodigoAcessoDTO();
            dto.setCodigoAcesso("123456");
            String json = objectMapper.writeValueAsString(dto);

            driver.perform(patch("/ativos/" + 999999 + "/status")
                            .content(json)
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

            CodigoAcessoDTO dto = new CodigoAcessoDTO();
            dto.setCodigoAcesso("123456");
            String json = objectMapper.writeValueAsString(dto);

            String responseJsonString = driver.perform(patch("/ativos/" + ativo.getId() + "/status")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            AtivoResponseDTO ativoRetornado = objectMapper.readValue(responseJsonString, new TypeReference<AtivoResponseDTO>() {});

            assertEquals(StatusDisponibilidade.INDISPONIVEL, ativoRetornado.getStatusDisponibilidade());
        }

        @Test
        @DisplayName("Quando disponibilizamos o ativo")
        void quandoDisponibilizamosOAtivo() throws Exception {

            criarAtivo("AtivoInativo", tesouro, 100, StatusDisponibilidade.INDISPONIVEL);
            Ativo ativo = ativos.get(ativos.size() - 1);

            CodigoAcessoDTO dto = new CodigoAcessoDTO();
            dto.setCodigoAcesso("123456");
            String json = objectMapper.writeValueAsString(dto);

            String responseJsonString = driver.perform(patch("/ativos/" + ativo.getId() + "/status")
                            .content(json)
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
                    .tipo(tesouro.getNomeTipo())
                    .statusDisponibilidade(StatusDisponibilidade.DISPONIVEL)
                    .codigoAcesso("123456")
                    .build();

            String ativoJson = objectMapper.writeValueAsString(novoAtivo);

            driver.perform(
                            post("/ativos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(ativoJson))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("Deve retornar erro 400 com JSON malformado")
        void deveRetornarErro400ComJsonMalformado() throws Exception {
            String jsonMalformado = "{ nome: 'teste', valor: }";

            driver.perform(
                            post("/ativos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMalformado))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

}