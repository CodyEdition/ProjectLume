<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Card Form</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/card-styles.css">
    <style>
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
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
            max-width: 600px;
            margin: 2rem auto;
            padding: 0 2rem;
        }
        .form-container {
            background-color: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .form-group {
            margin-bottom: 1rem;
        }
        .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            color: #333;
            font-weight: bold;
        }
        .form-group input,
        .form-group textarea,
        .form-group select {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
            box-sizing: border-box;
        }
        .form-group input:focus,
        .form-group textarea:focus,
        .form-group select:focus {
            outline: none;
            border-color: #ffb677;
        }
        .form-group textarea {
            height: 100px;
            resize: vertical;
        }
        .btn {
            padding: 0.75rem 1.5rem;
            background-color: #ffb677; /* pastel orange */
            color: #111111; /* black text for readability */
            border: 1px solid #ffb677;
            border-radius: 4px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            margin-right: 1rem;
            text-align: center;
        }
        .btn:hover { background-color: #ffa055; border-color: #ffa055; }
        .btn-secondary { background-color: #bfbfbf; color: #ffffff; border-color: #bfbfbf; }
        .btn-secondary:hover { background-color: #a9a9a9; border-color: #a9a9a9; }
        .btn-danger { background-color: #111111; color: #ffffff; border: 1px solid #111111; }
        .btn-danger:hover { background-color: #222222; border-color: #222222; }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            padding: 0.75rem;
            border-radius: 4px;
            margin-bottom: 1rem;
        }
        .form-actions {
            margin-top: 2rem;
        }
        .deck-info {
            background-color: #e9ecef;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1rem;
        }
        .preview-section {
            margin-top: 2rem;
            padding-top: 2rem;
            border-top: 2px solid #eee;
        }
        .preview-section h3 {
            color: #333;
            margin-bottom: 1rem;
            font-family: 'Debata', Arial, sans-serif;
        }
        .preview-card-wrapper {
            display: flex;
            justify-content: center;
            max-width: 400px;
            margin: 0 auto;
        }   
    </style>
</head>
<body>
    <div class="header">
        <h1>Project Lume</h1>
        <a href="${pageContext.request.contextPath}/card?deckId=${deck.id}">Back to Cards</a>
    </div>
    
    <div class="container">
        <div class="form-container">
            <h2 style="font-family: 'Debata', Arial, sans-serif;">${empty card ? 'Create New Card' : 'Edit Card'}</h2>
            
            <div class="deck-info">
                <strong>Deck:</strong> ${deck.name}
            </div>
            
            <c:if test="${not empty error}">
                <div class="error">
                    ${error}
                </div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/card${empty card ? '' : '/edit/'}${card.id}">
                <input type="hidden" name="deckId" value="${deck.id}">
                
                <div class="form-group">
                    <label for="frontText">Front Text:</label>
                    <textarea id="frontText" name="frontText" required>${card.frontText}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="backText">Back Text:</label>
                    <textarea id="backText" name="backText" required>${card.backText}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="difficultyLevel">Difficulty Level:</label>
                    <select id="difficultyLevel" name="difficultyLevel">
                        <option value="EASY" ${card.difficultyLevel == 'EASY' ? 'selected' : ''}>Easy</option>
                        <option value="MEDIUM" ${card.difficultyLevel == 'MEDIUM' ? 'selected' : ''}>Medium</option>
                        <option value="HARD" ${card.difficultyLevel == 'HARD' ? 'selected' : ''}>Hard</option>
                    </select>
                </div>
                
                <div class="form-actions">
                    <button type="submit" class="btn">${empty card ? 'Create Card' : 'Update Card'}</button>
                    <a href="${pageContext.request.contextPath}/card?deckId=${deck.id}" class="btn btn-secondary">Cancel</a>
                </div>
            </form>

            <div class="preview-section">
                <h3>Preview</h3>
                <div class="preview-card-wrapper">
                    <%
                        com.projectlume.model.Card previewCard = (com.projectlume.model.Card) pageContext.getAttribute("card");
                        String previewDifficulty = "MEDIUM";
                        String badgeClass = "blue";
                        
                        if (previewCard != null && previewCard.getDifficultyLevel() != null) {
                            previewDifficulty = previewCard.getDifficultyLevel().toString();
                            if ("EASY".equals(previewDifficulty)) {
                                badgeClass = "green";
                            } else if ("MEDIUM".equals(previewDifficulty)) {
                                badgeClass = "blue";
                            } else if ("HARD".equals(previewDifficulty)) {
                                badgeClass = "red";
                            }
                        }
                        pageContext.setAttribute("previewDifficulty", previewDifficulty);
                        pageContext.setAttribute("previewBadgeClass", badgeClass);
                        
                        String previewSerial = "NOV11.25.11";
                        if (previewCard != null && previewCard.getCreatedAt() != null) {
                            java.time.LocalDateTime date = previewCard.getCreatedAt();
                            String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", 
                                                  "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
                            String month = monthNames[date.getMonthValue() - 1];
                            String day = String.format("%02d", date.getDayOfMonth());
                            String yearShort = String.format("%02d", date.getYear() % 100);
                            previewSerial = month + day + "." + yearShort + "." + day;
                        }
                        pageContext.setAttribute("previewSerial", previewSerial);
                    %>
                    <div class="collectible-card card-scanlines">
                        <div class="card-specs-bar">
                            <span>CARD | ID: ${not empty card && card.id != null ? card.id : 'NEW'}</span>
                            <span class="card-badge ${previewBadgeClass}" id="previewBadge">${previewDifficulty}</span>
                        </div>
                        
                        <div class="card-artwork-panel">
                            <div class="card-holographic"></div>
                            
                            <div class="card-content">
                                <div>
                                    <div class="card-number">001</div>
                                    <div class="card-title" id="previewFrontText">
                                        <c:out value="${not empty card ? card.frontText : '(front text)'}" default="(front text)"/>
                                    </div>
                                    <div class="card-description" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #ddd;">
                                        <strong>Answer:</strong><br/>
                                        <span id="previewBackText">
                                            <c:out value="${not empty card ? card.backText : '(back text)'}" default="(back text)"/>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="card-serial">${previewSerial}S3</div>
                    </div>
                </div>
            </div>
            
            <script>
                // Update preview in real-time as user types
                document.addEventListener('DOMContentLoaded', function() {
                    var frontTextInput = document.getElementById('frontText');
                    var backTextInput = document.getElementById('backText');
                    var difficultySelect = document.getElementById('difficultyLevel');
                    var previewFrontText = document.getElementById('previewFrontText');
                    var previewBackText = document.getElementById('previewBackText');
                    var previewBadge = document.getElementById('previewBadge');
                    
                    function updatePreview() {
                        if (frontTextInput && previewFrontText) {
                            previewFrontText.textContent = frontTextInput.value || '(front text)';
                        }
                        if (backTextInput && previewBackText) {
                            previewBackText.textContent = backTextInput.value || '(back text)';
                        }
                        if (difficultySelect && previewBadge) {
                            var difficulty = difficultySelect.value;
                            previewBadge.textContent = difficulty;
                            
                            // Update badge class
                            previewBadge.className = 'card-badge';
                            if (difficulty === 'EASY') {
                                previewBadge.classList.add('green');
                            } else if (difficulty === 'MEDIUM') {
                                previewBadge.classList.add('blue');
                            } else if (difficulty === 'HARD') {
                                previewBadge.classList.add('red');
                            }
                        }
                    }
                    
                    // Initialize preview on page load
                    updatePreview();
                    
                    // Update preview on input changes
                    if (frontTextInput) {
                        frontTextInput.addEventListener('input', updatePreview);
                    }
                    if (backTextInput) {
                        backTextInput.addEventListener('input', updatePreview);
                    }
                    if (difficultySelect) {
                        difficultySelect.addEventListener('change', updatePreview);
                    }
                });
            </script>
        </div>
    </div>
</body>
</html>
