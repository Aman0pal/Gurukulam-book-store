package GURUKULAM.example.BOOK_STORE;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import java.util.Set;
import java.util.HashSet;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String full_name;
    private String username;
    private String email;
    private String password;
    private String imageUrl;
    private String role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_favorites",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "book_no")
    )
    private Set<Books> favoriteBooks = new HashSet<>();

    public boolean isFavorite(Long bookNo) {
        if (favoriteBooks == null) return false;
        for (Books book : favoriteBooks) {
            if (book.getBook_no().equals(bookNo)) {
                return true;
            }
        }
        return false;
    }

    public Set<Books> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(Set<Books> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }

    public void addFavoriteBook(Books book) {
        this.favoriteBooks.add(book);
    }

    public void removeFavoriteBook(Books book) {
        this.favoriteBooks.remove(book);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    //setter
    public void setId(Long id){this.id=id;}
    //getter
    public Long getId(){return id;}

    public void setFull_name(String full_name){this.full_name=full_name;}
    public String getFull_name(){return full_name;}

    public void setUsername(String username){this.username=username;}
    public String getUsername(){return username;}

    public void setEmail(String email){this.email=email;}
    public String getEmail(){return email;}

    public void setPassword(String password){this.password=password;}
    public String getPassword(){return password;}

}
