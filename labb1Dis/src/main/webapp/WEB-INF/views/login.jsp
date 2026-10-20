<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<section class="login panel">
    <p class="eyebrow">Välkommen till Labbshoppen</p>
    <h1>Logga in</h1>
    <p>Här hittar du det lilla extra till nästa studiepass.</p>
    <form method="post" action="${pageContext.request.contextPath}/shop/login">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <label for="username">Användarnamn</label>
        <input id="username" name="username" autocomplete="username" maxlength="80" required autofocus>
        <label for="password">Lösenord</label>
        <input id="password" name="password" type="password" autocomplete="current-password" maxlength="256" required>
        <button type="submit">Logga in</button>
    </form>
    <p class="hint">Demokonto: <strong>student</strong> / <strong>labb123</strong></p>
</section>
<%@ include file="footer.jspf" %>
