package com.application.service;

import com.application.entities.AuditPlan;

public interface AuditPlanService {

	AuditPlan createPlanWithAssignment(AuditPlan planDetails, int targetStoreId);
}
