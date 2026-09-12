package GURUKULAM.example.BOOK_STORE;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {
    List<BookRequest> findByStatusOrderByIdDesc(String status);
    List<BookRequest> findAllByOrderByIdDesc();
    long countByStatus(String status);
}
