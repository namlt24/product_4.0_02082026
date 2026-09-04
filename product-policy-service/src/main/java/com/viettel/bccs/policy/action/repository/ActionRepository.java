package com.viettel.bccs.policy.action.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.action.entity.ActionEntity;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, String> {

}