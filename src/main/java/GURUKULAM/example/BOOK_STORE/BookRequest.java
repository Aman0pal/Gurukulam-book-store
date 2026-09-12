package GURUKULAM.example.BOOK_STORE;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class BookRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String book_name;
    private String book_author;
    private String book_category;
    
    @Column(name = "book_sub_category")
    private String book_sub_category;
    
    private String book_language;
    private String pdf_file_path;
    private String requested_by;
    private String status;

    public BookRequest() {
    }

    public BookRequest(String book_name, String book_author, String book_category, String book_sub_category, String book_language, String pdf_file_path, String requested_by, String status) {
        this.book_name = book_name;
        this.book_author = book_author;
        this.book_category = book_category;
        this.book_sub_category = book_sub_category;
        this.book_language = book_language;
        this.pdf_file_path = pdf_file_path;
        this.requested_by = requested_by;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getBook_author() {
        return book_author;
    }

    public void setBook_author(String book_author) {
        this.book_author = book_author;
    }

    public String getBook_category() {
        return book_category;
    }

    public void setBook_category(String book_category) {
        this.book_category = book_category;
    }

    public String getBook_sub_category() {
        return book_sub_category;
    }

    public void setBook_sub_category(String book_sub_category) {
        this.book_sub_category = book_sub_category;
    }

    public String getBook_language() {
        return book_language;
    }

    public void setBook_language(String book_language) {
        this.book_language = book_language;
    }

    public String getPdf_file_path() {
        return pdf_file_path;
    }

    public void setPdf_file_path(String pdf_file_path) {
        this.pdf_file_path = pdf_file_path;
    }

    public String getRequested_by() {
        return requested_by;
    }

    public void setRequested_by(String requested_by) {
        this.requested_by = requested_by;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
