package com.viettel.bccs.policy.ref.refproductpackagefee.service;

import com.viettel.bccs.policy.ref.refproductpackagefee.mapper.RefProductPackageFeeMapper;
import com.viettel.bccs.policy.ref.refproductpackagefee.repository.RefProductPackageFeeRepository;
import com.viettel.bccs.policy.utils.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefProductPackageFeeService {

    private final RefProductPackageFeeRepository refProductPackageFeeRepository;
    private final RefProductPackageFeeMapper refProductPackageFeeMapper;


}