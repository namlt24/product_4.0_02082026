package com.viettel.bccs.policy.action.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.policy.action.mapper.ActionMapper;
import com.viettel.bccs.policy.action.repository.ActionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActionService {

    private final ActionRepository repository;
    private final ActionMapper mapper;


}