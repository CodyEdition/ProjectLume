<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Deck Form</title>
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
        .form-group textarea {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
            box-sizing: border-box;
        }
        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #ffb677;
        }
        .form-group textarea {
            height: 100px;
            resize: vertical;
        }
        .btn {
            padding: 0.75rem 1.5rem;
            background-color: #ffb677;
            color: #111111;
            border: 1px solid #ffb677;
            border-radius: 4px;
            text-decoration: none;
            display: inline-block;
            cursor: pointer;
            margin-right: 1rem;
        }
        .btn:hover {
            background-color: #ffa055;
            border-color: #ffa055;
        }
        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }
        .btn-secondary:hover {
            background-color: #545b62;
        }
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
    <jsp:include page="/WEB-INF/views/includes/header.jsp" />
    
    <div class="container">
        <div class="form-container">
            <h2>${empty deck ? 'Create New Deck' : 'Edit Deck'}</h2>
            
            <c:if test="${not empty error}">
                <div class="error">
                    ${error}
                </div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/deck${empty deck ? '' : '/edit/'}${deck.id}">
                <div class="form-group">
                    <label for="name">Deck Name:</label>
                    <input type="text" id="name" name="name" value="${deck.name}" required>
                </div>
                
                <div class="form-group">
                    <label for="description">Description:</label>
                    <textarea id="description" name="description">${deck.description}</textarea>
                </div>
                
                <div class="form-actions">
                    <button type="submit" class="btn">${empty deck ? 'Create Deck' : 'Update Deck'}</button>
                    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">Cancel</a>
                </div>
            </form>
            
            <div class="preview-section">
                <h3>Preview</h3>
                <div class="preview-card-wrapper">
                    <%
                        com.projectlume.model.Deck previewDeck = (com.projectlume.model.Deck) pageContext.getAttribute("deck");
                        String dateCode = "NOV10.25.10";
                        if (previewDeck != null && previewDeck.getCreatedAt() != null) {
                            java.time.LocalDateTime date = previewDeck.getCreatedAt();
                            String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", 
                                                  "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
                            String month = monthNames[date.getMonthValue() - 1];
                            String day = String.format("%02d", date.getDayOfMonth());
                            String yearShort = String.format("%02d", date.getYear() % 100);
                            dateCode = month + day + "." + yearShort + "." + day;
                        }
                        pageContext.setAttribute("previewDateCode", dateCode);
                        String serialNumber = dateCode + "S3";
                        pageContext.setAttribute("previewSerialNumber", serialNumber);
                    %>
                    <div class="collectible-card card-scanlines">
                        <div class="card-specs-bar">
                            <span>DECK | ID: ${not empty deck && deck.id != null ? deck.id : 'NEW'}</span>
                            <span class="card-badge">${previewDateCode}</span>
                        </div>
                        
                        <div class="card-artwork-panel">
                            <div class="card-holographic"></div>
                            
                            <div class="card-content">
                                <div>
                                    <div class="card-number">001</div>
                                    <div class="card-title" id="previewDeckName">
                                        <c:out value="${not empty deck ? deck.name : '(deck name)'}" default="(deck name)"/>
                                    </div>
                                    <div class="card-description" id="previewDeckDescription">
                                        <c:out value="${not empty deck && not empty deck.description ? deck.description : '(description)'}" default="(description)"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="card-serial">${previewSerialNumber}</div>
                    </div>
                </div>
            </div>
            
            <script>
                // Update preview in real-time as user types
                document.addEventListener('DOMContentLoaded', function() {
                    var nameInput = document.getElementById('name');
                    var descriptionInput = document.getElementById('description');
                    var previewDeckName = document.getElementById('previewDeckName');
                    var previewDeckDescription = document.getElementById('previewDeckDescription');
                    
                    function updatePreview() {
                        if (nameInput && previewDeckName) {
                            previewDeckName.textContent = nameInput.value || '(deck name)';
                        }
                        if (descriptionInput && previewDeckDescription) {
                            previewDeckDescription.textContent = descriptionInput.value || '(description)';
                        }
                    }
                    
                    // Initialize preview on page load
                    updatePreview();
                    
                    // Update preview on input changes
                    if (nameInput) {
                        nameInput.addEventListener('input', updatePreview);
                    }
                    if (descriptionInput) {
                        descriptionInput.addEventListener('input', updatePreview);
                    }
                });
            </script>
