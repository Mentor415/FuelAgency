package com.faos.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.faos.exception.CustomException;
import com.faos.exception.CustomExceptions;
import com.faos.model.Bill;
import com.faos.model.Booking;
import com.faos.model.BookingResponse;
import com.faos.model.Customer;
import com.faos.service.BillService;
import com.faos.service.BookingService;
import com.faos.service.CustomerService;
import com.faos.service.CylinderService;


@Controller
public class CustomerBookingController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CylinderService cylinderService;
    
    @Autowired
    private BillService billService;
    

    @PostMapping("/logins")
    public ResponseEntity<Customer> customerLogin(@RequestBody Customer customer) {
        try {
            Optional<Customer> existingCustomer = customerService.getCustomerByIds(customer.getConsumerId());
            System.out.println(customer.getConsumerId());
            if (existingCustomer.isPresent()) {
                return new ResponseEntity<>(existingCustomer.get(), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            // Return custom error message
            throw new CustomException("An error occurred while logging in.", e);
        }
    }

    @PostMapping("/addbooking")
    public ResponseEntity<?> addBooking(@RequestParam String cylinderid, @RequestParam String consumerId, @RequestBody Booking booking ) {
        try {
            boolean canBook = bookingService.canCustomerBook(consumerId);
            if (!canBook) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You cannot book a cylinder. You must wait at least 30 days since your last booking.");
            }
            if (booking.getDeliveryOption() == null || booking.getDeliveryOption().isEmpty()) {
                return ResponseEntity.badRequest().body("Delivery option is required.");
            }

            // Create a bill based on delivery option
            long count = bookingService.countBookingsInLast12Months(consumerId);
            boolean surchargeApplicable = count >= 6;

            // ✅ Create Bill with surcharge check
            Bill bill = billService.createBill(booking.getDeliveryOption(), surchargeApplicable);
            booking.setBill(bill); // Associate the bill with the booking

            // Update previous booking if needed
            Optional<Long> previousBookingId = Optional.ofNullable(bookingService.getPreviousBookingId(consumerId));

            if (previousBookingId.isPresent() && previousBookingId.get() != 0) {
                long Id = previousBookingId.get();
                cylinderService.updateCylinder(Id); // Updating the previous cylinder
            }

            // Save the current booking
            Booking savedBooking = bookingService.saveBooking(booking, consumerId, cylinderid);

            // Set the booking ID on the cylinder
            cylinderService.setBookingId(cylinderid, savedBooking.getBookingId());

            String message = surchargeApplicable
                    ? "You have exceeded six cylinders a year limit. A 20% charge is added."
                    : "Booking confirmed successfully.";

            return ResponseEntity.status(HttpStatus.CREATED).body(new BookingResponse(savedBooking, message, surchargeApplicable));
        } catch (Exception | CustomExceptions e) {
            throw new CustomException("An error occurred while processing the booking.", e);
        }
    }

    // Check if customer can book based on their last booking date
    @GetMapping("/checkBooking")
    public ResponseEntity<Boolean> checkBooking(@RequestParam String consumerId) {
        try {
            System.out.println("check_______________________________________");
            boolean canBook = bookingService.canCustomerBook(consumerId);
            return ResponseEntity.ok(canBook);
        } catch (Exception e) {
            throw new CustomException("An error occurred while checking booking eligibility.", e);
        }
    }
    @GetMapping("/sixeValidation")
    public ResponseEntity<Boolean> sixValidation(@RequestParam String consumerId) {
        try {
            System.out.println("Checking booking eligibility...");
            boolean canBook = bookingService.canCustomerBooks(consumerId);
            return ResponseEntity.ok(canBook);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }


    @GetMapping("/recentBooking")
    public ResponseEntity<Booking> getLastBooking() {
        try {
            Booking lastBooking = bookingService.getLastBooking();
            if (lastBooking == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(lastBooking);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving the last booking.", e);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Booking>> history(@RequestParam String consumerId) {
        try {
            List<Booking> allBookings = bookingService.findAllBookings(consumerId);
            return ResponseEntity.ok(allBookings);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving the booking history.", e);
        }
    }

    @GetMapping("/bill")
    public ResponseEntity<List<Booking>> bill(@RequestParam String bookingId) {
        try {
            List<Booking> allBookings = bookingService.findByBookingId(bookingId);
            System.out.println(allBookings);
            return ResponseEntity.ok(allBookings);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving the booking history.", e);
        }
    }


    @GetMapping("/customer")
    public ResponseEntity<List<Customer>> consumer(@RequestParam String consumerId) {
        try {
            List<Customer> allBookings = customerService.findByConsumerId(consumerId);
            System.out.println(allBookings);
            return ResponseEntity.ok(allBookings);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving the booking history.", e);
        }
    }

    @GetMapping("/getCylinderId")
    public ResponseEntity<String> getCylinderId(@RequestParam String type) {
        try {
            String cylinderId = cylinderService.getCylinderId("Available");
            System.out.println(cylinderId);
            if (cylinderId == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(cylinderId);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving the cylinder ID.", e);
        }


    }
    @PostMapping("/updateCylinder")
    public ResponseEntity<String> updateStatus(@RequestParam String cylinderid) {
        try {
            System.out.println(cylinderid);
            // Validate the input
            if (cylinderid == null || cylinderid.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Cylinder ID is required.");
            }

            // Update the status
            cylinderService.updateCylinderStatus(cylinderid);

            // Return success response
            return ResponseEntity.ok("Cylinder status updated successfully.");
        } catch (Exception e) {
            // Log the error and return failure response
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update cylinder status: " + e.getMessage());
        }
    }

}
