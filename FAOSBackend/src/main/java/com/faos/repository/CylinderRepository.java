package com.faos.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.faos.model.Cylinder;
import com.faos.model.Supplier;

public interface CylinderRepository extends JpaRepository<Cylinder, String> {

	// Cylinder-related queries
    List<Cylinder> findByStatus(String status);
    Optional<Cylinder> findByIdAndStatus(String id, String status);
    List<Cylinder> findByType(String type);
    List<Cylinder> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM Cylinder c WHERE c.status = :status")
    long countByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE Cylinder c SET c.status = :status WHERE c.id = :id")
    void updateCylinderStatus(String id, String status);

    List<Cylinder> findByRefillDateAfter(LocalDateTime date);
    List<Cylinder> findByTypeAndStatus(String type, String status);
    List<Cylinder> findByWeight(Double weight);
    List<Cylinder> findByWeightAndStatus(Double weight, String status);

    // Supplier-related queries
    @Query("SELECT c FROM Cylinder c WHERE c.supplier.supplierID = :supplierID")
    List<Cylinder> findBySupplierId(String supplierID);

    List<Cylinder> findBySupplier(Supplier supplier);
	
	
    @Query(value = "SELECT cylinderid FROM cylinders WHERE   status = 'Available' LIMIT 1", nativeQuery = true)
    Optional<String> findByConType(String type);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cylinder SET status = 'delivered' , type='empty' WHERE cylinderid = :cylinderid", nativeQuery = true)
    void updateCylinderStatus(String cylinderid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cylinder SET bookingId = :bookingId WHERE cylinderid = :cylinderid", nativeQuery = true)
    int updateCylinderBookingId(@Param("cylinderid") String cylinderid, @Param("bookingId") Long bookingId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cylinder SET status = 'Available', bookingId = null, type='full' WHERE bookingId = :bookingIds", nativeQuery = true)
    void updateCylinder(long bookingIds);

}
