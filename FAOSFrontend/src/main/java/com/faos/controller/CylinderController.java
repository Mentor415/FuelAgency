package com.faos.controller;

import com.faos.model.Cylinder;
import com.faos.model.Supplier;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
public class CylinderController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String backendUrl = "http://localhost:8080/api/cylinders";
    
    @GetMapping("/cylinder/index")
    public String home() {
    	return "cylinder-home";
    }

    //view all cylinders
    @GetMapping("/cylinders")
    public String getAllCylinders(Model model) {
        ResponseEntity<Cylinder[]> response = restTemplate.getForEntity(backendUrl, Cylinder[].class);
        List<Cylinder> cylinders = Arrays.asList(response.getBody());
        model.addAttribute("cylinders", cylinders);
        return "cylinder-list";
    }
    
    @GetMapping("/add-cylinder")
    public String showAddCylinderForm(Model model) {
        ResponseEntity<Supplier[]> response = restTemplate.getForEntity("http://localhost:8080/suppliers", Supplier[].class);
        List<Supplier> suppliers = Arrays.asList(response.getBody());
        
        model.addAttribute("cylinder", new Cylinder());
        model.addAttribute("suppliers", suppliers);
        return "add-cylinder";
    }

    // add cylinder
    @PostMapping("/save-cylinder")
    public String saveCylinder(@ModelAttribute Cylinder cylinder, @RequestParam String supplierId, Model model) {
        restTemplate.postForEntity(backendUrl + "/" + supplierId, cylinder, Cylinder.class);
        //return "redirect:/cylinders";
        model.addAttribute("message","Cylinder added successfully!");
        return "cylinder-success";
    }
    
    @GetMapping("/update-cylinder")
    public String updateCylinderForm(Model model) {
        ResponseEntity<Supplier[]> response = restTemplate.getForEntity("http://localhost:8080/suppliers", Supplier[].class);
        List<Supplier> suppliers = Arrays.asList(response.getBody());

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("cylinder", new Cylinder()); // Empty object for form
        return "update-cylinder";
    }
    
    //update cylinder at backend
    @PostMapping("/update-cylinder/{id}")
    public String updateCylinder(@PathVariable String id, @ModelAttribute Cylinder cylinder, Model model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Cylinder> requestEntity = new HttpEntity<>(cylinder, headers);
        ResponseEntity<Cylinder> response = restTemplate.exchange(
            "http://localhost:8080/api/cylinders/" + id, 
            HttpMethod.PUT, 
            requestEntity, 
            Cylinder.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("message", "Cylinder updated successfully!");
        } else {
            model.addAttribute("message", "Failed to update cylinder!");
        }

        return "cylinder-success";
    }
    
    @GetMapping("/get-cylinder-details/{id}")
    @ResponseBody
    public ResponseEntity<?> getCylinderByID(@PathVariable String id) {
        try {
            ResponseEntity<Cylinder> response = restTemplate.getForEntity("http://localhost:8080/api/cylinders/" + id, Cylinder.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cylinder not found");
        }
    }
    
    //delete cylinder form
    @GetMapping("/delete-cylinder")
    public String deleteCylinderForm(Model model) {
    	return "deletecylinder";
    }
    
    @PostMapping("/deleteCylinder")
    public String deleteCylinder(@RequestParam String cylinderId, Model model) {
    	 restTemplate.delete("http://localhost:8080/api/cylinders/"+cylinderId);
    	 model.addAttribute("message","CylinderID  "+ cylinderId + " deleted successfully!");
    	 return "cylinder-success";
    }
    
    // Fetch all empty and available cylinders
    @GetMapping("/refill-cylinder")
    public String showEmptyAvailableCylinders(Model model) {
        ResponseEntity<Cylinder[]> response = restTemplate.getForEntity("http://localhost:8080/api/cylinders/empty/available",
        		Cylinder[].class);
        
        List<Cylinder> cylinders = List.of(response.getBody());
        model.addAttribute("cylinders", cylinders);
        return "refill-cylinder-list";  // Render "cylinders.html"
    }
    
    // Refill Cylinder
    @PostMapping("/cylinders/refill/{id}")
    public String refillCylinder(@PathVariable String id) {
        String apiUrl = "http://localhost:8080/api/cylinders/refill/" + id;
        restTemplate.postForEntity(apiUrl, null, Void.class);
        return "redirect:/cylinders";  // Refresh list after refill
    }
    
    

}


