package GURUKULAM.example.BOOK_STORE;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.target_audience = ?1 ORDER BY n.id DESC")
    List<Notification> findByTarget_audienceOrderByIdDesc(String targetAudience);
}
