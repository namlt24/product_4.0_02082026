package com.viettel.bccs.policy.ref.refproductpackagefee.service;

import org.springframework.stereotype.Service;

import com.viettel.bccs.policy.ref.refproductpackagefee.mapper.RefProductPackageFeeMapper;
import com.viettel.bccs.policy.ref.refproductpackagefee.repository.RefProductPackageFeeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefProductPackageFeeService {

    private final RefProductPackageFeeRepository refProductPackageFeeRepository;
    private final RefProductPackageFeeMapper refProductPackageFeeMapper;


}