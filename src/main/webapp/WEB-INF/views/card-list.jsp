<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Cards</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/card-styles.css">
    <style>
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Italic.otf') format('opentype'); font-weight: 400; font-style: italic; font-display: swap; }
        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 0; }
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
        .header > div {
            display: flex;
            align-items: center;
            gap: 1rem;
        }
        .container { max-width: 1100px; margin: 2rem auto; padding: 0 2rem; }
        .card { background: #fff; padding: 1.5rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 1rem; }
        .error { background-color: #f8d7da; color: #721c24; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem; }
        .actions { display: flex; gap: 0.5rem; margin-top: 0.75rem; }
        .btn { padding: 0.5rem 1rem; background: #ffb677; color: #111111; border: 1px solid #ffb677; border-radius: 4px; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; text-align: center; }
        .btn:hover { background: #ffa055; border-color: #ffa055; }
        .btn-danger { background: #111111; color: #ffffff; border: 1px solid #111111; }
        .btn-danger:hover { background: #222222; border-color: #222222; }
        .btn-secondary { background: #bfbfbf; color: #ffffff; border-color: #bfbfbf; }
        .btn-secondary:hover { background: #a9a9a9; border-color: #a9a9a9; }
        .btn-danger { background: #111111; color: #ffffff; }
        .btn-danger:hover { background: #222222; }
        .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 2rem; padding: 1rem 0; }
        .card-section { margin-top: 2rem; }
        .card-section h2 { color: #333; margin-bottom: 1rem; }
        .meta { color: #666; font-size: 0.9rem; margin-bottom: 0.5rem; }
        h2 { margin: 0 0 0.25rem 0; font-family: 'Debata', 'Rokiest', Arial, sans-serif; }

        .retro-card {
            position: relative;
            background-color: #fafafa;
            background-image:
                radial-gradient(rgba(0,0,0,0.03) 1px, transparent 1.5px),
                radial-gradient(rgba(0,0,0,0.02) 0.5px, transparent 1.5px);
            background-size: 6px 6px, 8px 8px;
            background-position: 0 0, 3px 3px;
            border-radius: 12px;
            border: 1px solid rgba(0,0,0,0.12);
            box-shadow: 0 4px 12px rgba(0,0,0,0.10);
            padding: 1.25rem 1.25rem 1rem 1.25rem;
            overflow: hidden;
        }
        .retro-title { font-family: Georgia, 'Times New Roman', serif; letter-spacing: 0.2px; }
        .retro-meta { color: #6b6b6b; font-size: 0.85rem; margin-bottom: 0.5rem; }
        .retro-actions .btn { border: 1px solid rgba(0,0,0,0.08); }
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

    <div class="container">
        <c:if test="${not empty error}">
            <div class="error">${error}</div>
        </c:if>

        <div class="card">
            <h2>Deck: ${deck.name}</h2>
            <div class="meta">${deck.description}</div>
            <div class="actions">
                <a class="btn" href="${pageContext.request.contextPath}/card/new?deckId=${deck.id}">Add Card</a>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty cards}">
                <div class="card-section">
                    <h2>Cards in ${deck.name}</h2>
                    <div class="grid">
                        <c:forEach var="card" items="${cards}" varStatus="loop">
                            <c:set var="cardNumber" value="${loop.index + 1}" />
                            <c:set var="cardNumberPadded" value="${cardNumber < 10 ? '00' : (cardNumber < 100 ? '0' : '')}${cardNumber}" />
                            <%
                                com.projectlume.model.Card card = (com.projectlume.model.Card) pageContext.getAttribute("card");
                                String dateCode = "NOV10.25.10";
                                if (card != null && card.getCreatedAt() != null) {
                                    java.time.LocalDateTime date = card.getCreatedAt();
                                    String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", 
                                                          "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
                                    String month = monthNames[date.getMonthValue() - 1];
                                    String day = String.format("%02d", date.getDayOfMonth());
                                    String yearShort = String.format("%02d", date.getYear() % 100);
                                    dateCode = month + day + "." + yearShort + "." + day;
                                }
                                pageContext.setAttribute("dateCode", dateCode);
                            %>
                            <c:set var="serialNumber" value="${dateCode}S3" />
                            <c:set var="badgeClass" value="" />
                            <c:choose>
                                <c:when test="${card.difficultyLevel == 'EASY'}">
                                    <c:set var="badgeClass" value="green" />
                                </c:when>
                                <c:when test="${card.difficultyLevel == 'MEDIUM'}">
                                    <c:set var="badgeClass" value="blue" />
                                </c:when>
                                <c:when test="${card.difficultyLevel == 'HARD'}">
                                    <c:set var="badgeClass" value="orange" />
                                </c:when>
                            </c:choose>
                            
                            <div class="collectible-card card-scanlines">
                                <div class="card-specs-bar">
                                    <span>CARD | ID: ${card.id}</span>
                                    <span class="card-badge ${badgeClass}">${card.difficultyLevel}</span>
                                </div>
                                
                                <div class="card-artwork-panel">
                                    <div class="card-holographic"></div>
                                    
                                    <div class="card-content">
                                        <div>
                                            <div class="card-number">${cardNumberPadded}</div>
                                            <div class="card-title">${card.frontText}</div>
                                            <div class="card-description" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #ddd;">
                                                <strong>Answer:</strong><br/>
                                                ${card.backText}
                                            </div>
                                        </div>
                                        
                                        <div class="card-actions">
                                            <a href="${pageContext.request.contextPath}/card/edit/${card.id}" class="btn btn-secondary" title="Edit card">✎</a>
                                            <a href="${pageContext.request.contextPath}/card/delete/${card.id}" class="btn btn-danger"
                                               onclick="return confirm('Delete this card?');" title="Delete card">✕</a>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="card-serial">${serialNumber}</div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card">
                    <div class="meta">No cards yet in this deck.</div>
                    <a class="btn" href="${pageContext.request.contextPath}/card/new?deckId=${deck.id}">Add your first card</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>


