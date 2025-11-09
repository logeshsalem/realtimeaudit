package com.application.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AuditorInputDto {
	
	@JsonProperty("auditor_id")
	private String auditorId;
	
	@JsonProperty("latitude")
	private Double latitude;
	
	@JsonProperty("longitude")
	private Double longitude;
	
	@JsonProperty("availability_status")
	private String availabilityStatus;
	
	

}
