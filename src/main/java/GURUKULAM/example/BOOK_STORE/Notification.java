package GURUKULAM.example.BOOK_STORE;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private String message;
    private String sender_name;
    private String target_audience;
    
    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime created_at;

    public Notification() {
    }

    public Notification(String topic, String message, String sender_name, String target_audience, LocalDateTime created_at) {
        this.topic = topic;
        this.message = message;
        this.sender_name = sender_name;
        this.target_audience = target_audience;
        this.created_at = created_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getTarget_audience() {
        return target_audience;
    }

    public void setTarget_audience(String target_audience) {
        this.target_audience = target_audience;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
