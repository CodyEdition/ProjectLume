package com.projectlume.servlet;

import com.projectlume.dao.DeckDAO;
import com.projectlume.dao.StudySessionDAO;
import com.projectlume.dto.StudySessionDTO;
import com.projectlume.factory.DAOFactory;
import com.projectlume.model.Deck;
import com.projectlume.model.StudySession;
import com.projectlume.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(name = "StudyHistoryServlet", urlPatterns = {"/study/history"})
public class StudyHistoryServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(StudyHistoryServlet.class.getName());
    private final StudySessionDAO sessionHistoryDao;
    private final DeckDAO deckDao;

    public StudyHistoryServlet() {
        this.sessionHistoryDao = DAOFactory.createStudySessionDAO();
        this.deckDao = DAOFactory.createDeckDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Ensure user's logged in; if not, redirect and request authentication.
        if (!isUserLoggedIn(request)) {
            logger.info("User not logged in; redirecting to /auth");
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // List of stats to be displayed by study-history.jsp
        ArrayList<StudySessionDTO> dtoList = new ArrayList<>();

        // Fetch a list of study sessions
        Long userId = getCurrentUserId(request);
        List<StudySession> sessionList = Collections.emptyList();
        try {
            sessionList = sessionHistoryDao.findByUserId(userId);
        } catch (SQLException e) {
            logger.severe("Error fetching study history: " + e.getMessage());
            request.setAttribute("error", "Failed to fetch study history");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }

        // Convert relevant statistics to StudySessionDTO format, add to list
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, h:mma");
        for (int i = 0; i < sessionList.size(); i++) {
            StudySession session = sessionList.get(i);

            // Convert date to a more readable format
            // e.g. 2025-11-17T03:00:04 becomes 2025-11-17, 3:00am (or similar)
            LocalDateTime date = session.getSessionDate();
            String formattedDate = (date != null) ? dateFormatter.format(date) : "";

            // Fetch the title of the deck that was used in the session
            Long deckId = session.getDeckId();
            Deck deck = null;
            try {
                deck = deckDao.findById(deckId);
            } catch (SQLException e) {
                logger.severe("Error fetching deck " + i + ": " + e.getMessage());
                request.setAttribute("error", "Failed to fetch deck (ID: " + deckId + ")");
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            }
            String deckName = (deck != null ? deck.getName() : "");

            int studyCount = session.getCardsStudied();

            float accuracy = calculateAccuracy(session.getCorrectAnswers(), session.getIncorrectAnswers());
            // Truncate to 2 decimal places, append percentage symbol
            String formattedAccuracy = String.format("%.2f%%", accuracy);

            dtoList.add(new StudySessionDTO(formattedDate, deckName, studyCount, formattedAccuracy));
        }

        request.setAttribute("sessions", dtoList);

        // Display study history
        request.getRequestDispatcher("/WEB-INF/views/study-history.jsp").forward(request, response);
    }

    /**
     * Check if user is logged in
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    /**
     * Get current user ID from session
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        User user = (User) session.getAttribute("user");
        return user != null ? user.getId() : null;
    }

    /**
     * Given the correct and incorrect counts, calculate the accuracy as a percentage.
     */
    private float calculateAccuracy(int correctCount, int incorrectCount) {
        int total = (correctCount + incorrectCount);
        // Division by zero should be impossible with current code, but a check is made regardless
        return (total == 0) ? 0 : ((float) 100 * correctCount / total);
    }


}