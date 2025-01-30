package com.faos.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class Cylinder {

	    @Id
	    @Column(name = "id", nullable = false, unique = true)
	    private String id; // Alphanumeric ID

	    @Column(name = "weight", nullable = false)
	    private float weight;

	    @Column(name = "cylinder_type", nullable = false)
	    private String type;//Full or Empty

	    @Column(name = "status", nullable = false)
	    private String status;//Available or Delivered

	    @Column(name = "created_date", nullable = false)
	    private LocalDateTime createdDate;//Supplier's delivered date

	    @Column(name = "refill_date", nullable = false, columnDefinition = "DATETIME DEFAULT NOW()")
	    private LocalDateTime refillDate;//used when the cylinder is empty
	    
	    @ManyToOne
	    @JoinColumn(name="supplierID")
	    @JsonIgnoreProperties("cylinderList")
	    private Supplier suppObj;

	    // Default constructor with initialization for id
	    public Cylinder() {
	        this.id = generateAlphanumericId(); // Generate alphanumeric ID
	    }

	    // Constructor for easier initialization (without id)
	    public Cylinder(float weight,String type, String status, LocalDateTime refillDate,LocalDateTime createddate) {
	        this.id = generateAlphanumericId(); // Generate alphanumeric ID
	        this.weight=weight;
	        this.type = type;
	        this.status = status;
	        this.refillDate = refillDate;
	        this.createdDate = createddate;
	    }

	    // Method to generate a unique alphanumeric ID
	    private String generateAlphanumericId() {
	        return "CYL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // CYL- followed by 8 random characters
	    }

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public float getWeight() {
			return weight;
		}

		public void setWeight(float weight) {
			this.weight = weight;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public LocalDateTime getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(LocalDateTime createdDate) {
			this.createdDate = createdDate;
		}

		public LocalDateTime getRefillDate() {
			return refillDate;
		}

		public void setRefillDate(LocalDateTime refillDate) {
			this.refillDate = refillDate;
		}

	    
	}


