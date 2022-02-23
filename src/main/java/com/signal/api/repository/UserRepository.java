package com.signal.api.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.signal.api.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByUsername(String username);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);
	
	Boolean existsByRegion(String region);

	@Modifying
	@Transactional
	@Query(value = "UPDATE users SET password = :password WHERE id = :id", nativeQuery = true)
	int updatePassword(String password,Long id);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO roles (id,name) VALUES (3,'ROLE_USER')", nativeQuery = true)
	int createSeeder();

}
