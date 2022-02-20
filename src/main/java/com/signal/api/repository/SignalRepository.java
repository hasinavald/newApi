package com.signal.api.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.signal.api.model.Signal;

public interface SignalRepository extends JpaRepository<Signal, Long>{
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE signals SET status = :status WHERE id = :id", nativeQuery = true)
	int updateStatusSignal(String status,Long id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE signals SET region = :region WHERE id = :id", nativeQuery = true)
	int updateRegionSignal(String region,Long id);

	@Modifying
	@Transactional
	@Query(value = "SELECT * FROM signals WHERE username = :username AND seen = 0 AND status = 'Terminé'", nativeQuery = true)
	List<Signal> findBySeenAndClient(String username);

	@Modifying
	@Transactional
	@Query(value = "UPDATE signals SET seen = :seen WHERE id = :id", nativeQuery = true)
	int updateSeenByClient(Long id, int seen);


	@Modifying
	@Transactional
	@Query(value = "UPDATE signals SET seen = :seen WHERE id = :id", nativeQuery = true)
	int updateSeenByClientno(Long id, int seen);

	Optional<Signal> findById(Long id);

	List<Signal> findBySeen(int seen);
	
	List<Signal> findByRegion(String region);
	
	List<Signal> findByUsername(String username);
	
}
