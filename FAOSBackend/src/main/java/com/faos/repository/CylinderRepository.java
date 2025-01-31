package com.faos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.faos.model.Cylinder;

public interface CylinderRepository extends JpaRepository<Cylinder, String> {

    @Query(value = "SELECT cylinderid FROM cylinders WHERE   status = 'Available' LIMIT 1", nativeQuery = true)
    Optional<String> findByConType(String type);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cylinders SET status = 'delivered' , type='empty' WHERE cylinderid = :cylinderid", nativeQuery = true)
    void updateCylinderStatus(String cylinderid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cylinders SET bookingId = :bookingId WHERE cylinderid = :cylinderid", nativeQuery = true)
    int updateCylinderBookingId(@Param("cylinderid") String cylinderid, @Param("bookingId") Long bookingId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cylinders SET status = 'Available', bookingId = null, type='full' WHERE bookingId = :bookingIds", nativeQuery = true)
    void updateCylinder(long bookingIds);

}
