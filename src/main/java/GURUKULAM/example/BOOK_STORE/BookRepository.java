package GURUKULAM.example.BOOK_STORE;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

//JPA java persistent API
public interface BookRepository extends JpaRepository<Books, Long> {

    @Query("SELECT b FROM Books b WHERE b.book_language = ?1")
    List<Books> getBooksByLanguage(String language);

    @Query("SELECT DISTINCT b.book_language FROM Books b WHERE b.book_language IS NOT NULL")
    List<String> findDistinctLanguages();

    @Query("SELECT b FROM Books b WHERE b.book_category = ?1")
    List<Books> getBooksByCategory(String category);

    @Query("SELECT DISTINCT b.book_category FROM Books b WHERE b.book_category IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT b FROM Books b WHERE b.book_sub_category = ?1")
    List<Books> getBooksBySubCategory(String subCategory);

    @Query("SELECT DISTINCT b.book_sub_category FROM Books b WHERE b.book_sub_category IS NOT NULL")
    List<String> findDistinctSubCategories();

    // Combined filters
    @Query("SELECT b FROM Books b WHERE b.book_category = ?1 AND b.book_language = ?2")
    List<Books> getBooksByCategoryAndLanguage(String category, String language);

    @Query("SELECT b FROM Books b WHERE b.book_sub_category = ?1 AND b.book_language = ?2")
    List<Books> getBooksBySubCategoryAndLanguage(String subCategory, String language);

    // Search by book name (case-insensitive partial match), sorted by exact match, prefix match, then partial match
    @Query("SELECT b FROM Books b WHERE LOWER(b.book_name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
           "ORDER BY " +
           "CASE WHEN LOWER(b.book_name) = LOWER(?1) THEN 1 " +
           "WHEN LOWER(b.book_name) LIKE LOWER(CONCAT(?1, '%')) THEN 2 " +
           "ELSE 3 END, b.book_name ASC")
    List<Books> searchBooksByName(String name);

    @Query("SELECT b FROM Books b WHERE LOWER(b.book_name) = LOWER(?1)")
    List<Books> getBooksByExactName(String name);

    @Query("SELECT COUNT(b) FROM Books b WHERE b.book_language = ?1")
    long countByLanguage(String language);

    @Query("SELECT COUNT(b) FROM Books b WHERE b.book_category = ?1")
    long countByCategory(String category);

    @Query("SELECT COUNT(b) FROM Books b WHERE b.book_sub_category = ?1")
    long countBySubCategory(String subCategory);
}
