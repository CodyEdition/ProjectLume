<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Dashboard</title>
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
            color: #007bff;
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
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 1rem;
        }
        .deck-card {
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 1.5rem;
            background-color: #f8f9fa;
        }
        .deck-card h4 {
            margin: 0 0 0.5rem 0;
            color: #333;
        }
        .deck-card p {
            margin: 0 0 1rem 0;
            color: #666;
        }
        .deck-actions {
            display: flex;
            gap: 0.5rem;
        }
        .btn-sm {
            padding: 0.5rem 1rem;
            font-size: 0.875rem;
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
    <div class="header">
        <h1>Project Lume</h1>
        <div class="user-info">
            <span>Welcome, ${user.firstName}!</span>
            <a href="${pageContext.request.contextPath}/profile">Profile</a>
            <a href="${pageContext.request.contextPath}/auth/logout">Logout</a>
        </div>
    </div>
    
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
                <c:when test="${not empty decks}">
                    <div class="deck-grid">
                        <c:forEach var="deck" items="${decks}">
                            <div class="deck-card">
                                <h4>${deck.name}</h4>
                                <p>${deck.description}</p>
                                <div class="deck-actions">
                                    <a href="${pageContext.request.contextPath}/card?deckId=${deck.id}" class="btn btn-sm">View Cards</a>
                                    <a href="${pageContext.request.contextPath}/card/new?deckId=${deck.id}" class="btn btn-sm btn-success">Add Card</a>
                                    <a href="${pageContext.request.contextPath}/deck/edit/${deck.id}" class="btn btn-sm btn-secondary">Edit</a>
                                    <a href="${pageContext.request.contextPath}/deck/delete/${deck.id}" class="btn btn-sm btn-danger" 
                                       onclick="return confirm('Are you sure you want to delete this deck?')">Delete</a>
                                </div>
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
