package com.ufcg.psoft.commerce.services.administrador;

import com.ufcg.psoft.commerce.exceptions.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.repositories.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministradorServiceImpl implements AdministradorService {

    @Autowired
    AdministradorRepository administradorRepository;

    @Override
    public void validarCodigoAcesso(String codigoAcesso) {
        administradorRepository.findByCodigoAcesso(codigoAcesso).orElseThrow(CodigoDeAcessoInvalidoException::new);
    }
}