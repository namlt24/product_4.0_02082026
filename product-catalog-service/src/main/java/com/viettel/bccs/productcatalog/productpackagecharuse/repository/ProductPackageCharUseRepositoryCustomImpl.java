package com.viettel.bccs.productcatalog.productpackagecharuse.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productpackagecharuse.entity.ProductPackageCharUseEntity;
import com.viettel.bccs.productcatalog.utils.Const;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductPackageCharUseRepositoryCustomImpl implements ProductPackageCharUseRepositoryCustom {

    private final EntityManager entityManager;


}