package com.application.DTO;

import java.util.List;

import lombok.Data;

@Data
public class ProcessAssignmentRequest {

	private List<AuditorInputDto> auditors;
	
	private List<StoreInputDto> stores;
	
	private List<Object> disruptions;
}
