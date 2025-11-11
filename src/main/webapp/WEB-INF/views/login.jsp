<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Login</title>
    <style>
        /* Debata display font (for titles/banners/headers) */
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Debata'; src: url('${pageContext.request.contextPath}/assets/fonts/Debata-Italic.otf') format('opentype'); font-weight: 400; font-style: italic; font-display: swap; }
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .container {
            background-color: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 400px;
        }
        .header {
            text-align: center;
            margin-bottom: 2rem;
            background: linear-gradient(90deg, #2f2f2f 0%, #6b4a2f 40%, #ff7a00 80%, #ffa24d 100%);
            color: #fff;
            padding: 1.25rem 1rem;
            border-radius: 8px;
        }
        .header h1 {
            color: #fff;
            margin: 0;
            text-shadow: 0 0 6px rgba(255,255,255,0.55);
            font-family: 'Debata', Arial, sans-serif;
        }
        .header p {
            color: #f1f1f1;
            margin: 0.5rem 0 0 0;
            text-shadow: 0 0 4px rgba(255,255,255,0.45);
            font-family: 'Debata', Arial, sans-serif;
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
        .form-group input {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
            box-sizing: border-box;
        }
        .form-group input:focus {
            outline: none;
            border-color: #ffb677;
        }
        .btn {
            width: 100%;
            padding: 0.75rem;
            background-color: #ffb677; /* pastel orange */
            color: #111111; /* black text for readability */
            border: 1px solid #ffb677;
            border-radius: 4px;
            font-size: 1rem;
            cursor: pointer;
            margin-bottom: 1rem;
            text-align: center;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .btn:hover { background-color: #ffa055; border-color: #ffa055; }
        .btn-secondary { background-color: #bfbfbf; color: #ffffff; border-color: #bfbfbf; }
        .btn-secondary:hover { background-color: #a9a9a9; border-color: #a9a9a9; }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            padding: 0.75rem;
            border-radius: 4px;
            margin-bottom: 1rem;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            padding: 0.75rem;
            border-radius: 4px;
            margin-bottom: 1rem;
        }
        .links {
            text-align: center;
            margin-top: 1rem;
        }
        .links a {
            color: #ff7a00;
            text-decoration: none;
        }
        .links a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Project Lume</h1>
            <p>Flashcard Study Application</p>
        </div>
        
        <c:if test="${not empty error}">
            <div class="error">
                ${error}
            </div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="success">
                ${success}
            </div>
        </c:if>
        
        <form method="post" action="${pageContext.request.contextPath}/auth">
            <div class="form-group">
                <label for="username">Username or Email:</label>
                <input type="text" id="username" name="username" value="${param.username}" required>
            </div>
            
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>
            </div>
            
            <button type="submit" class="btn">Login</button>
        </form>
        
        <div class="links">
            <p>Don't have an account? <a href="${pageContext.request.contextPath}/auth/register">Register here</a></p>
        </div>
    </div>
</body>
</html>
