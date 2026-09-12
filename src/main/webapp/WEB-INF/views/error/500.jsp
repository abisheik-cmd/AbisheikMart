<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="display: flex; justify-content: center; align-items: center; min-height: 70vh;">
    <div style="text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border); max-width: 500px; width: 100%;">
        <div style="font-size: 4rem; margin-bottom: 0.8rem;">⚠️</div>
        <h1 style="color: #fff; margin-bottom: 0.5rem;">500 - Server Error</h1>
        <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">An unexpected internal error occurred. Our team has been notified.</p>
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary" style="text-decoration: none;">Return to Marketplace</a>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

