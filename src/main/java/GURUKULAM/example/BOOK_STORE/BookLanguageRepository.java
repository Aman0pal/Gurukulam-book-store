package GURUKULAM.example.BOOK_STORE;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookLanguageRepository extends JpaRepository<BookLanguage, Long> {
    BookLanguage findByName(String name);
    List<BookLanguage> findAllByOrderByNameAsc();
}
