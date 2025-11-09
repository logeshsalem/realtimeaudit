package com.application.DTO;

import java.util.List;

import com.application.entities.AuditPlan.AuditPriority;
import com.application.entities.Auditors;
import com.application.entities.Store;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AssignmentRequest {

	@JsonProperty("auditors")
	private List<Auditors> availableAuditors;
	
	@JsonProperty("stores")
	private List<Store> allStores;
	
	private int targetStoreId;
	
	private String requiredAuditType;
	
	private AuditPriority auditPriority;
}
