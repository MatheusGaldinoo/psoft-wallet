package com.ufcg.psoft.commerce.config;

import com.ufcg.psoft.commerce.dtos.cliente.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.models.Cliente;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(ClientePostPutRequestDTO.class, Cliente.class)
                .addMappings(mapper -> mapper.skip(Cliente::setId));

        return modelMapper;
    }

}
