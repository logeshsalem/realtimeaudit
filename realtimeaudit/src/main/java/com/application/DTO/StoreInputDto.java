package com.application.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class StoreInputDto {

	@JsonProperty("store_id")
	private String storeId;
	
	@JsonProperty("latitude")
	private Double latitude;
	
	@JsonProperty("longitude")
	private Double longitude;
	
	@JsonProperty("store_status")
	private String storeStatus;
	
	
	
}


