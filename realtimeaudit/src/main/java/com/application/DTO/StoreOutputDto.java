package com.application.DTO;

import com.application.entities.Store;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StoreOutputDto {
	
	@JsonProperty("store_id")
    private int storeId;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("store_status")
    private String storeStatus;
    
    @JsonProperty("address")
    private String address;

    @JsonProperty("name")
    private String name;
    
    // --- Static constructor to convert JPA Entity to DTO ---
    public static StoreOutputDto fromEntity(Store store) {
        StoreOutputDto dto = new StoreOutputDto();
        dto.storeId = store.getId();
        // Map Java JPA fields to Python-required fields
        dto.latitude = store.getLocationLat();
        dto.longitude = store.getLocationLon();
        dto.storeStatus = store.getStoreStatus().toString();
        dto.address = store.getAddress();
        dto.name = store.getName();
        return dto;
    }

}
