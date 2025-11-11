package com.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.application.entities.AuditPlan;

@Repository
public interface AuditPlanRepository extends JpaRepository<AuditPlan, Integer>{

}
