package com.example.farm4u.repository;

import com.example.farm4u.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("UPDATE User u SET u.currentMode = :mode WHERE u.id = :userId")
    void updateMode(@Param("userId") Long userId, @Param("mode") String mode);
    
    /** User Mode 조회용 */
    @Query("SELECT u.currentMode FROM User u WHERE u.id = :userId AND u.deleted = false")
    String findCurrentModeById(@Param("userId") Long userId);

    /**
     * 특정 공고의 소유자(farmers.userId) 조회
     * @param jobId 공고 id
     * @return 해당 공고의 userId(농가의 user_id)
     */
    @Query("SELECT j.userId FROM Job j WHERE j.id = :jobId AND j.deleted = false")
    Long findFarmerUserIdByJobId(@Param("jobId") Long jobId);

}
