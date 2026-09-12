package GURUKULAM.example.BOOK_STORE;

import org.springframework.data.jpa.repository.JpaRepository;

//JpaRepository like a pre-built toolbox for talking to your database.
public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmailAndPassword(String email, String password);
    java.util.List<User> findByRole(String role);
    java.util.List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByUsernameAndPassword(String username, String password);
    User findByEmail(String email);
    User findByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u JOIN u.favoriteBooks b WHERE b.book_no = ?1")
    java.util.List<User> findByFavoriteBookId(Long bookId);
}
