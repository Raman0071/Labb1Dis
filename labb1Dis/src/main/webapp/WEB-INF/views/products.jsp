<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">Utvalt för studiepasset</p>
<h1>Små saker. Stora idéer.</h1>
<p>Välj dina favoriter och lägg dem i varukorgen.</p>
<div class="products">
    <c:forEach var="product" items="${products}">
        <article class="panel">
            <div class="product-mark" aria-hidden="true"><c:out value="${product.id}" /></div>
            <h2><c:out value="${product.name}" /></h2>
            <p><c:out value="${product.description}" /></p>
            <p class="price"><fmt:formatNumber value="${product.price}" minFractionDigits="2" maxFractionDigits="2" /> kr</p>
            <form method="post" action="${pageContext.request.contextPath}/shop/add">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="productId" value="${product.id}">
                <label for="quantity-${product.id}">Antal</label>
                <div class="actions">
                    <input id="quantity-${product.id}" name="quantity" type="number" min="1" max="99" value="1" required>
                    <button type="submit">Lägg i varukorg</button>
                </div>
            </form>
        </article>
    </c:forEach>
</div>
<c:if test="${empty products}"><p>Det finns inga produkter just nu.</p></c:if>
<%@ include file="footer.jspf" %>
