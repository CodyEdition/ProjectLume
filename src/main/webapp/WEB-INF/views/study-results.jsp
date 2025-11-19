<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Study Results</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/card-styles.css">
    <style>
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
        body {
            font-family: Arial, sans-serif;
            background-color: #1a1a1a;
            margin: 0;
            padding: 0;
            min-height: 100vh;
        }
        .header {
            background: linear-gradient(90deg, #2f2f2f 0%, #6b4a2f 40%, #ff7a00 80%, #ffa24d 100%);
            color: white;
            padding: 1rem 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .header h1 {
            margin: 0;
            text-shadow: 0 0 6px rgba(255,255,255,0.55);
            font-family: 'Debata', Arial, sans-serif;
        }
        .header .user-info {
            display: flex;
            align-items: center;
            gap: 1rem;
            text-shadow: 0 0 5px rgba(255,255,255,0.6);
        }
        .header a {
            color: white;
            text-decoration: none;
            padding: 0.5rem 1rem;
            border: 1px solid white;
            border-radius: 4px;
            text-shadow: 0 0 5px rgba(255,255,255,0.6);
        }
        .header a:hover {
            background-color: white;
            color: #ff7a00;
        }
        .results-container {
            max-width: 600px;
            margin: 3rem auto;
            padding: 0 2rem;
        }
        .results-card {
            width: 100%;
            max-width: 500px;
            margin: 0 auto;
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 1rem;
            margin: 1.5rem 0;
        }
        .stat-item {
            text-align: center;
            padding: 1rem;
            background-color: rgba(255, 255, 255, 0.05);
            border-radius: 4px;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }
        .stat-value {
            font-size: 2rem;
            font-weight: bold;
            color: #ffb677;
            font-family: 'Courier New', monospace;
        }
        .stat-label {
            font-size: 0.9rem;
            color: #ccc;
            margin-top: 0.5rem;
            font-family: 'Courier New', monospace;
        }
        .accuracy-high {
            color: #00ff00;
        }
        .accuracy-medium {
            color: #ffb677;
        }
        .accuracy-low {
            color: #ff4444;
        }
        .action-buttons {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-top: 2rem;
        }
        .action-btn {
            background-color: #ffb677;
            color: #111;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 4px;
            text-decoration: none;
            display: inline-block;
            font-size: 1rem;
            cursor: pointer;
            transition: all 0.2s ease;
            font-family: 'Courier New', monospace;
        }
        .action-btn:hover {
            background-color: #ffa055;
            transform: scale(1.05);
        }
        .action-btn.secondary {
            background-color: #6c757d;
            color: white;
        }
        .action-btn.secondary:hover {
            background-color: #545b62;
        }
        .results-info {
            color: white;
            text-align: center;
            margin-bottom: 1rem;
            font-family: 'Courier New', monospace;
        }
        .session-date {
            color: #ccc;
            font-size: 0.9rem;
            margin-top: 1rem;
            font-family: 'Courier New', monospace;
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/includes/header.jsp" />
    
    <div class="results-container">
        <c:if test="${not empty error}">
            <div style="background-color: #f8d7da; color: #721c24; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
                ${error}
            </div>
        </c:if>
        
        <c:if test="${not empty session and not empty deck}">
            <div class="results-info">
                <h2 style="color: white; margin-bottom: 0.5rem;">Study Session Complete!</h2>
                <div style="color: #ffb677; font-size: 1.2rem;">${deck.name}</div>
            </div>
            
            <div class="results-card">
                <div class="collectible-card card-scanlines">
                    <div class="card-specs-bar">
                        <span>RESULTS | SESSION ${session.id}</span>
                        <span class="card-badge">COMPLETE</span>
                    </div>
                    
                    <div class="card-artwork-panel">
                        <div class="card-holographic"></div>
                        
                        <div class="card-content">
                            <div style="padding: 2rem 0;">
                                <c:set var="totalCards" value="${session.cardsStudied}" />
                                <c:set var="correctAnswers" value="${session.correctAnswers}" />
                                <c:set var="incorrectAnswers" value="${session.incorrectAnswers}" />
                                <c:set var="accuracy" value="${totalCards > 0 ? (correctAnswers * 100.0 / totalCards) : 0}" />
                                <c:set var="durationMinutes" value="${session.sessionDurationMinutes}" />
                                <c:set var="durationHours" value="${durationMinutes / 60}" />
                                <c:set var="durationRemainderMinutes" value="${durationMinutes % 60}" />
                                
                                <div class="stats-grid">
                                    <div class="stat-item">
                                        <div class="stat-value">${totalCards}</div>
                                        <div class="stat-label">Cards Studied</div>
                                    </div>
                                    
                                    <div class="stat-item">
                                        <div class="stat-value" style="color: #00ff00;">${correctAnswers}</div>
                                        <div class="stat-label">Correct</div>
                                    </div>
                                    
                                    <div class="stat-item">
                                        <div class="stat-value" style="color: #ff4444;">${incorrectAnswers}</div>
                                        <div class="stat-label">Incorrect</div>
                                    </div>
                                    
                                    <div class="stat-item">
                                        <c:choose>
                                            <c:when test="${accuracy >= 80}">
                                                <div class="stat-value accuracy-high"><fmt:formatNumber value="${accuracy}" maxFractionDigits="1" />%</div>
                                            </c:when>
                                            <c:when test="${accuracy >= 50}">
                                                <div class="stat-value accuracy-medium"><fmt:formatNumber value="${accuracy}" maxFractionDigits="1" />%</div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="stat-value accuracy-low"><fmt:formatNumber value="${accuracy}" maxFractionDigits="1" />%</div>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="stat-label">Accuracy</div>
                                    </div>
                                </div>
                                
                                <div style="text-align: center; margin-top: 1.5rem; padding-top: 1.5rem; border-top: 2px solid rgba(255, 255, 255, 0.1);">
                                    <div class="stat-label" style="margin-bottom: 0.5rem;">Session Duration</div>
                                    <div class="stat-value" style="font-size: 1.5rem;">
                                        <c:choose>
                                            <c:when test="${durationHours >= 1}">
                                                <fmt:formatNumber value="${durationHours}" maxFractionDigits="0" />h 
                                                <c:if test="${durationRemainderMinutes > 0}">
                                                    ${durationRemainderMinutes}m
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>
                                                ${durationMinutes}m
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                
                                <c:if test="${not empty session.sessionDate}">
                                    <%
                                        com.projectlume.model.StudySession sessionObj = (com.projectlume.model.StudySession) pageContext.getAttribute("session");
                                        String formattedDate = "";
                                        if (sessionObj != null && sessionObj.getSessionDate() != null) {
                                            java.time.LocalDateTime date = sessionObj.getSessionDate();
                                            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                                                  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                                            String month = monthNames[date.getMonthValue() - 1];
                                            String day = String.valueOf(date.getDayOfMonth());
                                            String year = String.valueOf(date.getYear());
                                            String hour = String.format("%02d", date.getHour());
                                            String minute = String.format("%02d", date.getMinute());
                                            formattedDate = month + " " + day + ", " + year + " at " + hour + ":" + minute;
                                        }
                                        pageContext.setAttribute("formattedDate", formattedDate);
                                    %>
                                    <div class="session-date">
                                        ${formattedDate}
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card-serial">SESSION COMPLETE</div>
                </div>
                
                <div class="action-buttons">
                    <a href="${pageContext.request.contextPath}/study/${deck.id}" class="action-btn">Study Again</a>
                    <a href="${pageContext.request.contextPath}/dashboard" class="action-btn secondary">Back to Dashboard</a>
                </div>
            </div>
        </c:if>
        
        <c:if test="${empty session or empty deck}">
            <div style="text-align: center; color: white; padding: 2rem;">
                <p>No session data available.</p>
                <a href="${pageContext.request.contextPath}/dashboard" class="action-btn secondary" style="margin-top: 1rem;">Back to Dashboard</a>
            </div>
        </c:if>
    </div>
</body>
</html>

