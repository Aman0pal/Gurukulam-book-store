package GURUKULAM.example.BOOK_STORE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long book_no;
    private String book_name;
    private String book_author;
    private String book_category;

    @Column(name = "book_sub_category")
    private String book_sub_category;

    private String book_language;

    private String pdf_file_path;

    public void setPdf_file_path(String pdf_file_path){this.pdf_file_path=pdf_file_path;}
    public String getPdf_file_path(){return pdf_file_path;}

    public void setBook_no(Long book_no){this.book_no=book_no;}
    public Long getBook_no(){return book_no;}

    public void setBook_name(String bookName){this.book_name=bookName;}
    public String getBook_name(){return book_name;}

    public void setBook_author(String book_author){this.book_author=book_author;}
    public String getBook_author(){return book_author;}

    public void setBook_category(String book_category){this.book_category=book_category;}
    public String getBook_category(){return book_category;}

    public void setBook_sub_category(String book_sub_category){this.book_sub_category=book_sub_category;}
    public String getBook_sub_category(){return book_sub_category;}

    public void setBook_language(String book_language){this.book_language=book_language;}
    public String getBook_language(){return book_language;}
}
