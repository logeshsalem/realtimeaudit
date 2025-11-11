package com.application.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.DTO.AssignmentRequestDTO;
import com.application.DTO.AssignmentResponseDTO;
import com.application.entities.AuditPlan;
import com.application.entities.Auditors;
import com.application.entities.Store;
import com.application.service.AuditPlanService;

@RestController
@RequestMapping("/api")
public class AuditPlanController {
	
	@Autowired
    private AuditPlanService auditPlanService;

    @PostMapping("/process")
    public ResponseEntity<List<AuditPlan>> generateAuditPlan() {
        try {
            List<AuditPlan> newPlan = auditPlanService.generateAndSaveAuditPlan();
            return ResponseEntity.ok(newPlan);
        } catch (Exception e) {
        	return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Error-Message", e.getMessage()) 
                    .build();
        }
    }

}
