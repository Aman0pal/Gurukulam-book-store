package GURUKULAM.example.BOOK_STORE;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    BookCategory findByName(String name);
    List<BookCategory> findAllByOrderByNameAsc();
}
