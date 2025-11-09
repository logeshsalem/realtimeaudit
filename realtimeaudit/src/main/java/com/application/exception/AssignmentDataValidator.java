package com.application.exception;

import org.springframework.stereotype.Component;

import com.application.DTO.AuditorInputDto;
import com.application.DTO.StoreInputDto;

@Component
public class AssignmentDataValidator {

	public void validateAuditor(AuditorInputDto auditor) {
		if(auditor.getAuditorId() == null) {
			throw new ValidationErrorException("Auditor missing required field: auditor_id");
		}
		if(auditor.getLatitude() == null || auditor.getLongitude() == null) {
			throw new ValidationErrorException("Auditor missing required fields: latitude or longitude");
		}
	}

	public void validateStore(StoreInputDto store) {
		if(store.getStoreId() == null) {
			throw new ValidationErrorException("Auditor missing required field: auditor_id");
		}
		if(store.getLatitude() == null || store.getLongitude() == null) {
			throw new ValidationErrorException("Auditor missing required fields: latitude or longitude");
		}
	}
}
