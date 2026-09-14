package GURUKULAM.example.BOOK_STORE;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
@Controller
public class Controller_1 {

    private UserRepository userRepository;
    private BookRepository bookRepository;
    private BookCategoryRepository bookCategoryRepository;
    private BookSubCategoryRepository bookSubCategoryRepository;
    private BookLanguageRepository bookLanguageRepository;
    private NotificationRepository notificationRepository;
    private BookRequestRepository bookRequestRepository;

    public Controller_1(UserRepository userRepository,BookRepository bookRepository, BookCategoryRepository bookCategoryRepository, BookSubCategoryRepository bookSubCategoryRepository, BookLanguageRepository bookLanguageRepository, NotificationRepository notificationRepository, BookRequestRepository bookRequestRepository){
        this.userRepository=userRepository;
        this.bookRepository=bookRepository;
        this.bookCategoryRepository = bookCategoryRepository;
        this.bookSubCategoryRepository = bookSubCategoryRepository;
        this.bookLanguageRepository = bookLanguageRepository;
        this.notificationRepository = notificationRepository;
        this.bookRequestRepository = bookRequestRepository;
    }





    //postmapping_code

    @PostMapping("/sign_in_data")
    public String Sign_in_formhandler(@ModelAttribute User user, @RequestParam(value = "confirm_password", required = false) String confirmPassword, HttpSession session, RedirectAttributes ra){
        if (user.getFull_name() == null || !user.getFull_name().matches("[A-Za-z\\s]+")) {
            ra.addFlashAttribute("error", "Name should only contain letters.");
            return "redirect:/sign";
        }
        if (user.getUsername() == null || !user.getUsername().matches("^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d_.]+$")) {
            ra.addFlashAttribute("error", "Username must contain both letters and numbers (may include _ and .).");
            return "redirect:/sign";
        }
        if (user.getPassword() == null || user.getPassword().length() <= 5 || user.getPassword().length() >= 16) {
            ra.addFlashAttribute("error", "Password must be at least 6 and less than 16 characters.");
            return "redirect:/sign";
        }
        if (confirmPassword == null || !user.getPassword().equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/sign";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            ra.addFlashAttribute("error", "Account already exists with this email, use another email.");
            return "redirect:/sign";
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            ra.addFlashAttribute("error", "Username is already taken.");
            return "redirect:/sign";
        }

        // Generate OTP instead of saving
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        
        // Send email
        boolean isSent = emailService.sendOtpEmail(user.getEmail(), String.valueOf(otp));
        
        if (isSent) {
            session.setAttribute("signup_user", user);
            session.setAttribute("signup_otp", String.valueOf(otp));
            return "redirect:/verify_signup_otp";
        } else {
            ra.addFlashAttribute("error", "Failed to send verification email. Please try again or check server config.");
            return "redirect:/sign";
        }
    }

    @GetMapping("/verify_signup_otp")
    public String verifySignupOtpPage(HttpSession session) {
        if (session.getAttribute("signup_user") == null || session.getAttribute("signup_otp") == null) {
            return "redirect:/sign";
        }
        return "verify_signup_otp";
    }

    @PostMapping("/verify_signup_otp_process")
    public String verifySignupOtpProcess(@RequestParam("otp") String otp, HttpSession session, RedirectAttributes ra) {
        String sessionOtp = (String) session.getAttribute("signup_otp");
        User signupUser = (User) session.getAttribute("signup_user");
        
        if (sessionOtp != null && signupUser != null && sessionOtp.equals(otp)) {
            // Save to database now
            userRepository.save(signupUser);
            
            // Clean up session
            session.removeAttribute("signup_otp");
            session.removeAttribute("signup_user");
            
            // Send welcome email
            emailService.sendWelcomeEmail(signupUser.getEmail(), signupUser.getFull_name());
            
            ra.addFlashAttribute("success", "Account created successfully! Welcome email sent. Please log in.");
            return "redirect:/login";
        } else {
            ra.addFlashAttribute("error", "Invalid OTP. Please try again.");
            return "redirect:/verify_signup_otp";
        }
    }

