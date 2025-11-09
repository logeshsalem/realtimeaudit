package com.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.application.entities.Auditors;
import com.application.entities.Auditors.AvailabilityStatus;
import com.application.repository.AuditorRepository;

@Service
public class AuditorServiceImpl implements AuditorService {

	private final AuditorRepository auditorRepository;
	
	@Autowired
	public AuditorServiceImpl(AuditorRepository auditorRepository) {
		this.auditorRepository = auditorRepository;
	}

	@Override
	public Auditors saveAuditors(Auditors auditors) {
		
		if(auditors.getAvailabilityStatus()==null) {
			auditors.setAvailabilityStatus(Auditors.AvailabilityStatus.AVAILABLE);
		}
		
		if(auditors.getName()==null || auditors.getName().trim().isEmpty()) 
			throw new IllegalArgumentException("Auditor name must be provided."); {
			
		}
		
		return auditorRepository.save(auditors);
	}

	@Override
	public List<Auditors> findAllAuditors() {
		return auditorRepository.findAll();
	}

	@Override
	public List<Auditors> findAvailableAuditors() {		
		return auditorRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
	}

	@Override
	public Optional<Auditors> findByAuditorsId(int id) {
		return auditorRepository.findById(id);
	}

	@Override
	public Auditors updateAvailabilityStatus(int id, AvailabilityStatus newStatus) {
		Auditors auditors = auditorRepository.findById(id).orElseThrow(
				()-> new RuntimeException("Auditor with Id "+id+" not found"));
		if(auditors.getAvailabilityStatus()!= newStatus) {
			auditors.setAvailabilityStatus(newStatus);
		}
		return auditorRepository.save(auditors);
	}
}
