package com.ufcg.psoft.commerce.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.base.TipoDeAtivo;
import com.ufcg.psoft.commerce.dtos.ativo.AtivoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.carteira.AtivoCarteiraResponseDTO;
import com.ufcg.psoft.commerce.dtos.carteira.CarteiraPostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClientePostPutRequestDTO;
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
import com.ufcg.psoft.commerce.repositories.AdministradorRepository;
import com.ufcg.psoft.commerce.repositories.AtivoRepository;
import com.ufcg.psoft.commerce.repositories.ClienteRepository;
import com.ufcg.psoft.commerce.repositories.TipoDeAtivoRepository;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.ufcg.psoft.commerce.enums.TipoPlano.NORMAL;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Compra")
public class CompraControllerTests {

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
    @DisplayName("Testes para visualizar carteira")
    class VisualizarCarteira {

        @Test
        @DisplayName("Deve retornar a carteira do cliente com ativos")
        void quandoCarteiraPossuiAtivos() throws Exception {
            Long idCliente = clientes.get(0).getId();

            String responseJsonString = driver.perform(get("/clientes/" + idCliente + "/visualizar-carteira"))
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

            String responseJsonString = driver.perform(get("/clientes/" + idCliente + "/visualizar-carteira"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<AtivoCarteiraResponseDTO> resultado = objectMapper
                    .readValue(responseJsonString, objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, AtivoCarteiraResponseDTO.class));

            assertEquals(0, resultado.size());
        }

        @Test
        @DisplayName("Deve retornar 400 quando cliente não existir") // Mudar para 404 depois (isNotFound())
        void quandoClienteNaoExiste() throws Exception {
            Long idCliente = 99999L;

            driver.perform(get("/clientes/" + idCliente + "/visualizar-carteira"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }


    }

}