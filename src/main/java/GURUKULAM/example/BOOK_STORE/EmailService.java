package GURUKULAM.example.BOOK_STORE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Gurukulam Book Store - Password Reset OTP");
            
            String text = "Hello,\n\n"
                    + "We received a request to reset your password for the Gurukulam Book Store.\n\n"
                    + "Your One-Time Password (OTP) is: " + otp + "\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Thank you,\nGurukulam Book Store Team";
                    
            message.setText(text);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com"); // Replaced dynamically by application.properties if configured
            message.setTo(toEmail);
            message.setSubject("Welcome to Gurukulam Book Store! 🎉");
            
            String text = "Hi " + name + ",\n\n"
                    + "Thank you for joining Gurukulam Book Store! We are thrilled to have you here.\n\n"
                    + "Here is what you can do on our site:\n"
                    + "- Browse hundreds of books across various categories\n"
                    + "- Add your top picks to your Favorites ❤️\n"
                    + "- Toggle Dark Mode for comfortable late-night reading\n\n"
                    + "Happy reading!\n\n"
                    + "Best,\nGurukulam Book Store Team";
                    
            message.setText(text);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
