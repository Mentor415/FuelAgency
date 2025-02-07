package com.faos.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.faos.model.BookingPageView;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controllers {
    private long gst=0;
    private long delivery_charge=0;
    private long price;
    private long surcharge=0L;
    
    @GetMapping("/")
    public String home(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Retrieve the "CUSTOMER" attribute from the session
            String userType = (String) session.getAttribute("userType");
            
            if ("CUSTOMER".equals(userType)) {
                session.invalidate(); // Invalidate session
                return "redirect:/"; // Redirect to home or another page
            }
        }
        return "index";
    }
    
    @GetMapping("/BookingLogin")
    public String login(Model model) {
        model.addAttribute("customer", new BookingPageView());
        model.addAttribute("errors", "");
        return "blogin";
    }

    @PostMapping("/logins")
    public String customerLogin(@ModelAttribute BookingPageView booking, Model model) {
        try {
            ResponseEntity<BookingPageView> response = getRestTemplate().postForEntity(
                    "http://localhost:8080/logins", booking, BookingPageView.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                BookingPageView customer = response.getBody();
                model.addAttribute("bookingPageView", customer);
                model.addAttribute("permit", "");
                model.addAttribute("customer", new BookingPageView());
                return "customer";
            } else {
                BookingPageView customer = response.getBody();
                model.addAttribute("bookingPageView", customer);
                model.addAttribute("customer", new BookingPageView());
                model.addAttribute("errors", "Bad credentials. Please try again.");
                return "blogin";
            }


        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute("customer", new BookingPageView());
            model.addAttribute("errors", "Bad credentials. Please try again.");
            model.addAttribute("error", "Bad credentials. Please try again.");
            return "blogin";
        }
    }

    @PostMapping("/booking")
    public String booking(@ModelAttribute BookingPageView booking, Model model) {
        try {
            ResponseEntity<BookingPageView> response = getRestTemplate().postForEntity(
                    "http://localhost:8080/logins", booking, BookingPageView.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                BookingPageView customer = response.getBody();
                model.addAttribute("bookingPageView", customer);
                model.addAttribute("permit", "");
                model.addAttribute("customer", new BookingPageView());
                return "booking";
            } else {
                BookingPageView customer = response.getBody();
                model.addAttribute("bookingPageView", customer);
                model.addAttribute("customer", new BookingPageView());
                model.addAttribute("errors", "Bad credentials. Please try again.");
                return "customerDashboard";
            }


        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute("customer", new BookingPageView());
            model.addAttribute("errors", "Bad credentials. Please try again.");
            model.addAttribute("error", "Bad credentials. Please try again.");
            return "customerDashboard";
        }
    }

    @PostMapping("/submit-booking")
    public String addCustomer(@ModelAttribute BookingPageView booking, Model model) {
        try {
            // Set delivery details
            if ("Normal".equals(booking.getDeliveryOption())) {
                booking.setDeliveryDate(LocalDate.now().plusDays(3));
                gst = 10;
                delivery_charge = 50;
                price = (1000 + gst + delivery_charge);
            } else if ("Express".equals(booking.getDeliveryOption())) {
                booking.setDeliveryDate(LocalDate.now().plusDays(1));
                gst = 10;
                delivery_charge = 100;
                price = (1000 + gst + delivery_charge);
            }
            price = 1000 + gst + delivery_charge;
            surcharge = 0; // Reset surcharge

            // ✅ Check if customer can book a cylinder (30-day restriction)
            Boolean canBook = getRestTemplate().getForEntity(
                    "http://localhost:8080/checkBooking?consumerId=" + booking.getConsumerId(), Boolean.class).getBody();

            if (Boolean.FALSE.equals(canBook)) {
                model.addAttribute("permit", "Sorry, you can't book a cylinder before 30 days.");
                return "booking"; // Stop the booking process
            }

            // ✅ Check if the customer has exceeded 6 bookings per year
            Boolean sixValidation = getRestTemplate().getForEntity(
                    "http://localhost:8080/sixeValidation?consumerId=" + booking.getConsumerId(), Boolean.class).getBody();

//            if (Boolean.TRUE.equals(sixValidation)) {
//            	 surcharge = (long) (price * 0.2);
//                // ⚡ Add surcharge message
//                model.addAttribute("surchargeMessage", "You have exceeded the six-cylinder limit. A 20% surcharge will be added.");
//            }

            // Fetch available cylinder ID
            String cylinderId = getRestTemplate().getForEntity(
                    "http://localhost:8080/getCylinderId?type=" + booking.getConnType(), String.class).getBody();
            System.out.println(cylinderId);
            if (cylinderId == null) {
                model.addAttribute("sorry", "Sorry, Cylinder not available");
                return "booking";
            }

            // ✅ Proceed with booking
            booking.setCylinderid(cylinderId);
            getRestTemplate().postForEntity(
                    "http://localhost:8080/updateCylinder?cylinderid=" + cylinderId, booking, String.class);
            booking.setBookingDate(LocalDate.now());

            // Save booking with customer & cylinder ID
            getRestTemplate().postForEntity(
                    "http://localhost:8080/addbooking?consumerId=" + booking.getConsumerId() + "&cylinderid=" + cylinderId,
                    booking, BookingPageView.class);

            // Fetch recent booking
            ResponseEntity<BookingPageView> recentResponse = getRestTemplate().getForEntity(
                    "http://localhost:8080/recentBooking", BookingPageView.class);

            if (recentResponse.getStatusCode().is2xxSuccessful() && recentResponse.getBody() != null) {
                model.addAttribute("recent", recentResponse.getBody());
            }
            long totalPrice = price + surcharge; // Final price after surcharge

            booking.setSurcharge(surcharge);
            booking.setPrice(totalPrice);

            // Pass data to success page
            model.addAttribute("book", booking);
            model.addAttribute("price", totalPrice);
            model.addAttribute("surcharge", surcharge);

           
            return "bsuccess";  // Redirects to success.html where the popup will be handled
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to submit booking: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/downloadBill")
    public ResponseEntity<byte[]> downloadBill(@ModelAttribute BookingPageView bill) {
        try {
            // Fetch customer details
            ResponseEntity<BookingPageView[]> response = getRestTemplate().getForEntity(
                    "http://localhost:8080/customer?consumerId=" + bill.getConsumerId(), BookingPageView[].class);

            ResponseEntity<BookingPageView[]> billResponse = getRestTemplate().getForEntity(
                    "http://localhost:8080/bill?bookingId=" + bill.getBookingId(), BookingPageView[].class);

            if (!response.getStatusCode().is2xxSuccessful() || !billResponse.getStatusCode().is2xxSuccessful()) {
                throw new Exception("API call failed with non-2xx response.");
            }

            BookingPageView customerDetails = Optional.ofNullable(response.getBody())
                    .filter(body -> body.length > 0)
                    .map(body -> body[0])
                    .orElseThrow(() -> new Exception("No customer details found."));

            BookingPageView billDetails = Optional.ofNullable(billResponse.getBody())
                    .filter(body -> body.length > 0)
                    .map(body -> body[0])
                    .orElseThrow(() -> new Exception("No bill details found."));


            long gst = 10;
            long deliveryCharge = "Express".equalsIgnoreCase(billDetails.getDeliveryOption()) ? 100 : 50;
            long basePrice = 1000;  // Base price before adding other charges
            long surcharge = Optional.ofNullable(billDetails.getSurcharge()).orElse(0L);
            long totalPrice = basePrice + gst + deliveryCharge + surcharge;


            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();


            PdfContentByte canvas = writer.getDirectContentUnder();
//            Font watermarkFont = new Font(Font.FontFamily.HELVETICA, 50, Font.BOLD, new BaseColor(200, 200, 200, 50));
//            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase("Fuel Agency", watermarkFont), 297.5f, 600, 45);
//
//
//            document.add(new Paragraph("Booking Confirmation Bill", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD)));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("Booking ID: " + billDetails.getBookingId()));
            document.add(new Paragraph("Customer Name: " + customerDetails.getConsumerName()));
            document.add(new Paragraph("Email: " + customerDetails.getEmail()));
            document.add(new Paragraph("Phone Number: " + customerDetails.getContactNo()));
            document.add(new Paragraph("Connection Type: " + customerDetails.getConnType()));
            document.add(new Paragraph("Time Slot: " + billDetails.getTimeSlot()));
            document.add(new Paragraph("Delivery Option: " + billDetails.getDeliveryOption()));
            document.add(new Paragraph("Delivery Date: " + billDetails.getDeliveryDate()));
            document.add(new Paragraph("Payment Option: " + billDetails.getPaymentOption()));
            document.add(new Paragraph("--------------------------------------------------"));

            document.add(new Paragraph("Base Price: " + basePrice));
            document.add(new Paragraph("GST: " + gst));
            document.add(new Paragraph("Delivery Charge: " + deliveryCharge));

            // ✅ Display surcharge if applicable
//            if (surcharge > 0) {
//                document.add(new Paragraph("Surcharge (20% extra): " + surcharge));
//            }

            document.add(new Paragraph("Total Price: " + totalPrice));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("Thank you for booking with us!"));
            document.close();

            // ✅ Prepare Response
            byte[] pdfBytes = outputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=bill.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating bill: " + e.getMessage()).getBytes());
        }
    }


   



    @GetMapping("/history")
    public String bookingHistory(@RequestParam String consumerId, Model model) {
        try {
            ResponseEntity<BookingPageView[]> response = getRestTemplate().getForEntity(
                    "http://localhost:8080/history?consumerId=" + consumerId, BookingPageView[].class);
//            ResponseEntity<BookingPageView> recentResponse = getRestTemplate().getForEntity(
//                    "http://localhost:8080/recentBooking", BookingPageView.class);
//
//            if (recentResponse.getStatusCode().is2xxSuccessful() && recentResponse.getBody() != null) {
//                model.addAttribute("recent", recentResponse.getBody());
//            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                model.addAttribute("history", response.getBody());
                model.addAttribute("consumerId" ,consumerId);
            } else {
                model.addAttribute("error", "No history available for this consumer.");
            }

            return "history";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch booking history: " + e.getMessage());
            return "error";
        }
    }

    @Bean
    private RestOperations getRestTemplate() {
        return new RestTemplate();
    }
}
