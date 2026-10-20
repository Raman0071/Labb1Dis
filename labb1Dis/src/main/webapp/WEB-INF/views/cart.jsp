<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">Dina val</p>
<h1>Varukorg</h1>
<c:choose>
    <c:when test="${empty cartView.items}">
        <section class="panel"><h2>Din varukorg är tom</h2><p>Hitta något till nästa studiepass.</p>
            <a class="button" href="${pageContext.request.contextPath}/shop/products">Se produkterna</a>
        </section>
    </c:when>
    <c:otherwise>
        <div class="panel table-wrap">
            <table>
                <caption>Produkter i din varukorg</caption>
                <thead><tr><th scope="col">Produkt</th><th scope="col">Pris</th><th scope="col">Antal</th><th scope="col">Summa</th><th scope="col">Åtgärd</th></tr></thead>
                <tbody>
                <c:forEach var="item" items="${cartView.items}">
                    <tr>
                        <td><c:out value="${item.product.name}" /></td>
                        <td><fmt:formatNumber value="${item.product.price}" minFractionDigits="2" maxFractionDigits="2" /> kr</td>
                        <td><c:out value="${item.quantity}" /></td>
                        <td><fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2" /> kr</td>
                        <td><form method="post" action="${pageContext.request.contextPath}/shop/remove">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="productId" value="${item.product.id}">
                            <button class="secondary" type="submit">Ta bort</button>
                        </form></td>
                    </tr>
                </c:forEach>
                </tbody>
                <tfoot><tr><th colspan="3" scope="row">Totalt</th><td colspan="2" class="price"><fmt:formatNumber value="${cartView.total}" minFractionDigits="2" maxFractionDigits="2" /> kr</td></tr></tfoot>
            </table>
        </div>
        <p><a href="${pageContext.request.contextPath}/shop/products">Fortsätt handla →</a></p>
    </c:otherwise>
</c:choose>
<%@ include file="footer.jspf" %>
