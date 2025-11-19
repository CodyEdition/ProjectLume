package com.projectlume.servlet;

import com.projectlume.dao.UserDAO;
import com.projectlume.exception.ValidationException;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.User;
import com.projectlume.service.AuthService;
import com.projectlume.service.AuthService.AuthException;
import com.projectlume.util.ValidationUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile/*"})
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ProfileServlet.class.getName());
    private final UserDAO userDAO;
    private final AuthService authService;
    
    public ProfileServlet() {
        this.userDAO = DAOFactory.createUserDAO();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Show profile page
            User user = (User) request.getSession().getAttribute("user");
            request.setAttribute("user", user);
            request.setAttribute("currentPage", "profile");
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        } else if (pathInfo.equals("/edit")) {
            // Show edit profile form
            User user = (User) request.getSession().getAttribute("user");
            request.setAttribute("user", user);
            request.setAttribute("currentPage", "profile");
            request.getRequestDispatcher("/WEB-INF/views/profile-edit.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/edit")) {
            // Handle profile update
            handleProfileUpdate(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Handle profile update form submission
     */
    private void handleProfileUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        // Extract form parameters
        String username = ValidationUtils.sanitizeString(request.getParameter("username"));
        String email = ValidationUtils.sanitizeString(request.getParameter("email"));
        String firstName = ValidationUtils.sanitizeString(request.getParameter("firstName"));
        String lastName = ValidationUtils.sanitizeString(request.getParameter("lastName"));
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        
        try {
            // Validate all input
            ValidationUtils.validateUsername(username);
            ValidationUtils.validateEmail(email);
            ValidationUtils.validateName(firstName, "firstName");
            ValidationUtils.validateName(lastName, "lastName");
            
            // Check username uniqueness (excluding current user)
            if (!username.equals(currentUser.getUsername())) {
                checkUsernameUniqueness(username);
            }
            
            // Check email uniqueness (excluding current user)
            if (!email.equals(currentUser.getEmail())) {
                checkEmailUniqueness(email);
            }
            
            // Update user information
            currentUser.setUsername(username);
            currentUser.setEmail(email);
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            
            userDAO.update(currentUser);
            
            // Handle optional password change
            if (currentPassword != null && !currentPassword.trim().isEmpty() &&
                newPassword != null && !newPassword.trim().isEmpty()) {
                try {
                    authService.changePassword(currentUser.getId(), currentPassword, newPassword);
                    logger.info("Password changed for user: " + currentUser.getUsername());
                } catch (AuthException e) {
                    request.setAttribute("error", "Password change failed: " + e.getMessage());
                    request.setAttribute("user", currentUser);
                    request.getRequestDispatcher("/WEB-INF/views/profile-edit.jsp").forward(request, response);
                    return;
                }
            }
            
            // Update session with new user data
            User updatedUser = userDAO.findById(currentUser.getId());
            if (updatedUser != null) {
                session.setAttribute("user", updatedUser);
                session.setAttribute("username", updatedUser.getUsername());
            }
            
            logger.info("Profile updated successfully for user: " + currentUser.getUsername());
            
            // Redirect to profile page on success
            response.sendRedirect(request.getContextPath() + "/profile");
            
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("user", currentUser);
            request.getRequestDispatcher("/WEB-INF/views/profile-edit.jsp").forward(request, response);
        } catch (SQLException e) {
            logger.severe("Database error during profile update: " + e.getMessage());
            request.setAttribute("error", "An error occurred while updating your profile. Please try again.");
            request.setAttribute("user", currentUser);
            request.getRequestDispatcher("/WEB-INF/views/profile-edit.jsp").forward(request, response);
        }
    }

    /**
     * Check if user is logged in
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    /**
     * Check if username is already taken by another user
     */
    private void checkUsernameUniqueness(String username) throws ValidationException, SQLException {
        User existingUser = userDAO.findByUsername(username);
        if (existingUser != null) {
            throw new ValidationException("username", "Username is already taken");
        }
    }

    /**
     * Check if email is already taken by another user
     */
    private void checkEmailUniqueness(String email) throws ValidationException, SQLException {
        User existingUser = userDAO.findByEmail(email);
        if (existingUser != null) {
            throw new ValidationException("email", "Email is already taken");
        }
    }
}


