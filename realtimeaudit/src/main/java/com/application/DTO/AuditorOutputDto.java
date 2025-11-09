package com.application.DTO;

import com.application.entities.Auditors;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditorOutputDto {

	@JsonProperty("auditor_id")
	private int auditorId;
	
	@JsonProperty("latitude")
	private Double latitude;
	
	@JsonProperty("longitude")
	private Double longitude;
	
	@JsonProperty("availability_status")
	private String availabilityStatus;
	
	@JsonProperty("current_assigned_hours")
	private Double currentAssignedHours;
	
	public static AuditorOutputDto fromEntity(Auditors auditor) {
        AuditorOutputDto dto = new AuditorOutputDto();
        dto.auditorId = auditor.getId();
        // Map Java JPA fields to Python-required fields
        dto.latitude = auditor.getHomeLat();
        dto.longitude = auditor.getHomeLon();
        dto.availabilityStatus = auditor.getAvailabilityStatus().toString();
        dto.currentAssignedHours = (double) auditor.getCurrentAssignedHours();
        return dto;
    }
}
