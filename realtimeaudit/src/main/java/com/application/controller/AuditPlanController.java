package com.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.entities.AuditPlan;
import com.application.service.AuditPlanService;

@RestController
@RequestMapping("/api")
public class AuditPlanController {

	private final AuditPlanService auditPlanService;
	
	@Autowired
	public AuditPlanController(AuditPlanService auditPlanService) {
		this.auditPlanService = auditPlanService;
	}
	
	
//	@PostMapping("/auditplan")
//	public ResponseEntity<AuditPlan> saveAuditPlan(@RequestBody AuditPlan auditPlan){
//		try {
//			AuditPlan createAuditPlan = auditPlanService.saveAuditPlan(auditPlan);
//			return new ResponseEntity<>(createAuditPlan, HttpStatus.CREATED);
//		}catch (Exception e) {
//			return  ResponseEntity
//					.status(HttpStatus.INTERNAL_SERVER_ERROR)
//		            .header("Error-Message", e.getMessage()) 
//		            .build();
//		}
//	}
	
	@PostMapping("/auditplan")
	public ResponseEntity<AuditPlan> createAuditPlan(@RequestBody AuditPlan auditPlan, 
			@RequestParam int targetStoreId){
		try {
			AuditPlan assignedPlan = auditPlanService.createPlanWithAssignment(auditPlan, targetStoreId);
			return new ResponseEntity<>(assignedPlan, HttpStatus.CREATED);
		}catch (Exception e) {
			System.err.println("Assignment Orchestration Failed: " +e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}
