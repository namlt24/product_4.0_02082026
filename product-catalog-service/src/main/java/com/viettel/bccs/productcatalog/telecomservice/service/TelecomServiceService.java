package com.viettel.bccs.productcatalog.telecomservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.productcatalog.telecomservice.dto.response.TelecomServiceDTO;
import com.viettel.bccs.productcatalog.telecomservice.mapper.TelecomServiceMapper;
import com.viettel.bccs.productcatalog.telecomservice.repository.TelecomServiceRepository;
import com.viettel.bccs.productcatalog.utils.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TelecomServiceService {

    private final TelecomServiceRepository repository;
    private final TelecomServiceMapper mapper;

    public TelecomServiceDTO getTelServiceByAlias(String alias) {
        RequestValidator.requireNotBlank(alias, "alias", "BCCS-PRODUCT-VALIDATE-0000");
        return mapper.toDto(repository.getTelServiceByAlias(alias));
    }
}
