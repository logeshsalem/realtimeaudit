package com.application.service;

import java.util.List;

import com.application.DTO.AssignmentRequestDTO;
import com.application.DTO.AssignmentResponseDTO;
import com.application.entities.AuditPlan;
import com.application.entities.Auditors;
import com.application.entities.Store;

public interface AuditPlanService {
	
	AssignmentResponseDTO getAssignment(AssignmentRequestDTO request);
	
	List<AuditPlan> generateAndSaveAuditPlan();
	
	AssignmentRequestDTO mapEntitiesToRequestDTO(List<Auditors> auditors, List<Store> stores);
	
	List<AuditPlan> processAndSaveAuditPlan(AssignmentResponseDTO prediction);

}
