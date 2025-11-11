package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.application.entities.Auditors;
import com.application.entities.Auditors.AvailabilityStatus;

@Repository
public interface AuditorRepository extends JpaRepository<Auditors, Integer> {
	
	List<Auditors> findByAvailabilityStatus(AvailabilityStatus status);

	Optional<Auditors> findByName(String assignedAuditorName);

}
