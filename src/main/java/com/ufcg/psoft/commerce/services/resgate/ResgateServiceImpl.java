package com.ufcg.psoft.commerce.services.resgate;

import com.ufcg.psoft.commerce.dtos.ativo.AtivoResponseDTO;
import com.ufcg.psoft.commerce.dtos.cliente.ClienteResponseDTO;
import com.ufcg.psoft.commerce.dtos.resgate.AtualizarStatusResgateDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgatePostPutRequestDTO;
import com.ufcg.psoft.commerce.dtos.resgate.ResgateResponseDTO;
import com.ufcg.psoft.commerce.enums.DecisaoAdministrador;
import com.ufcg.psoft.commerce.enums.EstadoResgate;
import com.ufcg.psoft.commerce.exceptions.ResgateNaoConfirmadoException;
import com.ufcg.psoft.commerce.exceptions.ResgateNaoEncontradoException;
import com.ufcg.psoft.commerce.exceptions.ResgateNaoPendenteException;
import com.ufcg.psoft.commerce.exceptions.ResgateNaoPertenceAoClienteException;
import com.ufcg.psoft.commerce.loggers.Logger;
import com.ufcg.psoft.commerce.models.transacao.Resgate;
import com.ufcg.psoft.commerce.models.usuario.Cliente;
import com.ufcg.psoft.commerce.repositories.ResgateRepository;
import com.ufcg.psoft.commerce.services.administrador.AdministradorService;
import com.ufcg.psoft.commerce.services.ativo.AtivoService;
import com.ufcg.psoft.commerce.services.carteira.CarteiraService;
import com.ufcg.psoft.commerce.services.cliente.ClienteService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResgateServiceImpl implements ResgateService {

    @Autowired
    ResgateRepository resgateRepository;

    @Autowired
    ClienteService clienteService;

    @Autowired
    AtivoService ativoService;

    @Autowired
    CarteiraService carteiraService;

    @Autowired
    AdministradorService administradorService;

    @Autowired
    ModelMapper modelMapper;


    @Override
    public ResgateResponseDTO solicitarResgate(Long idCliente, ResgatePostPutRequestDTO dto) {
        // Recuperar cliente e ativo
        Cliente cliente = clienteService.buscarPorId(idCliente);
        // TODO - No geral, é melhor tratar com entidades ou DTO entre os Services? Aqui parece melhor entidade...
        AtivoResponseDTO ativo = ativoService.recuperar(dto.getIdAtivo());

        carteiraService.validarQuantidadeDisponivel(idCliente, dto.getIdAtivo(), dto.getQuantidade());

        Resgate resgate = Resgate.builder()
                .idCliente(idCliente)
                .idAtivo(dto.getIdAtivo())
                .quantidade(dto.getQuantidade())
                .precoUnitario(ativo.getValor())
                .valorTotal(ativo.getValor() * dto.getQuantidade())
                .dataSolicitacao(LocalDateTime.now())
                .build();

        resgateRepository.save(resgate);
        clienteService.salvar(cliente);

        return modelMapper.map(resgate, ResgateResponseDTO.class);
    }

    @Override
    @Transactional
    public ResgateResponseDTO atualizarStatusResgate(Long idResgate, AtualizarStatusResgateDTO dto) {
        administradorService.validarCodigoAcesso(dto.getCodigoAcesso());

        Resgate resgate = resgateRepository.findById(idResgate).orElseThrow(ResgateNaoEncontradoException::new);

        if (resgate.getEstadoAtual() != EstadoResgate.SOLICITADO) {
            throw new ResgateNaoPendenteException();
        }

        ClienteResponseDTO clienteDto = clienteService.recuperar(resgate.getIdCliente());
        AtivoResponseDTO ativoDto = ativoService.recuperar(resgate.getIdAtivo());

        if (dto.getEstado() == DecisaoAdministrador.APROVADO) {
            // Para Confirmado
            resgate.modificarEstadoResgate();
            resgate.setDataFinalizacao(LocalDateTime.now());

            Logger.alertUser(clienteDto.getNome(),
                    String.format("Seu resgate do ativo '%s' foi aprovado!", ativoDto.getNome()));

            executarResgate(resgate.getIdCliente(), idResgate);
        } else {
            resgateRepository.delete(resgate);

            Logger.alertUser(clienteDto.getNome(),
                    String.format("Seu resgate do ativo '%s' foi rejeitado!", ativoDto.getNome()));
        }

        resgateRepository.save(resgate);

        return modelMapper.map(resgate, ResgateResponseDTO.class);
    }


    @Transactional
    @Override
    public ResgateResponseDTO executarResgate(Long idCliente, Long idResgate) {
        Resgate resgate = resgateRepository.findById(idResgate).orElseThrow(ResgateNaoEncontradoException::new);

        if (!resgate.getIdCliente().equals(idCliente)) {
            throw new ResgateNaoPertenceAoClienteException();
        }

        if (resgate.getEstadoAtual() != EstadoResgate.CONFIRMADO) {
            throw new ResgateNaoConfirmadoException();
        }

        carteiraService.aplicarResgate(idCliente, resgate.getIdAtivo(), resgate.getQuantidade());

        //Para EmConta
        resgate.modificarEstadoResgate();

        resgate.setDataFinalizacao(LocalDateTime.now());

        resgateRepository.save(resgate);

        return modelMapper.map(resgate, ResgateResponseDTO.class);
    }

    // Incompleto para testes
    @Override
    public List<ResgateResponseDTO> listarResgatesDoCliente(Long idCliente, String status, String periodoInicio, String periodoFim) {
        List<Resgate> resgates = resgateRepository.findByIdCliente(idCliente);
        return resgates.stream().map(resgate -> modelMapper.map(resgate, ResgateResponseDTO.class)).toList();
    }

    @Override
    public ResgateResponseDTO consultarResgate(Long idResgate) {
        return modelMapper.map(resgateRepository.findById(idResgate), ResgateResponseDTO.class);
    }

    // Todos atributos aqui são filtros da última US
    @Override
    public List<ResgateResponseDTO> listarResgates(Long clienteId, String status, String periodoInicio, String periodoFim) {
        return List.of();
    }
}
