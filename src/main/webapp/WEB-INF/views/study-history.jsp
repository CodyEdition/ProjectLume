<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Study History</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/card-styles.css">
    <style>
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Italic.otf') format('opentype'); font-weight: 400; font-style: italic; font-display: swap; }
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            margin: 0;
            padding: 0;
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
            font-family: 'Debata', 'Rokiest', Arial, sans-serif;
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
        .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }
        .history-section {
            background-color: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .history-section h2 {
            margin: 0 0 1.5rem 0;
            color: #333;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 1rem;
        }
        th {
            background-color: #333;
            color: white;
            padding: 0.75rem 1rem;
            text-align: left;
            font-weight: bold;
            border: 1px solid #444;
        }
        td {
            padding: 0.75rem 1rem;
            border: 1px solid #ddd;
            color: #333;
        }
        tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #666;
        }
        .empty-state h4 {
            margin: 0 0 1rem 0;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            padding: 0.75rem;
            border-radius: 4px;
            margin-bottom: 1rem;
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/includes/header.jsp" />
    
    <div class="container">
        <c:if test="${not empty error}">
            <div class="error">
                ${error}
            </div>
        </c:if>
        
        <div class="history-section">
            <h2>Study History and Statistics</h2>
            <c:choose>
                <c:when test="${empty sessions}">
                    <div class="empty-state">
                        <h4>No study history available</h4>
                        <p>Start studying your decks to see your progress here!</p>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-success" style="display: inline-block; margin-top: 1rem; padding: 0.75rem 1.5rem; background-color: #ffb677; color: #111111; border: 1px solid #ffb677; border-radius: 4px; text-decoration: none;">Go to Dashboard</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">Date</th>
                                <th scope="col">Deck Name</th>
                                <th scope="col">Cards Studied</th>
                                <th scope="col">Accuracy</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${sessions}" var="session">
                                <tr>
                                    <td><c:out value="${session.date}"/></td>
                                    <td><c:out value="${session.name}"/></td>
                                    <td><c:out value="${session.studyCount}"/></td>
                                    <td><c:out value="${session.formattedAccuracy}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>

