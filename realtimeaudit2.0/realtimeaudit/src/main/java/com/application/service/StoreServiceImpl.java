package com.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.application.entities.Store;
import com.application.entities.Store.StoreStatus;
import com.application.repository.StoreRepository;

@Service
public class StoreServiceImpl implements StoreService {
	
	private final StoreRepository storeRepository;
	
	@Autowired
	public StoreServiceImpl(StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}
	
	@Override
	public Store saveStore(Store store) {
		return storeRepository.save(store);
	}

	@Override
	public List<Store> findAllStores() {
		return storeRepository.findAll();
	}

	@Override
	public List<Store> findByStoreStatus() {
		return storeRepository.findByStoreStatus(StoreStatus.OPEN);
	}

	@Override
	public Store updateStoreStatus(int id, StoreStatus newStatus) {
		Store store = storeRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Store with Id "+id +" not found"));
		if(store.getStoreStatus() != newStatus) {
			store.setStoreStatus(newStatus);
		}
		
		return storeRepository.save(store);
	}

	@Override
	public Optional<Store> findStoreById(int targetStoreId) {
		return storeRepository.findById(targetStoreId);
	}
	
	

}