    @GetMapping("/api/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !userRepository.existsByEmail(email));
        return response;
    }

    @GetMapping("/api/check-username")
    @ResponseBody
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.existsByUsername(username);
        response.put("available", !exists);
        if (exists) {
            java.util.List<String> suggestions = new java.util.ArrayList<>();
            while (suggestions.size() < 5) {
                String suggestion = username + new Random().nextInt(10000);
                if (!userRepository.existsByUsername(suggestion) && !suggestions.contains(suggestion)) {
                    suggestions.add(suggestion);
                }
            }
            response.put("suggestions", suggestions);
        }
        return response;
    }

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/book_data")
    public String Add_book_formhandler(@ModelAttribute Books books, @RequestParam("pdfFile") org.springframework.web.multipart.MultipartFile pdfFile, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        String savedFilePath = null;
        if (!pdfFile.isEmpty()) {
            savedFilePath = cloudinaryService.uploadFile(pdfFile);
        }
        
        if (SessionUtil.isAdmin(session)) {
            if (savedFilePath != null) books.setPdf_file_path(savedFilePath);
            bookRepository.save(books);
            redirectAttributes.addFlashAttribute("successMessage", "Book added successfully!");
        } else {
            User user = (User) session.getAttribute("user");
            BookRequest request = new BookRequest(books.getBook_name(), books.getBook_author(), books.getBook_category(), books.getBook_sub_category(), books.getBook_language(), savedFilePath, user.getUsername(), "PENDING");
            bookRequestRepository.save(request);
            
            Notification notification = new Notification("New Book Submission", "User " + user.getUsername() + " requested to add '" + books.getBook_name() + "'.", "System", "ADMIN", java.time.LocalDateTime.now());
            notificationRepository.save(notification);
            
            Notification ownerNotif = new Notification("New Book Submission", "User " + user.getUsername() + " requested to add '" + books.getBook_name() + "'.", "System", "OWNER", java.time.LocalDateTime.now());
            notificationRepository.save(ownerNotif);
            
            redirectAttributes.addFlashAttribute("successMessage", "Book submitted for approval! An admin or owner will review it shortly.");
        }
        return "redirect:/add_book";
    }

    // --- Metadata Management APIs ---

    @GetMapping("/api/metadata/languages")
    @ResponseBody
    public List<BookLanguage> getLanguages() {
        List<BookLanguage> langs = bookLanguageRepository.findAllByOrderByNameAsc();
        for (BookLanguage lang : langs) {
            lang.setBookCount(bookRepository.countByLanguage(lang.getName()));
        }
        return langs;
    }

    @PostMapping("/api/metadata/languages")
    @ResponseBody
    public BookLanguage addLanguage(@RequestParam("name") String name) {
        BookLanguage lang = new BookLanguage(name);
        return bookLanguageRepository.save(lang);
    }

    @PostMapping("/api/metadata/languages/delete")
    @ResponseBody
    public String deleteLanguage(@RequestParam("id") Long id) {
        bookLanguageRepository.deleteById(id);
        return "Deleted";
    }

    @GetMapping("/api/metadata/migrate")
    @ResponseBody
    public String migrateMetadata() {
        String[] defaultLangs = {"English", "Hindi", "Sanskrit", "Bengali", "Marathi", "Gujarati", "Tamil", "Telugu", "Urdu", "Spanish", "French", "German"};
        for (String lang : defaultLangs) {
            if (bookLanguageRepository.findByName(lang) == null) {
                bookLanguageRepository.save(new BookLanguage(lang));
            }
        }
        List<String> dbLangs = bookRepository.findDistinctLanguages();
        for (String lang : dbLangs) {
            if (lang != null && !lang.trim().isEmpty() && bookLanguageRepository.findByName(lang) == null) {
                bookLanguageRepository.save(new BookLanguage(lang));
            }
        }
        
        String[] defaultCats = {"Literature", "Religious", "Children", "Historic", "Stories"};
        for (String cat : defaultCats) {
            if (bookCategoryRepository.findByName(cat) == null) {
                bookCategoryRepository.save(new BookCategory(cat));
            }
        }
        List<String> dbCats = bookRepository.findDistinctCategories();
        for (String cat : dbCats) {
            if (cat != null && !cat.trim().isEmpty() && bookCategoryRepository.findByName(cat) == null) {
                bookCategoryRepository.save(new BookCategory(cat));
            }
        }
        Map<String, String[]> defaultSubCats = new HashMap<>();
        defaultSubCats.put("Literature", new String[]{"Prose", "Poetry", "Drama"});
        defaultSubCats.put("Religious", new String[]{"Jain Dharm", "Hinduism", "Buddhism", "Islamic", "Christian", "Jewish"});
        defaultSubCats.put("Children", new String[]{"Moral Stories", "Picture Books", "Fairy Tales", "Comics", "General"});
        defaultSubCats.put("Historic", new String[]{"Ancient", "Medieval", "Modern", "Freedom Movement", "General"});
        defaultSubCats.put("Stories", new String[]{"Fiction", "Non-Fiction", "Mystery", "Adventure", "General"});
        
        for (Map.Entry<String, String[]> entry : defaultSubCats.entrySet()) {
            for (String sub : entry.getValue()) {
                List<BookSubCategory> existing = bookSubCategoryRepository.findByParentCategoryName(entry.getKey());
                if (existing.stream().noneMatch(s -> s.getName().equals(sub))) {
                    bookSubCategoryRepository.save(new BookSubCategory(entry.getKey(), sub));
                }
            }
        }

        List<Books> allBooks = bookRepository.findAll();
        for (Books book : allBooks) {
            if (book.getBook_category() != null && book.getBook_sub_category() != null && !book.getBook_sub_category().trim().isEmpty()) {
                List<BookSubCategory> existing = bookSubCategoryRepository.findByParentCategoryName(book.getBook_category());
                boolean exists = existing.stream().anyMatch(sub -> sub.getName().equals(book.getBook_sub_category()));
                if (!exists) {
                    bookSubCategoryRepository.save(new BookSubCategory(book.getBook_category(), book.getBook_sub_category()));
                }
            }
        }
        
        return "Migration complete!";
    }

    @GetMapping("/api/metadata/categories")
    @ResponseBody
    public List<BookCategory> getCategories() {
        List<BookCategory> cats = bookCategoryRepository.findAllByOrderByNameAsc();
        for (BookCategory cat : cats) {
            cat.setBookCount(bookRepository.countByCategory(cat.getName()));
        }
        return cats;
    }

    @PostMapping("/api/metadata/categories")
    @ResponseBody
    public BookCategory addCategory(@RequestParam("name") String name) {
        BookCategory cat = new BookCategory(name);
        return bookCategoryRepository.save(cat);
    }

    @PostMapping("/api/metadata/categories/delete")
    @ResponseBody
    public String deleteCategory(@RequestParam("id") Long id) {
        BookCategory category = bookCategoryRepository.findById(id).orElse(null);
        if (category != null) {
            List<BookSubCategory> subCategories = bookSubCategoryRepository.findByParentCategoryName(category.getName());
            bookSubCategoryRepository.deleteAll(subCategories);
            bookCategoryRepository.deleteById(id);
        }
        return "Deleted";
    }

    @GetMapping("/api/metadata/subcategories")
    @ResponseBody
    public List<BookSubCategory> getSubCategories() {
        List<BookSubCategory> subcats = bookSubCategoryRepository.findAllByOrderByParentCategoryNameAscNameAsc();
        for (BookSubCategory subcat : subcats) {
            subcat.setBookCount(bookRepository.countBySubCategory(subcat.getName()));
        }
        return subcats;
    }

    @PostMapping("/api/metadata/subcategories")
    @ResponseBody
    public BookSubCategory addSubCategory(@RequestParam("parentCategoryName") String parentCategoryName, @RequestParam("name") String name) {
        BookSubCategory sub = new BookSubCategory(parentCategoryName, name);
        return bookSubCategoryRepository.save(sub);
    }

    @PostMapping("/api/metadata/subcategories/delete")
    @ResponseBody
    public String deleteSubCategory(@RequestParam("id") Long id) {
        bookSubCategoryRepository.deleteById(id);
        return "Deleted";
    }



    private void populateMetadata(Model model) {
        List<String> languages = bookLanguageRepository.findAllByOrderByNameAsc().stream().map(BookLanguage::getName).toList();
        List<BookCategory> categoriesList = bookCategoryRepository.findAllByOrderByNameAsc();
        List<String> categories = categoriesList.stream().map(BookCategory::getName).toList();
        
        List<BookSubCategory> subCategories = bookSubCategoryRepository.findAllByOrderByParentCategoryNameAscNameAsc();
        java.util.Map<String, java.util.List<String>> subCategoriesMap = new java.util.LinkedHashMap<>();
        for (BookSubCategory sub : subCategories) {
            subCategoriesMap.computeIfAbsent(sub.getParentCategoryName(), k -> new java.util.ArrayList<>()).add(sub.getName());
        }
        
        model.addAttribute("languages", languages);
        model.addAttribute("categories", categories);
        model.addAttribute("subCategoriesMap", subCategoriesMap);
    }

    // getmapping_code

    @GetMapping("/home")
    public String Home_Page(HttpSession session, Model model){
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (!SessionUtil.isAdmin(session)) {
            return "redirect:/user_home";
        }
        List<Books> books = bookRepository.findAll();
        populateMetadata(model);

        model.addAttribute("books", books);
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("selectedSubCategory", "All");
        return "home";
    }

    @GetMapping("/user_home")
    public String UserHome_Page(HttpSession session, Model model){
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        List<Books> books = bookRepository.findAll();
        populateMetadata(model);

        model.addAttribute("books", books);
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("selectedSubCategory", "All");
        return "user_home";
    }

    @GetMapping("/sign")
    public String SignUp(){
        return "sign_in";
    }

    @GetMapping("/enter_as_guest")
    public String enterAsGuest(HttpSession session) {
        User guest = new User();
        guest.setUsername("Guest");
        guest.setFull_name("Guest User");
        guest.setRole("user");
        session.setAttribute("user", guest);
        session.setAttribute("role", "user");
        return "redirect:/user_home";
    }

    @GetMapping("/login")
    public String Log_in(){
        return "log_in";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String credential,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes ra) {

        User user = userRepository.findByEmailAndPassword(credential, password);
        if (user == null) {
            user = userRepository.findByUsernameAndPassword(credential, password);
        }

        if (user != null) {
            // Save in session block at server site
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole());

            // Role-based redirect
            if ("admin".equals(user.getRole()) || "owner".equals(user.getRole())) {
                return "redirect:/home";  // admin Pages
            } else {
                return "redirect:/user_home"; // user Pages
            }
        }

        ra.addFlashAttribute("error", "Invalid Email or Password");
        return "redirect:/login";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        // Remove JSESSIONID cookie
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete immediately
        response.addCookie(cookie);
        return "redirect:/login";
    }

    // default root
    @GetMapping("")
    public String homePage(HttpSession session) {
        //if not login then redirect to login page
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        // if login and admin go to home, else user_home
        if (SessionUtil.isAdmin(session)) {
            return "redirect:/home";
        }
        return "redirect:/user_home";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (SessionUtil.isAdmin(session)) {
            return "redirect:/home";
        }
        return "redirect:/user_home";
    }

    @GetMapping("/frgt_pass")
    public String Forget_password(){
        return "forget_pass";
    }

    @Autowired
    private EmailService emailService;

    @PostMapping("/send_otp")
    public String sendOtp(@RequestParam("credential") String credential, HttpSession session, RedirectAttributes ra) {
        User user = userRepository.findByEmail(credential);
        if (user == null) {
            user = userRepository.findByUsername(credential);
        }
        
        if (user == null) {
            ra.addFlashAttribute("error", "User not found with that email or username.");
            return "redirect:/frgt_pass";
        }
        
        // Generate 6 digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        
        // Send email
        boolean isSent = emailService.sendOtpEmail(user.getEmail(), String.valueOf(otp));
        
        if (isSent) {
            session.setAttribute("reset_otp", String.valueOf(otp));
            session.setAttribute("reset_email", user.getEmail());
            return "redirect:/verify_otp";
        } else {
            ra.addFlashAttribute("error", "Failed to send OTP email. Please try again or check server config.");
            return "redirect:/frgt_pass";
        }
    }

    @GetMapping("/verify_otp")
    public String verifyOtpPage(HttpSession session) {
        if (session.getAttribute("reset_otp") == null) {
            return "redirect:/frgt_pass";
        }
        return "verify_otp";
    }

    @PostMapping("/verify_otp_process")
    public String verifyOtpProcess(@RequestParam("otp") String otp, HttpSession session, RedirectAttributes ra) {
        String sessionOtp = (String) session.getAttribute("reset_otp");
        if (sessionOtp != null && sessionOtp.equals(otp)) {
            session.setAttribute("otp_verified", true);
            return "redirect:/reset_pass";
        } else {
            ra.addFlashAttribute("error", "Invalid OTP. Please try again.");
            return "redirect:/verify_otp";
        }
    }

    @GetMapping("/reset_pass")
    public String resetPassPage(HttpSession session) {
        if (session.getAttribute("otp_verified") == null || !(Boolean) session.getAttribute("otp_verified")) {
            return "redirect:/frgt_pass";
        }
        return "reset_pass";
    }

    @PostMapping("/reset_pass_process")
    public String resetPassProcess(@RequestParam("password") String password, 
                                   @RequestParam("confirm_password") String confirmPassword,
                                   HttpSession session, RedirectAttributes ra) {
                                   
        if (session.getAttribute("otp_verified") == null || !(Boolean) session.getAttribute("otp_verified")) {
            return "redirect:/frgt_pass";
        }
        
        if (password == null || password.length() <= 5 || password.length() >= 16) {
            ra.addFlashAttribute("error", "Password must be at least 6 and less than 16 characters.");
            return "redirect:/reset_pass";
        }
        
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/reset_pass";
        }
        
        String email = (String) session.getAttribute("reset_email");
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(password); // In production, hash this!
            userRepository.save(user);
            
            // Clean up session
            session.removeAttribute("reset_otp");
            session.removeAttribute("reset_email");
            session.removeAttribute("otp_verified");
            
            ra.addFlashAttribute("message", "Password successfully updated. Please log in.");
            return "redirect:/login";
        }
        
        ra.addFlashAttribute("error", "User not found.");
        return "redirect:/frgt_pass";
    }

    @GetMapping("/add_book")
    public String Add_books(HttpSession session, Model model){
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        populateMetadata(model);
        
        Map<String, List<String>> subCategoriesMap = new HashMap<>();
        List<BookSubCategory> subCats = bookSubCategoryRepository.findAllByOrderByParentCategoryNameAscNameAsc();
        for (BookSubCategory sc : subCats) {
            subCategoriesMap.computeIfAbsent(sc.getParentCategoryName(), k -> new java.util.ArrayList<>()).add(sc.getName());
        }
        
        
        model.addAttribute("subCategoriesMap", subCategoriesMap);
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        return "add_books";
    }

    @GetMapping("/dbBook")
    public String Show_bookDB(HttpSession session, Model model){
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        List<Books> books= bookRepository.findAll();
        populateMetadata(model);
        model.addAttribute("books",books);
        
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("totalBooks", bookRepository.count());
        return "db_book";
    }

    @GetMapping("/filter-by-language")
    public String filterByLanguage(@RequestParam(required = false, defaultValue = "All") String language,
                                   HttpSession session,
                                   Model model) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Books> books;
        populateMetadata(model);

        if (language == null || language.equals("All")) {
            books = bookRepository.findAll();
        } else {
            books = bookRepository.getBooksByLanguage(language);
        }

        model.addAttribute("books", books);
        model.addAttribute("selectedLanguage", language);
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("selectedSubCategory", "All");
        

        if (SessionUtil.isAdmin(session)) {
            return "home";
        }
        return "user_home";
    }

    @GetMapping("/filter-by-category")
    public String filterByCategory(@RequestParam(required = false, defaultValue = "All") String category,
                                   HttpSession session,
                                   Model model) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Books> books;
        populateMetadata(model);

        if (category == null || category.equals("All")) {
            books = bookRepository.findAll();
        } else {
            books = bookRepository.getBooksByCategory(category);
        }

        model.addAttribute("books", books);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSubCategory", "All");
        model.addAttribute("selectedLanguage", "All");

        if (SessionUtil.isAdmin(session)) {
            return "home";
        }
        return "user_home";
    }

    @GetMapping("/filter-by-subcategory")
    public String filterBySubCategory(@RequestParam(required = false, defaultValue = "All") String subCategory,
                                      HttpSession session,
                                      Model model) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Books> books;
        populateMetadata(model);

        if (subCategory == null || subCategory.equals("All")) {
            books = bookRepository.findAll();
        } else {
            books = bookRepository.getBooksBySubCategory(subCategory);
        }

        model.addAttribute("books", books);
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("selectedSubCategory", subCategory);
        model.addAttribute("selectedLanguage", "All");

        if (SessionUtil.isAdmin(session)) {
            return "home";
        }
        return "user_home";
    }

    // Combined filter: category/subcategory + language work together
    @GetMapping("/filter")
    public String combinedFilter(
            @RequestParam(required = false, defaultValue = "All") String category,
            @RequestParam(required = false, defaultValue = "All") String subCategory,
            @RequestParam(required = false, defaultValue = "All") String language,
            HttpSession session,
            Model model) {

        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Books> books;
        populateMetadata(model);

        boolean hasCat = !category.equals("All");
        boolean hasSub = !subCategory.equals("All");
        boolean hasLang = !language.equals("All");

        if (hasSub && hasLang) {
            books = bookRepository.getBooksBySubCategoryAndLanguage(subCategory, language);
        } else if (hasCat && hasLang) {
            books = bookRepository.getBooksByCategoryAndLanguage(category, language);
        } else if (hasSub) {
            books = bookRepository.getBooksBySubCategory(subCategory);
        } else if (hasCat) {
            books = bookRepository.getBooksByCategory(category);
        } else if (hasLang) {
            books = bookRepository.getBooksByLanguage(language);
        } else {
            books = bookRepository.findAll();
        }

        model.addAttribute("books", books);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSubCategory", subCategory);
        model.addAttribute("selectedLanguage", language);

        if (SessionUtil.isAdmin(session)) {
            return "home";
        }
        return "user_home";
    }

    @GetMapping("/dbUser")
    public String DbUser(HttpSession session, Model model){
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        List<User> users=userRepository.findAll();
        populateMetadata(model);
        model.addAttribute("users",users);
        
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        
        User owner = userRepository.findByRole("owner").stream().findFirst().orElse(null);
        model.addAttribute("owner", owner);
        model.addAttribute("selectedUserRole", "All");
        
        long totalAdmins = userRepository.findByRole("admin").size();
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalAdmins", totalAdmins);
        
        return "db_user";
    }

    @GetMapping("/api/search-users")
    @ResponseBody
    public List<String> searchUsersApi(@RequestParam("query") String query, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return java.util.Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<User> matches = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query.trim(), query.trim());
        return matches.stream()
                      .map(u -> u.getUsername() + " (" + u.getEmail() + ")")
                      .distinct()
                      .limit(5)
                      .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/filter-user-role")
    public String filterUserRole(@RequestParam(required = false, defaultValue = "All") String userRole,
                                 HttpSession session,
                                 Model model) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users;
        if (userRole == null || userRole.equals("All")) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByRole(userRole.toLowerCase());
        }

        populateMetadata(model);
        
        User owner = userRepository.findByRole("owner").stream().findFirst().orElse(null);
        
        model.addAttribute("users", users);
        
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("owner", owner);
        model.addAttribute("selectedUserRole", userRole);

        long totalAdmins = userRepository.findByRole("admin").size();
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalAdmins", totalAdmins);

        return "db_user";
    }

    @GetMapping("/search-db-user")
    public String searchDbUser(@RequestParam("query") String query,
                               HttpSession session,
                               Model model) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users;
        if (query == null || query.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query.trim(), query.trim());
        }

        populateMetadata(model);
        
        User owner = userRepository.findByRole("owner").stream().findFirst().orElse(null);
        
        model.addAttribute("users", users);
        
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("owner", owner);
        model.addAttribute("selectedUserRole", "All");

        long totalAdmins = userRepository.findByRole("admin").size();
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalAdmins", totalAdmins);

        return "db_user";
    }

    @PostMapping("/delete_book")
    public String deleteBook(@RequestParam Long id, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        
        Books book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            java.util.List<User> usersWithFavorite = userRepository.findByFavoriteBookId(id);
            for (User user : usersWithFavorite) {
                user.removeFavoriteBook(book);
                userRepository.save(user);
            }
            bookRepository.deleteById(id);
        }
        
        return "redirect:/dbBook";
    }

    @PostMapping("/delete_user")
    public String deleteUser(@RequestParam Long id, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        userRepository.deleteById(id);
        return "redirect:/dbUser";
    }

    @PostMapping("/make_admin")
    public String makeAdmin(@RequestParam Long id, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isOwner(session)) {
            return "redirect:/login";
        }
        User user = userRepository.findById(id).orElse(null);
        if (user != null && "user".equals(user.getRole())) {
            user.setRole("admin");
            userRepository.save(user);
        }
        return "redirect:/dbUser";
    }

    @PostMapping("/remove_admin")
    public String removeAdmin(@RequestParam Long id, HttpSession session) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isOwner(session)) {
            return "redirect:/login";
        }
        User user = userRepository.findById(id).orElse(null);
        if (user != null && "admin".equals(user.getRole())) {
            user.setRole("user");
            userRepository.save(user);
        }
        return "redirect:/dbUser";
    }

    @GetMapping("/api/search-books")
    @ResponseBody
    public List<String> searchBooksApi(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Books> matches = bookRepository.searchBooksByName(query);
        return matches.stream()
                      .map(Books::getBook_name)
                      .distinct()
                      .limit(5)
                      .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam("query") String query, 
                              @RequestParam(value = "exact", required = false, defaultValue = "false") boolean exact,
                              HttpSession session, Model model) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        List<Books> books;
        if (query == null || query.trim().isEmpty()) {
            books = bookRepository.findAll();
        } else if (exact) {
            books = bookRepository.getBooksByExactName(query.trim());
            // Fallback just in case exact match wasn't found
            if (books.isEmpty()) {
                books = bookRepository.searchBooksByName(query.trim());
            }
        } else {
            books = bookRepository.searchBooksByName(query.trim());
        }
        
        populateMetadata(model);

        model.addAttribute("books", books);
        model.addAttribute("selectedLanguage", "All");
        model.addAttribute("selectedCategory", "All");
        model.addAttribute("selectedSubCategory", "All");
        
        if (SessionUtil.isAdmin(session)) {
            return "home";
        } else {
            return "user_home";
        }
    }

    @PostMapping("/add_favorite")
    public String addFavorite(@RequestParam Long book_no, HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        if (user.getId() == null) {
            return "redirect:/login";
        }
        // Reload user from DB to get current state
        User dbUser = userRepository.findById(user.getId()).orElse(null);
        Books book = bookRepository.findById(book_no).orElse(null);
        
        if (dbUser != null && book != null) {
            dbUser.addFavoriteBook(book);
            userRepository.save(dbUser);
            // Update session user
            session.setAttribute("user", dbUser);
        }
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/home");
    }

    @PostMapping("/remove_favorite")
    public String removeFavorite(@RequestParam Long book_no, HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        if (user.getId() == null) {
            return "redirect:/login";
        }
        User dbUser = userRepository.findById(user.getId()).orElse(null);
        Books book = bookRepository.findById(book_no).orElse(null);
        
        if (dbUser != null && book != null) {
            dbUser.removeFavoriteBook(book);
            userRepository.save(dbUser);
            session.setAttribute("user", dbUser);
        }
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/home");
    }

    @GetMapping("/favorites")
    public String viewFavorites(HttpSession session, Model model) {
        if (!SessionUtil.isLoggedIn(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        if (user.getId() == null) {
            return "redirect:/login";
        }
        User dbUser = userRepository.findById(user.getId()).orElse(null);
        
        if (dbUser != null) {
            model.addAttribute("favoriteBooks", dbUser.getFavoriteBooks());
        }
        
        return "fav";
    }

    // --- Notification & Broadcast APIs ---

    @GetMapping("/api/notifications")
    @ResponseBody
    public List<Notification> getNotifications(HttpSession session) {
        if (!SessionUtil.isLoggedIn(session)) return java.util.Collections.emptyList();
        
        List<Notification> allNotifs = notificationRepository.findByTarget_audienceOrderByIdDesc("ALL");
        String role = (String) session.getAttribute("role");
        if (role != null) {
            allNotifs.addAll(notificationRepository.findByTarget_audienceOrderByIdDesc(role.toUpperCase()));
        }
        allNotifs.sort((n1, n2) -> n2.getId().compareTo(n1.getId()));
        return allNotifs;
    }

    @PostMapping("/api/notifications/broadcast")
    @ResponseBody
    public Map<String, String> broadcastNotice(@RequestParam("topic") String topic, @RequestParam("message") String message, @RequestParam("target_audience") String targetAudience, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            response.put("status", "error");
            response.put("message", "Unauthorized");
            return response;
        }
        User user = (User) session.getAttribute("user");
        Notification notification = new Notification(topic, message, user.getFull_name(), targetAudience, java.time.LocalDateTime.now());
        notificationRepository.save(notification);
        response.put("status", "success");
        return response;
    }

    // --- Book Request & Approval Workflow APIs ---

    @GetMapping("/manage_requests")
    public String manageRequests(HttpSession session, Model model) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        
        List<BookRequest> requests = bookRequestRepository.findAllByOrderByIdDesc();
        model.addAttribute("requests", requests);
        model.addAttribute("pendingCount", bookRequestRepository.countByStatus("PENDING"));
        return "manage_requests";
    }

    @PostMapping("/requests/accept")
    public String acceptRequest(@RequestParam("id") Long id, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) return "redirect:/login";
        
        BookRequest req = bookRequestRepository.findById(id).orElse(null);
        if (req != null && "PENDING".equals(req.getStatus())) {
            Books book = new Books();
            book.setBook_name(req.getBook_name());
            book.setBook_author(req.getBook_author());
            book.setBook_category(req.getBook_category());
            book.setBook_sub_category(req.getBook_sub_category());
            book.setBook_language(req.getBook_language());
            book.setPdf_file_path(req.getPdf_file_path());
            
            bookRepository.save(book);
            req.setStatus("ACCEPTED");
            bookRequestRepository.save(req);
            
            Notification notification = new Notification("Book Approved", "Your book '" + req.getBook_name() + "' has been approved and published!", "System", "ALL", java.time.LocalDateTime.now());
            notificationRepository.save(notification);
            
            ra.addFlashAttribute("successMessage", "Request accepted! Book has been published.");
        }
        return "redirect:/manage_requests";
    }

    @PostMapping("/requests/reject")
    public String rejectRequest(@RequestParam("id") Long id, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.isAdmin(session)) return "redirect:/login";
        
        BookRequest req = bookRequestRepository.findById(id).orElse(null);
        if (req != null && "PENDING".equals(req.getStatus())) {
            req.setStatus("REJECTED");
            bookRequestRepository.save(req);
            ra.addFlashAttribute("successMessage", "Request rejected.");
        }
        return "redirect:/manage_requests";
    }
}



















