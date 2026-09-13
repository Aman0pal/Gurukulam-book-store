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

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.api.sender}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String RESEND_URL = "https://api.resend.com/emails";

    public boolean sendOtpEmail(String toEmail, String otp) {
        try {
            String subject = "Gurukulam Book Store - Password Reset OTP";
            String text = "Hello,\n\n"
                    + "We received a request to reset your password for the Gurukulam Book Store.\n\n"
                    + "Your One-Time Password (OTP) is: " + otp + "\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Thank you,\nGurukulam Book Store Team";
                    
            return sendEmailViaResend(toEmail, subject, text);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendWelcomeEmail(String toEmail, String name) {
        try {
            String subject = "Welcome to Gurukulam Book Store! 🎉";
            String text = "Hi " + name + ",\n\n"
                    + "Thank you for joining Gurukulam Book Store! We are thrilled to have you here.\n\n"
                    + "Here is what you can do on our site:\n"
                    + "- Browse hundreds of books across various categories\n"
                    + "- Add your top picks to your Favorites ❤️\n"
                    + "- Toggle Dark Mode for comfortable late-night reading\n\n"
                    + "Happy reading!\n\n"
                    + "Best,\nGurukulam Book Store Team";
                    
            return sendEmailViaResend(toEmail, subject, text);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean sendEmailViaResend(String toEmail, String subject, String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", "Gurukulam <" + senderEmail + ">");
        // Resend API expects 'to' to be an array of strings
        payload.put("to", List.of(toEmail));
        payload.put("subject", subject);
        payload.put("text", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_URL, request, String.class);
            return response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED;
        } catch (Exception e) {
            System.err.println("Resend API Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
