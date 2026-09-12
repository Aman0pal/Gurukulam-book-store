package GURUKULAM.example.BOOK_STORE;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookSubCategoryRepository extends JpaRepository<BookSubCategory, Long> {
    List<BookSubCategory> findByParentCategoryName(String parentCategoryName);
    List<BookSubCategory> findAllByOrderByParentCategoryNameAscNameAsc();
}
