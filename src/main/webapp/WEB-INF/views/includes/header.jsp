<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="header">
    <h1>Project Lume</h1>
    <div class="user-info">
        <span>Welcome, ${user.firstName}!</span>
        <c:if test="${currentPage != 'dashboard'}">
            <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
        </c:if>
        <c:if test="${currentPage != 'profile'}">
            <a href="${pageContext.request.contextPath}/profile">Profile</a>
        </c:if>
        <c:if test="${currentPage != 'studyHistory'}">
            <a href="${pageContext.request.contextPath}/study/history">Study History</a>
        </c:if>
        <a href="${pageContext.request.contextPath}/auth/logout">Logout</a>
    </div>
</div>