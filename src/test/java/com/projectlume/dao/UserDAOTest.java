package com.projectlume.dao;

import com.projectlume.factory.DAOFactory;
import com.projectlume.model.User;
import com.projectlume.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for UserDAO
 * Tests JDBC operations: connections, PreparedStatement, ResultSet, and database operations
 */
public class UserDAOTest extends BaseTest {
    private UserDAO userDAO;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        userDAO = DAOFactory.createUserDAO();
    }
    
    @Test
    public void testCreateUser() throws SQLException {
        // Arrange
        User user = new User(generateUniqueUsername(), generateUniqueEmail(), 
                            BCrypt.hashpw("password123", BCrypt.gensalt()), 
                            "Test", "User");
        
        // Act
        User createdUser = userDAO.create(user);
        
        // Assert
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals(user.getUsername(), createdUser.getUsername());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertNotNull(createdUser.getCreatedAt());
        assertNotNull(createdUser.getUpdatedAt());
    }
    
    @Test
    public void testFindUserById() throws SQLException {
        // Arrange
        User user = new User(generateUniqueUsername(), generateUniqueEmail(), 
                            BCrypt.hashpw("password123", BCrypt.gensalt()), 
                            "Test", "User");
        User createdUser = userDAO.create(user);
        
        // Act
        User foundUser = userDAO.findById(createdUser.getId());
        
        // Assert
        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getUsername(), foundUser.getUsername());
    }
    
    @Test
    public void testFindUserByUsername() throws SQLException {
        // Arrange
        String username = generateUniqueUsername();
        User user = new User(username, generateUniqueEmail(), 
                            BCrypt.hashpw("password123", BCrypt.gensalt()), 
                            "Test", "User");
        userDAO.create(user);
        
        // Act
        User foundUser = userDAO.findByUsername(username);
        
        // Assert
        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
    }
    
    @Test
    public void testFindUserByEmail() throws SQLException {
        // Arrange
        String email = generateUniqueEmail();
        User user = new User(generateUniqueUsername(), email, 
                            BCrypt.hashpw("password123", BCrypt.gensalt()), 
                            "Test", "User");
        userDAO.create(user);
        
        // Act
        User foundUser = userDAO.findByEmail(email);
        
        // Assert
        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
    }
    
    @Test
    public void testUpdateUser() throws SQLException {
        // Arrange
        User user = new User(generateUniqueUsername(), generateUniqueEmail(), 
                            BCrypt.hashpw("password123", BCrypt.gensalt()), 
                            "Original", "Name");
        User createdUser = userDAO.create(user);
        
        // Act
        createdUser.setFirstName("Updated");
        createdUser.setLastName("Name");
        User updatedUser = userDAO.update(createdUser);
        
        // Assert
        assertNotNull(updatedUser);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }
    
    @Test
    public void testDeleteUser() throws SQLException {
        // Arrange
        User user = new User(generateUniqueUsername(), generateUniqueEmail(), 
                            BCrypt.hashpw("password123", BCrypt.gensalt()), 
                            "Test", "User");
        User createdUser = userDAO.create(user);
        
        // Act
        boolean deleted = userDAO.delete(createdUser.getId());
        
        // Assert
        assertTrue(deleted);
        User deletedUser = userDAO.findById(createdUser.getId());
        assertNull(deletedUser); // Should not find inactive user
    }
    
    @Test
    public void testFindAllUsers() throws SQLException {
        // Arrange
        userDAO.create(new User(generateUniqueUsername(), generateUniqueEmail(), 
                               BCrypt.hashpw("password123", BCrypt.gensalt()), 
                               "User", "One"));
        userDAO.create(new User(generateUniqueUsername(), generateUniqueEmail(), 
                               BCrypt.hashpw("password123", BCrypt.gensalt()), 
                               "User", "Two"));
        
        // Act
        java.util.List<User> users = userDAO.findAll();
        
        // Assert
        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }
}

