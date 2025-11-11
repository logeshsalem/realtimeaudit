package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.application.entities.Auditors;
import com.application.entities.Store;
import com.application.entities.Store.StoreStatus;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {
	List<Store> findByStoreStatus(StoreStatus storeStatus);
	Optional<Store> findByName(String storeName); 
}
