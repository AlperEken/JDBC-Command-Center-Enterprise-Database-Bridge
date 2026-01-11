import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    // Statiska variabler som fylls i av loadConfig()
    private static String url;
    private static String user;
    private static String password;

    public static void main(String[] args) {
        // 1. Försök ladda inställningarna från db.properties
        if (!loadConfig()) {
            System.err.println("Programmet avslutas då konfigurationsfilen saknas eller är felaktig.");
            return;
        }

        // 2. Anslut till databasen med variablerna från filen
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Ansluten till databasen!");
            Scanner scanner = new Scanner(System.in);

            String userRole = authenticateUser(scanner);

            if (userRole.equals("CUSTOMER")) {
                runCustomerMenu(conn, scanner);
            } else if (userRole.equals("ADMIN")) {
                runAdminMenu(conn, scanner);
            }

        } catch (SQLException e) {
            System.err.println("Fel vid anslutning till databasen: " + e.getMessage());
        }
    }

    // --- Befintliga menyer och logik ---

    private static String authenticateUser(Scanner scanner) {
        while (true) {
            System.out.println("\nVälkommen! Är du en:");
            System.out.println("1. Kund");
            System.out.println("2. Administratör");
            System.out.print("Välj (1 eller 2): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": return "CUSTOMER";
                case "2": return "ADMIN";
                default: System.out.println("Ogiltigt val. Försök igen.");
            }
        }
    }

    private static void runCustomerMenu(Connection conn, Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\nKundmeny:");
            System.out.println("1. Skapa en order");
            System.out.println("2. Lägg till en ny kund");
            System.out.println("3. Avsluta");
            System.out.print("Välj ett alternativ: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: createOrder(conn, scanner); break;
                case 2: addCustomer(conn, scanner); break;
                case 3: System.out.println("Tack för besöket!"); return;
                default: System.out.println("Ogiltigt val, försök igen.");
            }
        }
    }

    private static void runAdminMenu(Connection conn, Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\nAdministratörsmeny:");
            System.out.println("1. Visa alla leverantörer");
            System.out.println("2. Lägg till en ny leverantör");
            System.out.println("3. Visa alla produkter");
            System.out.println("4. Lägg till en ny produkt");
            System.out.println("5. Skapa en order");
            System.out.println("6. Visa alla ordrar");
            System.out.println("7. Lägg till en ny kund");
            System.out.println("8. Skapa ny rabattkod");
            System.out.println("9. Visa rabattkodshistorik");
            System.out.println("10. Bekräfta order");
            System.out.println("11. Avsluta");
            System.out.print("Välj ett alternativ: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: showSuppliers(conn); break;
                case 2: addSupplier(conn, scanner); break;
                case 3: showProducts(conn); break;
                case 4: addProduct(conn, scanner); break;
                case 5: createOrder(conn, scanner); break;
                case 6: showOrders(conn); break;
                case 7: addCustomer(conn, scanner); break;
                case 8: createDiscount(conn); break;
                case 9: showDiscountHistory(conn); break;
                case 10: confirmOrder(conn, scanner); break;
                case 11: System.out.println("Avslutar administratörsläget..."); return;
                default: System.out.println("Ogiltigt val, försök igen.");
            }
        }
    }

    // --- Databasoperationer ---

    private static void showDiscountHistory(Connection conn) throws SQLException {
        String query = "SELECT d.reason, d.percentage, d.startDate, d.endDate, d.code AS discountName FROM Discount d";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nRabattkodshistorik:");
            while (rs.next()) {
                System.out.println("Rabattnamn: " + rs.getString("discountName") +
                        ", Anledning: " + rs.getString("reason") +
                        ", Procent: " + rs.getInt("percentage") + "%" +
                        ", Startdatum: " + rs.getString("startDate") +
                        ", Slutdatum: " + rs.getString("endDate"));
            }
        } catch (SQLException e) {
            System.err.println("Fel vid hämtning av rabattkodshistorik: " + e.getMessage());
        }
    }

    private static void showSuppliers(Connection conn) throws SQLException {
        String query = "SELECT * FROM Suppliers";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nLeverantörer:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Namn: " + rs.getString("name"));
            }
        }
    }

    private static void addSupplier(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Ange leverantörsID: ");
        int id = scanner.nextInt(); scanner.nextLine();
        System.out.print("Ange namn: ");
        String name = scanner.nextLine();
        System.out.print("Ange tel: ");
        int tel = scanner.nextInt(); scanner.nextLine();
        System.out.print("Ange adress: ");
        String addr = scanner.nextLine();

        String query = "INSERT INTO Suppliers (id, name, telNr, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id); pstmt.setString(2, name);
            pstmt.setInt(3, tel); pstmt.setString(4, addr);
            pstmt.executeUpdate();
            System.out.println("Leverantör tillagd!");
        }
    }

    private static void addProduct(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Ange produktID: ");
        int code = scanner.nextInt(); scanner.nextLine();
        System.out.print("Ange namn: ");
        String name = scanner.nextLine();
        System.out.print("Ange antal: ");
        int qty = scanner.nextInt();
        System.out.print("Ange pris: ");
        double price = scanner.nextDouble();
        System.out.print("Ange leverantörs-ID: ");
        int sId = scanner.nextInt(); scanner.nextLine();

        String query = "INSERT INTO Product (code, name, quantity, price, supplierId) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code); pstmt.setString(2, name);
            pstmt.setInt(3, qty); pstmt.setDouble(4, price);
            pstmt.setInt(5, sId);
            pstmt.executeUpdate();
            System.out.println("Produkt tillagd!");
        }
    }

    private static boolean updateProductQuantity(Connection conn, int productCode, int orderedQuantity) throws SQLException {
        String checkQuery = "SELECT quantity FROM Product WHERE code = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setInt(1, productCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt("quantity") >= orderedQuantity) {
                    String updateQuery = "UPDATE Product SET quantity = quantity - ? WHERE code = ?";
                    try (PreparedStatement uPstmt = conn.prepareStatement(updateQuery)) {
                        uPstmt.setInt(1, orderedQuantity);
                        uPstmt.setInt(2, productCode);
                        return uPstmt.executeUpdate() > 0;
                    }
                }
            }
        }
        return false;
    }

    private static void createOrder(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Ange orderID: ");
        int orderId = scanner.nextInt(); scanner.nextLine();
        if (orderIdExists(conn, orderId)) return;

        System.out.print("Ange kund-ID: ");
        int cId = scanner.nextInt(); scanner.nextLine();
        if (!customerExists(conn, cId)) return;

        System.out.print("Datum (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        conn.setAutoCommit(false);
        try {
            String q = "INSERT INTO Orders (orderId, customerId, orderDate, isConfirmed) VALUES (?, ?, ?, ?)";
            try (PreparedStatement p = conn.prepareStatement(q)) {
                p.setInt(1, orderId); p.setInt(2, cId);
                p.setDate(3, Date.valueOf(date)); p.setBoolean(4, false);
                p.executeUpdate();
            }
            // Logik för att lägga till produkter...
            conn.commit();
            System.out.println("Order skapad!");
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void addOrderItem(Connection conn, int orderId, int pCode, int qty, String dCode, double fPrice) throws SQLException {
        String q = "INSERT INTO OrderItems (orderItemId, orderId, productCode, quantity, discountCode, finalPrice) VALUES ((SELECT COALESCE(MAX(orderItemId),0)+1 FROM OrderItems), ?, ?, ?, ?, ?)";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, orderId); p.setInt(2, pCode);
            p.setInt(3, qty); p.setString(4, dCode);
            p.setDouble(5, fPrice);
            p.executeUpdate();
        }
    }

    private static void addCustomer(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("ID: "); int id = scanner.nextInt(); scanner.nextLine();
        System.out.print("Förnamn: "); String fn = scanner.nextLine();
        System.out.print("Efternamn: "); String ln = scanner.nextLine();
        System.out.print("Email: "); String em = scanner.nextLine();

        String q = "INSERT INTO Customer (customerId, firstName, lastName, email, isAdmin) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, id); p.setString(2, fn); p.setString(3, ln);
            p.setString(4, em); p.setBoolean(5, false);
            p.executeUpdate();
            System.out.println("Kund tillagd!");
        }
    }

    private static boolean customerExists(Connection conn, int id) throws SQLException {
        String q = "SELECT 1 FROM Customer WHERE customerId = ?";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) { return rs.next(); }
        }
    }

    private static void showProducts(Connection conn) throws SQLException {
        String q = "SELECT * FROM Product";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(q)) {
            while (rs.next()) {
                System.out.println("Kod: " + rs.getInt("code") + ", Namn: " + rs.getString("name") + ", Pris: " + rs.getDouble("price"));
            }
        }
    }

    private static void showOrders(Connection conn) throws SQLException {
        String q = "SELECT * FROM Orders";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(q)) {
            while (rs.next()) {
                System.out.println("Order: " + rs.getInt("orderId") + ", Datum: " + rs.getDate("orderDate"));
            }
        }
    }

    private static boolean orderIdExists(Connection conn, int id) throws SQLException {
        String q = "SELECT 1 FROM Orders WHERE orderId = ?";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) { return rs.next(); }
        }
    }

    public static void createDiscount(Connection conn) { /* Logik för rabatt... */ }

    public static boolean productExists(Connection conn, long code) throws SQLException {
        String q = "SELECT 1 FROM Product WHERE code = ?";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setLong(1, code);
            try (ResultSet rs = p.executeQuery()) { return rs.next(); }
        }
    }

    private static double calculateFinalPrice(Connection conn, int code, int qty, double disc) throws SQLException {
        String q = "SELECT price FROM Product WHERE code = ?";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, code);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return (rs.getDouble("price") * qty) * (1 - disc/100);
            }
        }
        return 0;
    }

    private static void confirmOrder(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("ID att bekräfta: ");
        int id = scanner.nextInt(); scanner.nextLine();
        String q = "UPDATE Orders SET isConfirmed = true WHERE orderId = ?";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, id);
            if (p.executeUpdate() > 0) System.out.println("Order bekräftad!");
        }
    }

    // --- Hjälpmetod för konfiguration ---

    private static boolean loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            // Kontrollera att ingen data saknas
            return url != null && user != null && password != null;
        } catch (IOException e) {
            System.err.println("Kunde inte hitta eller läsa db.properties: " + e.getMessage());
            return false;
        }
    }
}