<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; }
        .container { max-width: 700px; margin: 10vh auto; background: #fff; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { margin: 0 0 0.5rem 0; color: #333; }
        p { color: #666; }
        .code { display: inline-block; background: #eee; padding: 0.25rem 0.5rem; border-radius: 4px; margin-left: 0.5rem; }
        a { color: #ff7a00; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
    </head>
<body>
    <div class="container">
        <h1>Something went wrong <span class="code">${pageContext.errorData.statusCode}</span></h1>
        <p><c:out value="${error}" default="Please try again or return to the dashboard."/></p>
        <p><a href="${pageContext.request.contextPath}/dashboard">Go to Dashboard</a></p>
    </div>
</body>
</html>


