package GURUKULAM.example.BOOK_STORE;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static boolean isLoggedIn(HttpSession session){
        return session !=null && session.getAttribute("user") !=null;
    }
    public static boolean isOwner(HttpSession session) {
        return "owner".equals(session.getAttribute("role"));
    }

    public static boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "admin".equals(role) || "owner".equals(role);
    }

    public static boolean isUser(HttpSession session) {
        return "user".equals(session.getAttribute("role"));
    }
}
