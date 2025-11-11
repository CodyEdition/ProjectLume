<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Study</title>
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
        .header a {
            color: white;
            text-decoration: none;
            padding: 0.5rem 1rem;
            border: 1px solid white;
            border-radius: 4px;
        }
        .study-container {
            max-width: 600px;
            margin: 3rem auto;
            padding: 0 2rem;
        }
        .study-card-wrapper {
            position: relative;
            perspective: 1000px;
            margin-bottom: 2rem;
        }
        .study-card {
            width: 100%;
            max-width: 500px;
            margin: 0 auto;
            transition: transform 0.6s;
            transform-style: preserve-3d;
        }
        .study-card.flipped {
            transform: rotateY(180deg);
        }
        .study-card-front,
        .study-card-back {
            backface-visibility: hidden;
            -webkit-backface-visibility: hidden;
        }
        .study-card-back {
            transform: rotateY(180deg);
        }
        .progress-bar {
            background-color: #333;
            height: 8px;
            border-radius: 4px;
            margin-bottom: 2rem;
            overflow: hidden;
        }
        .progress-fill {
            background: linear-gradient(90deg, #ff7a00, #ffa24d);
            height: 100%;
            transition: width 0.3s ease;
        }
        .answer-buttons {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-top: 2rem;
        }
        .answer-btn {
            padding: 1rem 2rem;
            font-size: 1rem;
            border: 2px solid white;
            border-radius: 4px;
            cursor: pointer;
            font-family: 'Courier New', monospace;
            transition: all 0.2s ease;
        }
        .answer-btn.correct {
            background-color: #00ff00;
            color: #000;
        }
        .answer-btn.incorrect {
            background-color: #ff0000;
            color: #fff;
        }
        .answer-btn:hover {
            transform: scale(1.05);
        }
        .flip-btn {
            background-color: #ffb677;
            color: #111;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 4px;
            cursor: pointer;
            font-size: 1rem;
            margin-top: 1rem;
        }
        .flip-btn:hover {
            background-color: #ffa055;
        }
        .study-info {
            color: white;
            text-align: center;
            margin-bottom: 1rem;
            font-family: 'Courier New', monospace;
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
    
    <div class="study-container">
        <c:if test="${not empty error}">
            <div style="background-color: #f8d7da; color: #721c24; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem;">
                ${error}
            </div>
        </c:if>
        
        <c:if test="${not empty card}">
            <div class="study-info">
                Card ${cardIndex + 1} of ${totalCards}
            </div>
            
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${((cardIndex + 1) / totalCards) * 100}%"></div>
            </div>
            
            <div class="study-card-wrapper">
                <div class="study-card" id="studyCard">
                    <div class="study-card-front">
                        <div class="collectible-card card-scanlines">
                            <div class="card-specs-bar">
                                <span>STUDY | CARD ${cardIndex + 1}</span>
                                <span class="card-badge">${deck.name}</span>
                            </div>
                            
                            <div class="card-artwork-panel">
                                <div class="card-holographic"></div>
                                
                                <div class="card-content">
                                    <div style="text-align: center; padding: 2rem 0;">
                                        <div class="card-number">${cardIndex + 1 < 10 ? '00' : (cardIndex + 1 < 100 ? '0' : '')}${cardIndex + 1}</div>
                                        <div class="card-title" style="margin-top: 1rem;">${card.frontText}</div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="card-serial">STUDY MODE</div>
                        </div>
                        
                        <div style="text-align: center; margin-top: 1rem;">
                            <button class="flip-btn" onclick="flipCard()">Flip Card</button>
                        </div>
                    </div>
                    
                    <div class="study-card-back">
                        <div class="collectible-card card-scanlines">
                            <div class="card-specs-bar">
                                <span>ANSWER | CARD ${cardIndex + 1}</span>
                                <span class="card-badge">${deck.name}</span>
                            </div>
                            
                            <div class="card-artwork-panel">
                                <div class="card-holographic"></div>
                                
                                <div class="card-content">
                                    <div style="text-align: center; padding: 2rem 0;">
                                        <div class="card-number">${cardIndex + 1 < 10 ? '00' : (cardIndex + 1 < 100 ? '0' : '')}${cardIndex + 1}</div>
                                        <div class="card-title" style="margin-top: 1rem;">${card.frontText}</div>
                                        <div class="card-description" style="margin-top: 2rem; padding-top: 1rem; border-top: 2px solid #ddd;">
                                            <strong>Answer:</strong><br/>
                                            ${card.backText}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="card-serial">STUDY MODE</div>
                        </div>
                        
                        <form method="POST" action="${pageContext.request.contextPath}/study/answer" style="text-align: center; margin-top: 1rem;">
                            <div class="answer-buttons">
                                <button type="submit" name="wasCorrect" value="true" class="answer-btn correct">Correct</button>
                                <button type="submit" name="wasCorrect" value="false" class="answer-btn incorrect">Incorrect</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </c:if>
    </div>
    
    <script>
        function flipCard() {
            document.getElementById('studyCard').classList.add('flipped');
        }
    </script>
</body>
</html>

