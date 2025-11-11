<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Profile</title>
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
            max-width: 800px;
            margin: 2rem auto;
            padding: 0 2rem;
        }
        .card {
            background: #fff;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .card h2 {
            margin: 0 0 1.5rem 0;
            color: #333;
            font-family: 'Debata', Arial, sans-serif;
        }
        .row {
            margin-bottom: 1.25rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid #eee;
        }
        .row:last-child {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0;
        }
        .label {
            color: #666;
            display: block;
            font-size: 0.9rem;
            margin-bottom: 0.25rem;
        }
        .value {
            color: #333;
            font-weight: bold;
            font-size: 1.1rem;
        }
        .btn {
            padding: 0.75rem 1.5rem;
            background-color: #ffb677;
            color: #111111;
            border: 1px solid #ffb677;
            border-radius: 4px;
            text-decoration: none;
            display: inline-block;
            margin-top: 1.5rem;
            cursor: pointer;
        }
        .btn:hover {
            background-color: #ffa055;
            border-color: #ffa055;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Project Lume</h1>
        <div class="user-info">
            <span>Welcome, ${user.firstName}!</span>
            <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
            <a href="${pageContext.request.contextPath}/auth/logout">Logout</a>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <h2>Your Profile</h2>
            <div class="row">
                <span class="label">Username</span>
                <span class="value">${user.username}</span>
            </div>
            <div class="row">
                <span class="label">Email</span>
                <span class="value">${user.email}</span>
            </div>
            <div class="row">
                <span class="label">First Name</span>
                <span class="value">${user.firstName}</span>
            </div>
            <div class="row">
                <span class="label">Last Name</span>
                <span class="value">${user.lastName}</span>
            </div>
            <a href="${pageContext.request.contextPath}/profile/edit" class="btn">Edit Profile</a>
        </div>
    </div>
</body>
</html>


