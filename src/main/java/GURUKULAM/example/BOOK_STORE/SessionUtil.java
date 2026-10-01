package GURUKULAM.example.BOOK_STORE;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static boolean isLoggedIn(HttpSession session){
        return session != null && session.getAttribute("user") != null;
    }

    public static boolean isOwner(HttpSession session) {
        if (session == null) return false;
        String role = (String) session.getAttribute("role");
        if (role == null) {
            User user = (User) session.getAttribute("user");
            if (user != null) role = user.getRole();
        }
        return "owner".equalsIgnoreCase(role);
    }

    public static boolean isAdmin(HttpSession session) {
        if (session == null) return false;
        String role = (String) session.getAttribute("role");
        if (role == null) {
            User user = (User) session.getAttribute("user");
            if (user != null) role = user.getRole();
        }
        return "admin".equalsIgnoreCase(role) || "owner".equalsIgnoreCase(role);
    }

    public static boolean isUser(HttpSession session) {
        if (session == null) return false;
        String role = (String) session.getAttribute("role");
        if (role == null) {
            User user = (User) session.getAttribute("user");
            if (user != null) role = user.getRole();
        }
        return "user".equalsIgnoreCase(role);
    }
}
