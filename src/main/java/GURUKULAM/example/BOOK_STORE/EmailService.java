package GURUKULAM.example.BOOK_STORE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.sender}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public boolean sendOtpEmail(String toEmail, String otp) {
        try {
            String subject = "Gurukulam Book Store - Password Reset OTP";
            String textContent = "Hello,\n\n"
                    + "We received a request to reset your password for the Gurukulam Book Store.\n\n"
                    + "Your One-Time Password (OTP) is: " + otp + "\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Thank you,\nGurukulam Book Store Team";
                    
            return sendEmailViaBrevo(toEmail, subject, textContent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean sendSignupOtpEmail(String toEmail, String otp) {
        try {
            String subject = "Gurukulam Book Store - Account Verification OTP";
            String textContent = "Hello,\n\n"
                    + "Welcome to Gurukulam Book Store! We received a request to create a new account with this email address.\n\n"
                    + "Your One-Time Password (OTP) for account verification is: " + otp + "\n\n"
                    + "If you did not request to create an account, please ignore this email.\n\n"
                    + "Thank you,\nGurukulam Book Store Team";
                    
            return sendEmailViaBrevo(toEmail, subject, textContent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendWelcomeEmail(String toEmail, String name) {
        try {
            String subject = "Welcome to Gurukulam Book Store! 🎉";
            String textContent = "Hi " + name + ",\n\n"
                    + "Thank you for joining Gurukulam Book Store! We are thrilled to have you here.\n\n"
                    + "Here is what you can do on our site:\n"
                    + "- Browse hundreds of books across various categories\n"
                    + "- Add your top picks to your Favorites ❤️\n"
                    + "- Toggle Dark Mode for comfortable late-night reading\n\n"
                    + "Happy reading!\n\n"
                    + "Best,\nGurukulam Book Store Team";
                    
            return sendEmailViaBrevo(toEmail, subject, textContent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean sendEmailViaBrevo(String toEmail, String subject, String textContent) {
        HttpHeaders headers = new HttpHeaders();
        // Brevo API uses the 'api-key' header instead of 'Authorization: Bearer'
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> payload = new HashMap<>();
        
        // Brevo expects 'sender' as an object
        Map<String, String> senderObj = new HashMap<>();
        senderObj.put("name", "Gurukulam Book Store");
        senderObj.put("email", senderEmail);
        payload.put("sender", senderObj);

        // Brevo expects 'to' as an array of objects
        Map<String, String> toObj = new HashMap<>();
        toObj.put("email", toEmail);
        payload.put("to", List.of(toObj));

        payload.put("subject", subject);
        payload.put("textContent", textContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);
            return response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.ACCEPTED;
        } catch (Exception e) {
            System.err.println("Brevo API Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
