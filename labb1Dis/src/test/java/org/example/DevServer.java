package org.example;

import org.apache.catalina.startup.Tomcat;
import java.nio.file.Files;
import java.nio.file.Path;

/** Local development launcher. Test scope keeps embedded Tomcat out of the deployed WAR. */
public final class DevServer {
    private DevServer() { }
    public static void main(String[] args) throws Exception {
        Path base = Path.of("target", "dev-tomcat").toAbsolutePath();
        Files.createDirectories(base);
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(base.toString());
        tomcat.setPort(Integer.getInteger("shop.port", 8080));
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        var context = tomcat.addWebapp("/webshop", Path.of("src/main/webapp").toAbsolutePath().toString());
        context.setParentClassLoader(DevServer.class.getClassLoader());
        String url = System.getenv("SHOP_DB_URL");
        context.addParameter("databaseUrl", url == null || url.isBlank()
                ? "jdbc:h2:file:" + Path.of("data", "webshop").toAbsolutePath().toString().replace('\\', '/') : url);
        // Development-only console in the same JVM, so it can share H2's file connection.
        // The connector and H2 console both restrict access to the local computer.
        var console = Tomcat.addServlet(context, "h2-console", new org.h2.server.web.WebServlet());
        console.addInitParameter("properties", Path.of("data").toAbsolutePath().toString());
        context.addServletMappingDecoded("/database/*", "h2-console");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { tomcat.stop(); tomcat.destroy(); } catch (Exception e) { e.printStackTrace(); }
        }));
        tomcat.start();
        System.out.println("Labbshoppen: http://localhost:" + tomcat.getConnector().getLocalPort() + "/webshop/");
        System.out.println("H2-konsol: http://localhost:" + tomcat.getConnector().getLocalPort() + "/webshop/database/");
        tomcat.getServer().await();
    }
}
