# Labbshoppen

En webbshop för laborationen i distribuerade informationssystem. Byggd med Java, Servlets, JSP/JSTL och JDBC, utan applikationsramverk.

Användaren kan logga in, se produkter, lägga produkter i en varukorg, visa antal och totalpris samt ta bort produkter och logga ut.

## Starta projektet

Krav: JDK 17 eller senare och Maven 3.9 eller senare. Kör kommandona från projektroten.

```sh
mvn test-compile exec:java
```

Öppna **http://localhost:8080/webshop/**. Logga in med **student** och lösenordet **labb123**. Avsluta servern med Ctrl+C.

I IntelliJ kan du också importera projektet som ett Maven-projekt och köra `main` i `src/test/java/org/example/DevServer.java`. Använd projektroten som arbetskatalog. Utvecklingsstartaren kör Tomcat 9 automatiskt.

## Arkitektur

- **Presentation:** `ShopServlet` och JSP-sidor tar emot HTTP-anrop och visar gränssnittet.
- **Affärslogik:** `ShopService`, `Passwords`, `Cart` och `CartItem` hanterar inloggning, varukorgens regler och beräkningar.
- **Datalager:** `Database`, `ProductDao` och `UserDao` hanterar SQL och JDBC.

`ShopService` omvandlar modeller till `UserDTO`, `ProductDTO`, `CartDTO` och `CartItemDTO` som presentationen läser. Varje session har en egen varukorg. Belopp beräknas med `BigDecimal`.

## Databas

H2 startar tillsammans med applikationen. Tabellerna `users` och `products`, ett demokonto och tre produkter skapas automatiskt vid första start. Databasen sparas i `data/webshop.mv.db`. Databasfilen behöver inte laddas upp till GitHub. Varukorgen lagras i sessionen.

Vid lokal körning finns H2-konsolen på **http://localhost:8080/webshop/database/**. Anslut med:

- Drivrutin: `org.h2.Driver`
- JDBC URL: `jdbc:h2:file:./data/webshop`
- Användarnamn: `sa`
- Lösenord: lämna tomt

## Tester och bygge

```sh
mvn test
mvn package
```

Testerna kontrollerar affärslogik och JDBC samt webbflödet med riktig Tomcat och JSP. `mvn package` kör testerna och skapar `target/webshop.war`, som kan installeras i en separat Tomcat 9.
