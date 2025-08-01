package com.example.farm4u.repository;

import com.example.farm4u.entity.Farmer;
import com.example.farm4u.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long>{

    @Query("SELECT f FROM Farmer f WHERE f.userId = :userId")
    Optional<Farmer> findByUserId(Long userId);

    Optional<Farmer> findByBusinessNumber(String businessNumber);

    List<Farmer> findByDeletedFalse();

    @Query("SELECT f FROM Farmer f WHERE f.userId=:userId AND f.deleted=false")
    Optional<Farmer> findByUserIdAndDeletedFalse(Long userId);

    @Query("SELECT f FROM Farmer f WHERE f.latitude BETWEEN :latMin AND :latMax AND f.longitude BETWEEN :lngMin AND :lngMax AND f.deleted=false")
    List<Farmer> findByLocation(@Param("latMin") double latMin, @Param("latMax") double latMax,
                                @Param("lngMin") double lngMin, @Param("lngMax") double lngMax);

}
