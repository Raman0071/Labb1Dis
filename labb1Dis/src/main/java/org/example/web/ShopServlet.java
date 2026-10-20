package org.example.web;

import org.example.dao.Database;
import org.example.dao.ProductDao;
import org.example.dao.UserDao;
import org.example.model.Cart;
import org.example.service.ShopService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.UUID;

/** HTTP controller. Shared fields contain services only; identity and cart belong to sessions. */
public class ShopServlet extends HttpServlet {
    private ShopService service;

    @Override public void init() throws ServletException {
        String url = getServletContext().getInitParameter("databaseUrl");
        if (url == null || url.isBlank()) { url = System.getenv("SHOP_DB_URL"); }
        if (url == null || url.isBlank()) {
            String base = System.getProperty("catalina.base", System.getProperty("user.home"));
            url = "jdbc:h2:file:" + Path.of(base, "data", "webshop").toAbsolutePath().toString().replace('\\', '/');
        }
        try {
            Database database = new Database(url);
            database.initialize();
            service = new ShopService(new ProductDao(database), new UserDao(database));
        } catch (SQLException | RuntimeException e) { throw new ServletException("Databasen kunde inte startas.", e); }
    }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        prepare(request, response);
        String route = route(request);
        if (!route.equals("/login") && !route.equals("/products") && !route.equals("/cart")) {
            response.sendError(404); return;
        }
        HttpSession session = session(request);
        if (route.equals("/login")) {
            if (session.getAttribute("user") != null) { redirect(request, response, "/products"); }
            else { view(request, response, "login"); }
            return;
        }
        if (!authenticated(request, response)) { return; }
        try {
            if (route.equals("/products")) {
                request.setAttribute("products", service.listProducts());
                view(request, response, "products");
            } else {
                view(request, response, "cart");
            }
        } catch (SQLException e) { databaseError(request, response, e); }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        prepare(request, response);
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("csrf") == null
                || !session.getAttribute("csrf").equals(request.getParameter("csrf"))) {
            response.sendError(403, "Formuläret har gått ut. Ladda om sidan."); return;
        }
        String route = route(request);
        try {
            if (route.equals("/login")) {
                var user = service.login(request.getParameter("username"), request.getParameter("password"));
                if (user.isEmpty()) {
                    response.setStatus(401);
                    request.setAttribute("error", "Fel användarnamn eller lösenord.");
                    view(request, response, "login");
                } else {
                    // A new session prevents session fixation and discards any previous user's cart.
                    session.invalidate();
                    HttpSession fresh = session(request);
                    fresh.setAttribute("user", user.get());
                    redirect(request, response, "/products");
                }
                return;
            }
            if (!authenticated(request, response)) { return; }
            if (route.equals("/logout")) {
                session.invalidate();
                redirect(request, response, "/login");
            } else if (route.equals("/add") || route.equals("/remove")) {
                Cart cart = (Cart) session.getAttribute("cart");
                try {
                    long id = Long.parseLong(request.getParameter("productId"));
                    if (route.equals("/add")) {
                        service.addToCart(cart, id, Integer.parseInt(request.getParameter("quantity")));
                        session.setAttribute("message", "Produkten lades i varukorgen.");
                    } else { service.removeFromCart(cart, id); }
                } catch (NumberFormatException e) { session.setAttribute("error", "Ange ett giltigt produkt-id och antal (1–99)."); }
                catch (IllegalArgumentException e) { session.setAttribute("error", e.getMessage()); }
                redirect(request, response, "/cart");
            } else { response.sendError(404); }
        } catch (SQLException e) { databaseError(request, response, e); }
    }

    private HttpSession session(HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (session) {
            if (session.getAttribute("csrf") == null) { session.setAttribute("csrf", UUID.randomUUID().toString()); }
            if (session.getAttribute("cart") == null) { session.setAttribute("cart", new Cart()); }
        }
        return session;
    }
    private boolean authenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) { redirect(request, response, "/login"); return false; }
        return true;
    }
    private String route(HttpServletRequest request) { return request.getPathInfo() == null ? "/products" : request.getPathInfo(); }
    private void prepare(HttpServletRequest request, HttpServletResponse response) throws java.io.UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", "default-src 'self'; style-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'self'");
    }
    private void redirect(HttpServletRequest request, HttpServletResponse response, String route) throws IOException {
        response.setStatus(303);
        response.setHeader("Location", request.getContextPath() + "/shop" + route);
    }
    private void view(HttpServletRequest request, HttpServletResponse response, String name) throws ServletException, IOException {
        HttpSession session = request.getSession();
        // All rendered cart data comes from a DTO; the session's Cart is used only for commands.
        request.setAttribute("cartView", service.getCart((Cart) session.getAttribute("cart")));
        synchronized (session) {
            for (String key : new String[]{"error", "message"}) {
                if (session.getAttribute(key) != null) {
                    request.setAttribute(key, session.getAttribute(key));
                    session.removeAttribute(key);
                }
            }
        }
        request.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(request, response);
    }
    private void databaseError(HttpServletRequest request, HttpServletResponse response, SQLException e) throws ServletException, IOException {
        getServletContext().log("Database operation failed", e);
        response.setStatus(500);
        view(request, response, "error");
    }
}
