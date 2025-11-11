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
import com.application.DTO.AuditorDTO;
import com.application.DTO.StoreDTO;
import com.application.entities.AuditPlan;
import com.application.entities.Auditors;
import com.application.entities.Auditors.AvailabilityStatus;
import com.application.entities.Store;
import com.application.repository.AuditPlanRepository;
import com.application.repository.AuditorRepository;
import com.application.repository.StoreRepository;

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
		//return restTemplate.postForObject(pythonApiUrl, request, AssignmentResponseDTO.class);
		 org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
	        headers.set("Content-Type", "application/json"); // It's good practice to set this
	        headers.set("X-API-Key", pythonApiKey); // Using the key injected from properties
	        
	        // 2. Create an HttpEntity to wrap the request body (DTO) and the headers
	        HttpEntity<AssignmentRequestDTO> entity = new HttpEntity<>(request, headers);
	        
	        // 3. Define the full endpoint URL
	        String fullUrl = pythonApiUrl + "/api/process-assignments";

	        // 4. Use restTemplate.exchange() which allows sending custom headers
	        // It sends the entity, and expects a response that can be mapped to AssignmentResponseDto
	        ResponseEntity<AssignmentResponseDTO> response = restTemplate.exchange(
	            fullUrl,
	            HttpMethod.POST,
	            entity,
	            AssignmentResponseDTO.class
	        );

	        // 5. Return the body of the response
	        return response.getBody();
	}

	@Override
	public List<AuditPlan> generateAndSaveAuditPlan() {
		List<Auditors> auditors = auditorRepository.findAll();
		List<Store> stores = storeRepository.findAll();
		
		System.out.println("--- CHECKPOINT 2: Fetched " + auditors.size() + " auditors and " + stores.size() + " stores from DB ---");

        // 2. MAP
        AssignmentRequestDTO requestDto = mapEntitiesToRequestDTO(auditors, stores);
        System.out.println("--- CHECKPOINT 3: Mapped entities to DTO. Calling Python API... ---");

        // 3. CALL
        AssignmentResponseDTO predictionResponse = getAssignment(requestDto);
        System.out.println("--- CHECKPOINT 4: Successfully received response from Python API ---");

        // 4. PROCESS & SAVE
        List<AuditPlan> savedAssignments = processAndSaveAuditPlan(predictionResponse);
        System.out.println("--- CHECKPOINT 5: Finished processing and saving assignments. ---");
		AssignmentRequestDTO requestDTO = mapEntitiesToRequestDTO(auditors, stores);
		
		AssignmentResponseDTO predictionResponseDTO = getAssignment(requestDTO);
		return processAndSaveAuditPlan(predictionResponse);
	}

	@Override
	public AssignmentRequestDTO mapEntitiesToRequestDTO(List<Auditors> auditors, List<Store> stores) {
		List<AuditorDTO> auditorDTO = auditors.stream().map(auditor -> {
			AuditorDTO dto = new AuditorDTO();
			dto.setAuditorId(auditor.getId());
			dto.setLatitude(auditor.getHomeLat());
			dto.setLongitude(auditor.getHomeLon());
			Auditors.AvailabilityStatus status = auditor.getAvailabilityStatus();
			if (status == Auditors.AvailabilityStatus.AVAILABLE) {
	            dto.setAvailabilityStatus("AVAILABLE");
	        } else {
	            // Treat ON_LEAVE, UNAVAILABLE, or even null as "Unavailable" for the API.
	            dto.setAvailabilityStatus("UNAVAILABLE");
	        }
			return dto;
		}).collect(Collectors.toList());
		
		 List<StoreDTO> storeDtos = stores.stream().map(store -> {
	            StoreDTO dto = new StoreDTO();
	            dto.setStoreId(store.getName()); // Assuming name is the unique ID
	            dto.setLatitude(store.getLocationLat());
	            dto.setLongitude(store.getLocationLon());
	            Store.StoreStatus status = store.getStoreStatus();

	            // 2. Compare the enum constant.
	            if (status == Store.StoreStatus.OPEN) {
	                dto.setStoreStatus("OPEN");
	            } else {
	                // Treat CLOSED, RENOVATING, or null as "Closed" for the API.
	                dto.setStoreStatus("CLOSED");
	            }
	            return dto;
	        }).collect(Collectors.toList());
		 
		 AssignmentRequestDTO requestDTO = new AssignmentRequestDTO();
		 requestDTO.setAuditors(auditorDTO);
		 requestDTO.setStores(storeDtos);
		 return requestDTO;
	}

	@Override
	public List<AuditPlan> processAndSaveAuditPlan(AssignmentResponseDTO prediction) {
	    List<AuditPlan> savedAssignments = new ArrayList<>();

	    // This initial check is good and remains the same.
	    if (prediction == null || prediction.getData() == null || prediction.getData().getStores() == null) {
	        logger.warn("Prediction data is null or empty. Skipping assignment processing.");
	        return savedAssignments;
	    }

	    // Correctly cast the generic list from the response.
	    List<Map<String, Object>> storeResults = (List<Map<String, Object>>) (List<?>) prediction.getData().getStores();
	    logger.info("Processing {} potential store assignments from API response.", storeResults.size());

	    // Loop through each assignment suggested by the AI
	    for (Map<String, Object> storeResult : storeResults) {
	        Object storeIdObj = storeResult.get("store_id");
	        Object auditorIdObj = storeResult.get("assigned_auditor_id");

	        // Only process if the AI actually assigned an auditor and a store
	        if (auditorIdObj != null && storeIdObj != null) {
	            
	            // --- FIX 1: Correctly parse numeric IDs from the response ---
	            // The response JSON contains numbers, not strings.
	            Integer storeId = (int) ((Number) storeIdObj).longValue();
	            Integer auditorId = (int) ((Number) auditorIdObj).longValue();

	            // --- FIX 2: Use the standard findById method, not the deleted findByName ---
	            // Fetch both entities from the database using their numeric primary keys.
	            Optional<Auditors> auditorOptional = auditorRepository.findById(auditorId);
	            Optional<Store> storeOptional = storeRepository.findById(storeId);

	            // --- FIX 3: Check if both entities were actually found ---
	            if (auditorOptional.isPresent() && storeOptional.isPresent()) {
	                // Get the actual entities from the Optional wrapper.
	                Auditors auditorEntity = auditorOptional.get();
	                Store storeEntity = storeOptional.get();
	                
	                AuditPlan plan = new AuditPlan();
	                
	                // --- FIX 4: Call the correct setter methods ---
	                // Assuming your setters are named setAuditors and setStore.
	                plan.setAuditors(auditorEntity);
	                plan.setStore(storeEntity);

	                // Set default values using the correct enum constants.
	                plan.setAuditStatus(AuditPlan.AuditStatus.PLANNED);
	                plan.setAuditPriority(AuditPlan.AuditPriority.MEDIUM);

	                // Save the new assignment plan to the database.
	                savedAssignments.add(auditPlanRepository.save(plan));
	                
	            } else {
	                // Log a clear warning if an ID from the AI response doesn't exist in our DB.
	                logger.warn("Could not create assignment. Auditor or Store not found in DB for IDs: AuditorID={}, StoreID={}",
	                            auditorId, storeId);
	            }
	        }
	    }
	    return savedAssignments;
	}

	

}
