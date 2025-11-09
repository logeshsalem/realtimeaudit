package com.application.exception;

import lombok.Data;

@Data
public class AssignmentSuggestion {

	private int suggestedAuditorid;
	
	private Double suitabilityScore;
	
	private String justification;
}
