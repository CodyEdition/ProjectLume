<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Card Form</title>
    <style>
        /* Rokiest font family (OTF files) */
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Medium.otf') format('opentype'); font-weight: 500; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Semibold.otf') format('opentype'); font-weight: 600; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Bold.otf') format('opentype'); font-weight: 700; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Extrabold.otf') format('opentype'); font-weight: 800; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Black.otf') format('opentype'); font-weight: 900; font-style: normal; font-display: swap; }
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            margin: 0;
            padding: 0;
        }
        .header {
            background-color: #007bff;
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
            text-shadow: 0 0 4px rgba(255,255,255,0.45);
        }
        .header a:hover {
            background-color: white;
            color: #007bff;
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
            border-color: #007bff;
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
        /* Retro preview style with CSS noise */
        .retro-card-preview {
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
            padding: 1rem;
            margin-top: 1rem;
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
            <h2 style="font-family: 'Debata', 'Rokiest', Arial, sans-serif;">${empty card ? 'Create New Card' : 'Edit Card'}</h2>
            
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

                <div class="retro-card-preview">
                <div style="color:#6b6b6b; font-size:0.9rem; margin-bottom:0.5rem;">Preview</div>
                <div><c:out value="${card.frontText}" default="(front text)"/></div>
                <div style="margin-top:0.5rem;"><c:out value="${card.backText}" default="(back text)"/></div>
            </div>
        </div>
    </div>
</body>
</html>
