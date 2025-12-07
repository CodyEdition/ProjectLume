<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Dashboard</title>
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
        .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }
        .welcome {
            background-color: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 2rem;
        }
        .welcome h2 {
            margin: 0 0 1rem 0;
            color: #333;
        }
        .actions {
            display: flex;
            gap: 1rem;
            margin-top: 1rem;
        }
        .btn {
            padding: 0.75rem 1.5rem;
            background-color: #ffb677;
            color: #111111;
            border: 1px solid #ffb677;
            border-radius: 4px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            text-align: center;
        }
        .btn:hover { background-color: #ffa055; border-color: #ffa055; }
        .btn-success { background-color: #ffb677; border-color: #ffb677; }
        .btn-success:hover { background-color: #ffa055; border-color: #ffa055; }
        .btn-secondary { background-color: #bfbfbf; color: #ffffff; border-color: #bfbfbf; }
        .btn-secondary:hover { background-color: #a9a9a9; border-color: #a9a9a9; }
        .btn-danger { background-color: #111111; color: #ffffff; border: 1px solid #111111; }
        .btn-danger:hover { background-color: #222222; border-color: #222222; }
        .decks-section {
            background-color: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .decks-section h3 {
            margin: 0 0 1rem 0;
            color: #333;
        }
        .deck-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 2rem;
            padding: 1rem 0;
        }
        .decks-section {
            background-color: #1a1a1a;
            padding: 2rem;
            border-radius: 8px;
        }
        .decks-section h3 {
            color: white;
            margin-bottom: 1.5rem;
        }
        .btn-danger { background-color: #111111; color: #ffffff; border-color: #111111; }
        .btn-danger:hover { background-color: #222222; border-color: #222222; }
        .btn-secondary {
            background-color: #6c757d;
        }
        .btn-secondary:hover {
            background-color: #545b62;
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
        
        <div class="welcome">
            <h2>Welcome to your Dashboard</h2>
            <p>Manage your flashcard decks and start studying!</p>
            <div class="actions">
                <a href="${pageContext.request.contextPath}/deck/new" class="btn btn-success">Create New Deck</a>
            </div>
        </div>
        
        <div class="decks-section">
            <h3>Your Decks</h3>
            <c:choose>
                <c:when test="${not empty deckStatsList}">
                    <div class="deck-grid">
                        <c:forEach var="deckStats" items="${deckStatsList}" varStatus="loop">
                            <c:set var="cardNumber" value="${loop.index + 1}" />
                            <c:set var="cardNumberPadded" value="${cardNumber < 10 ? '00' : (cardNumber < 100 ? '0' : '')}${cardNumber}" />
                            <c:set var="dateCode" value="${deckStats.dateCode}" />
                            <c:set var="serialNumber" value="${dateCode}S3" />
                            
                            <div class="collectible-card card-scanlines">
                                <div class="card-specs-bar">
                                    <span>DECK | ID: ${deckStats.deckId}</span>
                                    <span class="card-badge">${dateCode}</span>
                                </div>
                                
                                <div class="card-artwork-panel">
                                    <div class="card-holographic"></div>
                                    
                                    <div class="card-content">
                                        <div>
                                            <div class="card-number">${cardNumberPadded}</div>
                                            <div class="card-title">${deckStats.name}</div>
                                            <div class="card-description">${deckStats.description}</div>
                                            <div style="margin-top: 10px; font-size: 0.9em; color: #888;">
                                                <div>Cards: ${deckStats.totalCards} | Studied: ${deckStats.cardsStudied}</div>
                                                <div>Completion: <fmt:formatNumber value="${deckStats.completionPercentage}" maxFractionDigits="1"/>%</div>
                                                <c:if test="${deckStats.averageAccuracy != null}">
                                                    <div>Accuracy: <fmt:formatNumber value="${deckStats.averageAccuracy}" maxFractionDigits="1"/>%</div>
                                                </c:if>
                                            </div>
                                        </div>
                                        
                                        <div class="card-actions">
                                            <a href="${pageContext.request.contextPath}/study/${deckStats.deckId}" class="btn btn-success" title="Start studying this deck">Study</a>
                                            <a href="${pageContext.request.contextPath}/card?deckId=${deckStats.deckId}" class="btn btn-secondary" title="View cards">View</a>
                                            <a href="${pageContext.request.contextPath}/deck/edit/${deckStats.deckId}" class="btn btn-secondary" title="Edit deck">✎</a>
                                            <a href="${pageContext.request.contextPath}/deck/delete/${deckStats.deckId}" class="btn btn-danger" 
                                               onclick="return confirm('Are you sure you want to delete this deck?')" title="Delete deck">✕</a>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="card-serial">${serialNumber}</div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        <h4>No decks yet</h4>
                        <p>Create your first deck to get started!</p>
                        <a href="${pageContext.request.contextPath}/deck/new" class="btn btn-success">Create Your First Deck</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
