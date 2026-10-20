package org.example;

import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

/** Real HTTP tests: Tomcat compiles and executes the JSPs against an isolated H2 database. */
class WebShopTest {
    private static Tomcat tomcat;
    private static String base;
    private static String databaseUrl;
    @BeforeAll static void startServer() throws Exception {
        Path directory = Path.of("target", "test-tomcat").toAbsolutePath();
        Files.createDirectories(directory);
        tomcat = new Tomcat();
        tomcat.setBaseDir(directory.toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        var context = tomcat.addWebapp("/webshop", Path.of("src/main/webapp").toAbsolutePath().toString());
        context.setParentClassLoader(WebShopTest.class.getClassLoader());
        databaseUrl = "jdbc:h2:mem:http-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        context.addParameter("databaseUrl", databaseUrl);
        tomcat.start();
        base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/webshop";
    }
    @AfterAll static void stopServer() throws Exception {
        if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
    }
    private HttpClient client() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.NEVER).build();
    }
    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(HttpClient client, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String token(String html) {
        var matcher = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);
        assertTrue(matcher.find(), html);
        return matcher.group(1);
    }
    private String login(HttpClient client) throws Exception {
        var page = get(client, "/shop/login");
        assertEquals(200, page.statusCode());
        var response = post(client, "/shop/login", "csrf=" + token(page.body()) + "&username=student&password=labb123");
        assertEquals(303, response.statusCode(), response.body());
        var products = get(client, "/shop/products");
        assertEquals(200, products.statusCode(), products.body());
        assertTrue(products.body().contains("Anteckningsbok"));
        return token(products.body());
    }
    @Test void fullShoppingFlowAndSessionIsolation() throws Exception {
        var first = client();
        var second = client();
        assertEquals(303, get(first, "/shop/cart").statusCode());
        String csrf = login(first);
        assertEquals(303, post(first, "/shop/add", "csrf=" + csrf + "&productId=3&quantity=2").statusCode());
        var cart = get(first, "/shop/cart");
        assertEquals(200, cart.statusCode(), cart.body());
        assertTrue(cart.body().contains("Penna"));
        assertTrue(cart.body().contains("39,00"));
        login(second);
        assertTrue(get(second, "/shop/cart").body().contains("Din varukorg är tom"));
        assertEquals(303, post(first, "/shop/remove", "csrf=" + csrf + "&productId=3").statusCode());
        assertTrue(get(first, "/shop/cart").body().contains("Din varukorg är tom"));
        assertEquals(303, post(first, "/shop/logout", "csrf=" + csrf).statusCode());
        assertEquals(303, get(first, "/shop/products").statusCode());
        login(first);
        assertTrue(get(first, "/shop/cart").body().contains("Din varukorg är tom"));
    }
    @Test void rejectsForgedPostsAndInvalidLogin() throws Exception {
        var client = client();
        String oldCsrf = token(get(client, "/shop/login").body());
        assertEquals(401, post(client, "/shop/login", "csrf=" + oldCsrf + "&username=student&password=wrong").statusCode());
        String csrf = login(client);
        assertEquals(403, post(client, "/shop/add", "productId=1&quantity=1").statusCode());
        assertEquals(403, post(client, "/shop/add", "csrf=" + oldCsrf + "&productId=1&quantity=1").statusCode());
        assertEquals(303, post(client, "/shop/add", "csrf=" + csrf + "&productId=1&quantity=-1").statusCode());
        var cart = get(client, "/shop/cart");
        assertTrue(cart.body().contains("1–99"));
        assertTrue(cart.body().contains("Din varukorg är tom"));
        assertEquals(404, get(client, "/WEB-INF/views/products.jsp").statusCode());
        assertEquals(404, get(client, "/shop/unknown").statusCode());
        assertEquals(303, post(client, "/shop/add", "csrf=" + csrf + "&productId=999&quantity=1").statusCode());
        assertTrue(get(client, "/shop/cart").body().contains("Produkten finns inte."));
    }
    @Test void escapesDatabaseTextAndGetCannotChangeCart() throws Exception {
        var client = client();
        login(client);
        assertEquals(404, get(client, "/shop/add?productId=1&quantity=1").statusCode());
        assertTrue(get(client, "/shop/cart").body().contains("Din varukorg är tom"));
        var database = new org.example.dao.Database(databaseUrl);
        try (var connection = database.connect(); var query = connection.prepareStatement("UPDATE products SET name = ? WHERE id = 1")) {
            query.setString(1, "<script>alert('x')</script>");
            query.executeUpdate();
            try {
                var page = get(client, "/shop/products");
                assertEquals(200, page.statusCode());
                assertTrue(page.body().contains("&lt;script&gt;"));
                assertFalse(page.body().contains("<script>"));
            } finally {
                query.setString(1, "Anteckningsbok");
                query.executeUpdate();
            }
        }
    }
}
