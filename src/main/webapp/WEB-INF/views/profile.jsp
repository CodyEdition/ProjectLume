<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Project Lume - Profile</title>
    <style>
        /* Rokiest font family (OTF files) */
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Regular.otf') format('opentype'); font-weight: 400; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Medium.otf') format('opentype'); font-weight: 500; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Semibold.otf') format('opentype'); font-weight: 600; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Bold.otf') format('opentype'); font-weight: 700; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Extrabold.otf') format('opentype'); font-weight: 800; font-style: normal; font-display: swap; }
        @font-face { font-family: 'Rokiest'; src: url('${pageContext.request.contextPath}/assets/fonts/Rokiest-Black.otf') format('opentype'); font-weight: 900; font-style: normal; font-display: swap; }
        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 0; }
        .header { background: linear-gradient(90deg, #2f2f2f 0%, #6b4a2f 40%, #ff7a00 80%, #ffa24d 100%); color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }
        .container { max-width: 800px; margin: 2rem auto; padding: 0 2rem; }
        .card { background: #fff; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .row { margin-bottom: 0.75rem; }
        .label { color: #666; display: block; font-size: 0.9rem; }
        .value { color: #333; font-weight: bold; font-size: 1.05rem; }
        a { color: #ff8a1a; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
    </head>
<body>
    <div class="header">
        <h1 style="font-family: 'Debata', 'Rokiest', Arial, sans-serif;">Project Lume</h1>
        <div>
            <a href="${pageContext.request.contextPath}/dashboard" style="color: white; margin-right: 1rem; text-shadow: 0 0 4px rgba(255,255,255,0.45);">Dashboard</a>
            <a href="${pageContext.request.contextPath}/auth/logout" style="color: white; text-shadow: 0 0 4px rgba(255,255,255,0.45);">Logout</a>
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
        </div>
    </div>
</body>
</html>


