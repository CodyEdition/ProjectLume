<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Project Lume - Study history</title>
        <style>
            @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
            body {
                font-family: Arial, sans-serif;
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
            .header a {
                color: white;
                text-decoration: none;
                padding: 0.5rem 1rem;
                border: 1px solid white;
                border-radius: 4px;
            }
            .stats, .stat-header {
                /* Center div */
                text-align: center;
                width: fit-content;
                margin: 0 auto;
            }
            table {
                border-collapse: collapse;
                margin: 20% auto;
            }
            td, th {
                border-style: solid;
            }
            th {
                background: #a0aab1;
            }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>Project Lume</h1>
            <div>
                <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
                <a href="${pageContext.request.contextPath}/auth/logout">Logout</a>
            </div>
        </div>

        <div class="stat-header">
            <h2>Study history and statistics</h2>
        </div>

        <div class="stats">
            <c:choose>
                <c:when test="${empty sessions}">
                    <p>No history available.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                        <tr>
                            <th scope="col">Date</th>
                            <th scope="col">Deck name</th>
                            <th scope="col">Cards studied</th>
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

</body>
</html>

