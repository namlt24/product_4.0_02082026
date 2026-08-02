package com.viettel.bccs.policy.action.repository;

import com.viettel.bccs.policy.action.entity.ActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, String> {

}