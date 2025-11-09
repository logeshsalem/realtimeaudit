package com.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.application.DTO.AssignmentRequest;
import com.application.DTO.AuditorOutputDto;
import com.application.DTO.ProcessAssignmentRequest;
import com.application.entities.AuditPlan;
import com.application.entities.Auditors;
import com.application.entities.Store;
import com.application.exception.AssignmentSuggestion;
import com.application.repository.AuditPlanRepository;

@Service
public class AuditPlanServiceImpl implements AuditPlanService{
	
	
	private final AuditPlanRepository auditPlanRepository;
	private final AuditorService auditorService;
	private final StoreService storeService;
	private final RestTemplate restTemplate;
	
	@Value("${service.python.ai-url}")
	private String pythonAiUrl;
	
	@Autowired
	public AuditPlanServiceImpl(AuditPlanRepository auditPlanRepository, 
			AuditorService auditorService, StoreService storeService) {
		this.auditPlanRepository = auditPlanRepository;
		this.auditorService = auditorService;
		this.storeService = storeService;
		this.restTemplate = new RestTemplate();
	}

	@Override
	public AuditPlan createPlanWithAssignment(AuditPlan planDetails, int targetStoreId) {
		
		Store targetStore = storeService.findStoreById(targetStoreId)
				.orElseThrow(() -> new RuntimeException("Target store (ID: "+ targetStoreId+" ) not found."));
		
		List<Auditors> availableAuditors = auditorService.findAvailableAuditors();
		
		List<Store> allStores = storeService.findAllStores();
		
		if(availableAuditors.isEmpty()) {
			throw new RuntimeException("Assignment failed: No available auditors found to assign the audit");
		}
		
		List<AuditorOutputDto> availableAuditorOutputDtos = availableAuditors.stream()
				.map(AuditorOutputDto::fromEntity).collect(Collectors.toList());
		
		AssignmentRequest request = new AssignmentRequest();
		request.setAvailableAuditors(availableAuditors);
		request.setAllStores(allStores);
		request.setAuditPriority(planDetails.getAuditPriority());
		request.setTargetStoreId(targetStoreId);
		System.out.println("calling to "+pythonAiUrl+" suggest assignment");
		
		AssignmentSuggestion suggestion;
		try {
			suggestion = restTemplate.postForObject(pythonAiUrl+"/process-assignments", request, AssignmentSuggestion.class);
		}catch (Exception e) {
			System.err.println("Error calling python AI service "+e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Assignment prediction failed due to external service error.",e);
		}
		
		if(suggestion == null || suggestion.getSuggestedAuditorid() == 0) {
			throw new RuntimeException("AI service returned on invalid or empty suggestion");
		}
		
		Auditors choseAuditors = auditorService.findByAuditorsId(suggestion.getSuggestedAuditorid())
				.orElseThrow(() -> new RuntimeException("Suggested Auditor id ( "+suggestion.getSuggestedAuditorid()
				+") not found in DB after AI prediction."));
		
		planDetails.setStore(targetStore);
		planDetails.setAuditors(choseAuditors);
		
		System.out.println("assignment success: auditor Id "+choseAuditors.getId()+" assigned");
		return auditPlanRepository.save(planDetails);
	}
	

	

}
