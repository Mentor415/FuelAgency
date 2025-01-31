package com.faos.service;

import com.faos.model.Cylinder;
import com.faos.repository.CylinderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CylinderService {

    @Autowired
    private CylinderRepository cylinderRepository;

    // Method to get cylinder by type
    public String getCylinderId(String type) {
        return cylinderRepository.findByConType(type).orElse(null);
    }

    // Method to update cylinder status
    public void updateCylinderStatus(String cylinderid) {
        cylinderRepository.updateCylinderStatus(cylinderid);
    }

    // Method to get cylinder by ID
    public Optional<Cylinder> getCustomerById(String cylinderid) {
        return cylinderRepository.findById(cylinderid);
    }

    // Method to set booking ID on a cylinder, with proper transaction handling
    @Transactional
    public void setBookingId(String cylinderid, Long bookingId) {
        int updatedRows = cylinderRepository.updateCylinderBookingId(cylinderid, bookingId);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update cylinder bookingId");
        }
    }

    // Method to update cylinder status back to 'Available'
    public void updateCylinder(long bookingId) {
        cylinderRepository.updateCylinder(bookingId);
    }
}
