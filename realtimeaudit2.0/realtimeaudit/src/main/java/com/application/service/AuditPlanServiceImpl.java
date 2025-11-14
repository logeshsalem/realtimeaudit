package com.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.application.DTO.AssignmentRequestDTO;
import com.application.DTO.AssignmentResponseDTO;
import com.application.DTO.AuditPlanDTO;
import com.application.DTO.AuditPlanResponseDTO;
import com.application.DTO.AuditorDTO;
import com.application.DTO.StoreDTO;
import com.application.entities.AuditPlan;
import com.application.entities.Auditors;
import com.application.entities.Store;
import com.application.repository.AuditPlanRepository;
import com.application.repository.AuditorRepository;
import com.application.repository.StoreRepository;

// Import these two classes for printing the JSON
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuditPlanServiceImpl implements AuditPlanService{
	 
	@Value("${python.api.url}")
	private String pythonApiUrl;
	
	@Value("${python.api.key}")
	private String pythonApiKey;
	 
	private static final Logger logger = LoggerFactory.getLogger(AuditPlanServiceImpl.class);
	 
	@Autowired
	private RestTemplate restTemplate;
	 
	@Autowired
	private AuditorRepository auditorRepository;
	 
	@Autowired
	private StoreRepository storeRepository;
	 
	@Autowired
	private AuditPlanRepository auditPlanRepository;
	 
	@Override
	public AssignmentResponseDTO getAssignment(AssignmentRequestDTO request) {
	    // --- 1. PRINTING THE OUTGOING JSON (as before) ---
	    ObjectMapper objectMapper = new ObjectMapper(); // Helper to convert objects to JSON
	    try {
	        String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
	        System.out.println("=========================================================");
	        System.out.println("SENDING JSON PAYLOAD TO PYTHON API:");
	        System.out.println(jsonPayload);
	        System.out.println("=========================================================");
	    } catch (JsonProcessingException e) {
	        logger.error("Error converting request DTO to JSON", e);
	    }
	    
	    // --- 2. MAKING THE API CALL (as before) ---
	    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
	    headers.set("Content-Type", "application/json");
	    headers.set("X-API-Key", pythonApiKey);
	    
	    HttpEntity<AssignmentRequestDTO> entity = new HttpEntity<>(request, headers);
	    String fullUrl = pythonApiUrl + "/api/process-assignments";

	    ResponseEntity<AssignmentResponseDTO> response = restTemplate.exchange(
	        fullUrl,
	        HttpMethod.POST,
	        entity,
	        AssignmentResponseDTO.class
	    );

	    // --- 3. NEW: PRINTING THE RECEIVED JSON ---
	    try {
	        // Get the parsed response body and convert it back to a pretty JSON string
	        String receivedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody());
	        System.out.println("=========================================================");
	        System.out.println("RECEIVED JSON RESPONSE FROM PYTHON API:");
	        System.out.println(receivedJson);
	        System.out.println("=========================================================");
	    } catch (JsonProcessingException e) {
	        logger.error("Error converting response DTO to JSON for printing", e);
	    }

	    // --- 4. RETURNING THE RESPONSE (as before) ---
	    return response.getBody();
	}

	@Override
	public List<AuditPlan> generateAndSaveAuditPlan() {
	    logger.info("--- Starting generateAndSaveAuditPlan ---");
	    
		List<Auditors> auditors = auditorRepository.findAll();
		List<Store> stores = storeRepository.findAll();
		logger.info("Fetched {} auditors and {} stores from DB.", auditors.size(), stores.size());

        // MAP entities to DTO
        AssignmentRequestDTO requestDto = mapEntitiesToRequestDTO(auditors, stores);
        logger.info("Mapped entities to DTO. Calling Python API...");

        // CALL the Python API
        AssignmentResponseDTO predictionResponse = getAssignment(requestDto);
        logger.info("Successfully received response from Python API.");

        // PROCESS & SAVE the results
        List<AuditPlan> savedAssignments = processAndSaveAuditPlan(predictionResponse);
        logger.info("Finished processing and saving {} new assignments.", savedAssignments.size());

        // --- FIX 1: Removed redundant/duplicated code from this method ---
		return savedAssignments;
	}

	@Override
	public AssignmentRequestDTO mapEntitiesToRequestDTO(List<Auditors> auditors, List<Store> stores) {
		List<AuditorDTO> auditorDTOs = auditors.stream().map(auditor -> {
			AuditorDTO dto = new AuditorDTO();
			dto.setAuditorId(auditor.getId());
			dto.setLatitude(auditor.getHomeLat());
			dto.setLongitude(auditor.getHomeLon());
			
			Auditors.AvailabilityStatus status = auditor.getAvailabilityStatus();
			
			// --- FIX 2: Corrected String case to match Python validation ---
			if (status == Auditors.AvailabilityStatus.AVAILABLE) {
	            dto.setAvailabilityStatus("Available"); // Must be Title Case
	        } else {
	            dto.setAvailabilityStatus("Unavailable"); // Must be Title Case
	        }
			return dto;
		}).collect(Collectors.toList());
		
		 List<StoreDTO> storeDtos = stores.stream().map(store -> {
	            StoreDTO dto = new StoreDTO();
	            // --- FIX 3: Changed from getName() to getId() to use numeric ID ---
	            dto.setStoreId(store.getId()); 
	            dto.setLatitude(store.getLocationLat());
	            dto.setLongitude(store.getLocationLon());
	            
	            Store.StoreStatus status = store.getStoreStatus();

	            // --- FIX 2: Corrected String case to match Python validation ---
	            if (status == Store.StoreStatus.OPEN) {
	                dto.setStoreStatus("Open"); // Must be Title Case
	            } else {
	                dto.setStoreStatus("Closed"); // Must be Title Case
	            }
	            return dto;
	        }).collect(Collectors.toList());
		 
		 AssignmentRequestDTO requestDTO = new AssignmentRequestDTO();
		 requestDTO.setAuditors(auditorDTOs);
		 requestDTO.setStores(storeDtos);
		 return requestDTO;
	}

	@Override
	public List<AuditPlan> processAndSaveAuditPlan(AssignmentResponseDTO prediction) {
	    List<AuditPlan> savedAssignments = new ArrayList<>();

	    if (prediction == null || prediction.getData() == null || prediction.getData().getStores() == null) {
	        logger.warn("Prediction data is null or empty. Skipping assignment processing.");
	        return savedAssignments;
	    }

	    List<Map<String, Object>> storeResults = (List<Map<String, Object>>) (List<?>) prediction.getData().getStores();
	    logger.info("Processing {} potential store assignments from API response.", storeResults.size());

	    for (Map<String, Object> storeResult : storeResults) {
	        Object storeIdObj = storeResult.get("store_id");
	        Object auditorIdObj = storeResult.get("assigned_auditor_id");

	        if (auditorIdObj != null && storeIdObj != null) {
	            
	            // --- FIX 4: Changed from Integer to Long to match repository method ---
	            int storeId = (int) ((Number) storeIdObj).longValue();
	            int  auditorId = (int) ((Number) auditorIdObj).longValue();

	            Optional<Auditors> auditorOptional = auditorRepository.findById(auditorId);
	            Optional<Store> storeOptional = storeRepository.findById(storeId);

	            if (auditorOptional.isPresent() && storeOptional.isPresent()) {
	                Auditors auditorEntity = auditorOptional.get();
	                Store storeEntity = storeOptional.get();
	                
	                AuditPlan plan = new AuditPlan();
	                
	                plan.setAuditors(auditorEntity);
	                plan.setStore(storeEntity);

	                plan.setAuditStatus(AuditPlan.AuditStatus.PLANNED);
	                plan.setAuditPriority(AuditPlan.AuditPriority.MEDIUM);

	                savedAssignments.add(auditPlanRepository.save(plan));
	                
	            } else {
	                logger.warn("Could not create assignment. Auditor or Store not found in DB for IDs: AuditorID={}, StoreID={}",
	                            auditorId, storeId);
	            }
	        }
	    }
	    return savedAssignments;
	}

	@Override
	public AuditPlan reassignStore(Store storeToReassign, List<Auditors> candidateAuditors) {
		logger.info("Attempting to reassign store ID: {}. Found {} candidate auditors.", storeToReassign.getId(), candidateAuditors.size());

        if (candidateAuditors.isEmpty()) {
            logger.warn("No available auditors to reassign store ID: {}. Un-assigning the store.", storeToReassign.getId());
            // Find the old assignment and delete it, as no replacement is possible.
            auditPlanRepository.findByStore(storeToReassign).ifPresent(oldPlan -> {
            	auditPlanRepository.delete(oldPlan);
            });
            return null; // Return null to indicate no new assignment was made.
        }

        // 1. Create a focused request for the Python API
        AssignmentRequestDTO requestDTO = mapSingleStoreToRequestDTO(storeToReassign, candidateAuditors);

        // 2. Call the Python API
        AssignmentResponseDTO prediction = getAssignment(requestDTO);

        // 3. Process the response to find the new auditor
        if (prediction != null && prediction.getData() != null && prediction.getData().getStores() != null) {
            List<Map<String, Object>> storeResults = (List<Map<String, Object>>) (List<?>) prediction.getData().getStores();
            
            if (!storeResults.isEmpty()) {
                Map<String, Object> result = storeResults.get(0); // We only sent one store
                Object newAuditorIdObj = result.get("assigned_auditor_id");

                if (newAuditorIdObj != null) {
                    int newAuditorId = (int) ((Number) newAuditorIdObj).longValue();
                    
                    // Find the new auditor entity
                    Optional<Auditors> newAuditorOptional = auditorRepository.findById(newAuditorId);
                    
                    if (newAuditorOptional.isPresent()) {
                        // Find the original assignment plan for this store
                        Optional<AuditPlan> existingPlanOptional = auditPlanRepository.findByStore(storeToReassign);
                        if (existingPlanOptional.isPresent()) {
                            AuditPlan planToUpdate = existingPlanOptional.get();
                            planToUpdate.setAuditors(newAuditorOptional.get()); // Update with the new auditor
                            logger.info("Successfully reassigned store ID: {} to new auditor ID: {}", storeToReassign.getId(), newAuditorId);
                            return auditPlanRepository.save(planToUpdate); // Save and return the updated plan
                        }
                    }
                }
            }
        }
        
        // If the AI failed to assign anyone, delete the old assignment.
        logger.warn("AI did not assign a new auditor for store ID: {}. Un-assigning.", storeToReassign.getId());
        auditPlanRepository.findByStore(storeToReassign).ifPresent(oldPlan -> {
        	auditPlanRepository.delete(oldPlan);
        });
        return null;
	}
	
	private AssignmentRequestDTO mapSingleStoreToRequestDTO(Store store, List<Auditors> auditors) {
        // This is a simplified version of your main mapping method.
        List<AuditorDTO> auditorDTOs = auditors.stream().map(auditor -> {
           AuditorDTO dto = new AuditorDTO();
           dto.setAuditorId(auditor.getId());
           dto.setLatitude(auditor.getHomeLat());
           dto.setLongitude(auditor.getHomeLon());
           dto.setAvailabilityStatus("Available"); // We know they are all available
			return dto;
		}).collect(Collectors.toList());
		
		StoreDTO storeDto = new StoreDTO();
		storeDto.setStoreId(store.getId());
		storeDto.setLatitude(store.getLocationLat());
		storeDto.setLongitude(store.getLocationLon());
		storeDto.setStoreStatus("Open"); // Assuming we only reassign open stores

		AssignmentRequestDTO requestDTO = new AssignmentRequestDTO();
		requestDTO.setAuditors(auditorDTOs);
		requestDTO.setStores(List.of(storeDto)); // Create a list with just one store
		return requestDTO;
   }

	@Override
	public AuditPlanResponseDTO updateAuditPlanStatus(int auditPlanId, AuditPlanDTO updateDTO) {
	    AuditPlan existingPlan = auditPlanRepository.findById(auditPlanId)
	            .orElseThrow(() -> new RuntimeException("AuditPlan not found with ID: " + auditPlanId));

	    if (updateDTO.getAuditStatus() != null) {
	        existingPlan.setAuditStatus(updateDTO.getAuditStatus());
	    }
	    if (updateDTO.getAuditPriority() != null) {
	        existingPlan.setAuditPriority(updateDTO.getAuditPriority());
	    }

	    // Save the entity as before
	    AuditPlan savedPlan = auditPlanRepository.save(existingPlan);

	    // --- NEW: Convert the saved entity to a DTO before returning ---
	    // This happens WHILE THE TRANSACTION IS STILL OPEN, so lazy loading works.
	    return convertToDTO(savedPlan);
	}

	// Add this private helper method to your service class
	public AuditPlanResponseDTO convertToDTO(AuditPlan plan) {
	    AuditPlanResponseDTO dto = new AuditPlanResponseDTO();
	    dto.setAuditId(plan.getId());
	    dto.setAuditStatus(plan.getAuditStatus().name()); // .name() converts enum to string
	    dto.setAuditPriority(plan.getAuditPriority().name());
	    
	    // By calling getAuditors() and getStore() here, we trigger the lazy loading
	    // and get the real data before the transaction closes.
	    if (plan.getAuditors() != null) {
	        dto.setAuditorId(plan.getAuditors().getId());
	        dto.setAuditorName(plan.getAuditors().getName());
	    }
	    if (plan.getStore() != null) {
	        dto.setStoreId(plan.getStore().getId());
	        dto.setStoreName(plan.getStore().getName());
	    }
	    
	    return dto;
	}
}