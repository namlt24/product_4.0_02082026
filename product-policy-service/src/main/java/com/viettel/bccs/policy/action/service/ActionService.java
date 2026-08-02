package com.viettel.bccs.policy.action.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.action.dto.response.ActionResponse;
import com.viettel.bccs.policy.action.entity.ActionEntity;
import com.viettel.bccs.policy.action.mapper.ActionMapper;
import com.viettel.bccs.policy.action.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActionService {

    private final ActionRepository repository;
    private final ActionMapper mapper;


}