package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import exception.InvalidRouteException;
import exception.PassValidityExpiredException;
import model.BusPass;
import model.BusRoute;
import model.Passenger;
import service.BusPassService;
import service.PassengerService;
import service.RouteService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedded Localhost Web Server (built using standard Java HttpServer).
 * Exposes the Bus Pass Management System at http://localhost:8080
 */
public class WebServer {
    private final PassengerService passengerService;
    private final RouteService routeService;
    private final BusPassService passService;
    private HttpServer server;

    public WebServer(PassengerService passengerService, RouteService routeService, BusPassService passService) {
        this.passengerService = passengerService;
        this.routeService = routeService;
        this.passService = passService;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new DashboardHandler());
        server.createContext("/api/register-passenger", new RegisterPassengerHandler());
        server.createContext("/api/add-route", new AddRouteHandler());
        server.createContext("/api/issue-pass", new IssuePassHandler());
        server.createContext("/api/renew-pass", new RenewPassHandler());
        server.createContext("/api/cancel-pass", new CancelPassHandler());
        server.setExecutor(null); // default executor
        server.start();
        System.out.println(">>> Localhost Web Server running at: http://localhost:" + port + " <<<");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String html = buildDashboardHtml();
                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String err = "Server Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, err.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(err.getBytes());
                }
            }
        }
    }

    private class RegisterPassengerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String id = params.get("id");
                    String name = params.get("name");
                    String phone = params.get("phone");
                    String email = params.get("email");
                    String type = params.get("type");
                    int validity = Integer.parseInt(params.getOrDefault("validity", "180"));

                    passengerService.registerPassenger(id, name, phone, email, type, validity);
                    redirect(exchange, "/?msg=Passenger+" + id + "+registered+successfully");
                } catch (Exception e) {
                    redirect(exchange, "/?err=" + urlEncode(e.getMessage()));
                }
            } else {
                redirect(exchange, "/");
            }
        }
    }

    private class AddRouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String rNum = params.get("routeNumber");
                    String src = params.get("source");
                    String dest = params.get("destination");
                    String boarding = params.get("boardingPoint");
                    double fare = Double.parseDouble(params.getOrDefault("fare", "30"));
                    boolean avail = "1".equals(params.get("available")) || "true".equalsIgnoreCase(params.get("available"));

                    routeService.addRoute(rNum, src, dest, boarding, fare, avail);
                    redirect(exchange, "/?msg=Route+" + rNum + "+added+successfully");
                } catch (Exception e) {
                    redirect(exchange, "/?err=" + urlEncode(e.getMessage()));
                }
            } else {
                redirect(exchange, "/");
            }
        }
    }

    private class IssuePassHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String pId = params.get("passengerId");
                    String rNum = params.get("routeNumber");
                    String type = params.get("passType");

                    BusPass pass = passService.issuePass(pId, rNum, type);
                    redirect(exchange, "/?msg=Pass+" + pass.getPassId() + "+issued+successfully+(Fee:+Rs.+" + pass.getFee() + ")");
                } catch (Exception e) {
                    redirect(exchange, "/?err=" + urlEncode(e.getMessage()));
                }
            } else {
                redirect(exchange, "/");
            }
        }
    }

    private class RenewPassHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String passId = params.get("passId");
                    String type = params.get("passType");

                    BusPass pass = passService.renewPass(passId, type);
                    redirect(exchange, "/?msg=Pass+" + pass.getPassId() + "+renewed+until+" + pass.getExpiryDate());
                } catch (Exception e) {
                    redirect(exchange, "/?err=" + urlEncode(e.getMessage()));
                }
            } else {
                redirect(exchange, "/");
            }
        }
    }

    private class CancelPassHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String passId = params.get("passId");
                    passService.cancelPass(passId);
                    redirect(exchange, "/?msg=Pass+" + passId + "+marked+as+CANCELLED");
                } catch (Exception e) {
                    redirect(exchange, "/?err=" + urlEncode(e.getMessage()));
                }
            } else {
                redirect(exchange, "/");
            }
        }
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }

    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        byte[] body = exchange.getRequestBody().readAllBytes();
        String query = new String(body, StandardCharsets.UTF_8);
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            } else if (kv.length == 1) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private String urlEncode(String val) {
        if (val == null) return "";
        return java.net.URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    private String buildDashboardHtml() throws SQLException {
        List<Passenger> passengers = passengerService.getAllPassengers();
        List<BusRoute> routes = routeService.getAllRoutes();
        List<BusPass> passes = passService.getAllPasses();
        List<BusPass> expired = passService.getExpiredPasses();
        List<BusPass> expiringSoon = passService.getPassesExpiringSoon(7);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        html.append("<title>College Bus Pass Management System</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; background: #f0f2f5; color: #333; }");
        html.append(".header { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); color: white; padding: 20px 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.15); display: flex; justify-content: space-between; align-items: center; }");
        html.append(".header h1 { margin: 0; font-size: 24px; } .header p { margin: 5px 0 0 0; opacity: 0.85; font-size: 13px; }");
        html.append(".container { max-width: 1200px; margin: 25px auto; padding: 0 20px; }");
        html.append(".stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 25px; }");
        html.append(".stat-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.06); text-align: center; border-left: 4px solid #2a5298; }");
        html.append(".stat-card h3 { margin: 0; font-size: 28px; color: #1e3c72; } .stat-card p { margin: 5px 0 0 0; font-size: 13px; color: #666; font-weight: 600; }");
        html.append(".stat-card.alert { border-left-color: #e74c3c; } .stat-card.alert h3 { color: #e74c3c; }");
        html.append(".grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px; }");
        html.append(".card { background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.06); padding: 20px; }");
        html.append(".card h2 { margin-top: 0; font-size: 18px; border-bottom: 2px solid #f0f2f5; padding-bottom: 10px; color: #1e3c72; }");
        html.append("table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 10px; }");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #eee; }");
        html.append("th { background: #f8f9fa; color: #444; font-weight: 600; }");
        html.append(".badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }");
        html.append(".badge-active { background: #e8f5e9; color: #2e7d32; }");
        html.append(".badge-expired { background: #ffebee; color: #c62828; }");
        html.append(".badge-cancelled { background: #efebe9; color: #4e342e; }");
        html.append(".form-group { margin-bottom: 12px; }");
        html.append("label { display: block; font-size: 12px; font-weight: 600; margin-bottom: 4px; color: #555; }");
        html.append("input, select { width: 100%; padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; font-size: 13px; }");
        html.append("button { background: #2a5298; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-size: 13px; font-weight: 600; width: 100%; }");
        html.append("button:hover { background: #1e3c72; }");
        html.append(".btn-danger { background: #e74c3c; } .btn-danger:hover { background: #c0392b; }");
        html.append("</style></head><body>");

        html.append("<div class='header'>");
        html.append("<div><h1>College Bus Pass Management System</h1><p>Transport Administration Portal & Localhost Server</p></div>");
        html.append("<div><span>Database: SQLite (buspass.db)</span></div>");
        html.append("</div>");

        html.append("<div class='container'>");

        // Summary Statistics
        html.append("<div class='stats'>");
        html.append("<div class='stat-card'><h3>").append(passengers.size()).append("</h3><p>REGISTERED PASSENGERS</p></div>");
        html.append("<div class='stat-card'><h3>").append(routes.size()).append("</h3><p>BUS ROUTES</p></div>");
        html.append("<div class='stat-card'><h3>").append(passes.size()).append("</h3><p>TOTAL PASSES</p></div>");
        html.append("<div class='stat-card alert'><h3>").append(expired.size() + expiringSoon.size()).append("</h3><p>EXPIRING / EXPIRED</p></div>");
        html.append("</div>");

        // Action Forms
        html.append("<div class='grid-2'>");

        // Form 1: Issue Pass
        html.append("<div class='card'>");
        html.append("<h2>Issue Bus Pass (Polymorphic Fee Calculation)</h2>");
        html.append("<form method='POST' action='/api/issue-pass'>");
        html.append("<div class='form-group'><label>Passenger ID:</label><input type='text' name='passengerId' placeholder='e.g. STU101 or FAC201' required></div>");
        html.append("<div class='form-group'><label>Route Number:</label><select name='routeNumber'>");
        for (BusRoute r : routes) {
            if (r.isAvailable()) {
                html.append("<option value='").append(r.getRouteNumber()).append("'>").append(r.getRouteNumber())
                    .append(" - ").append(r.getSource()).append(" (Rs. ").append(r.getFare()).append(")</option>");
            }
        }
        html.append("</select></div>");
        html.append("<div class='form-group'><label>Pass Duration / Type:</label><select name='passType'><option value='MONTHLY'>MONTHLY (30 Days - Subsidy Applied)</option><option value='SEMESTER'>SEMESTER (180 Days - Subsidy Applied)</option></select></div>");
        html.append("<button type='submit'>Issue Pass & Calculate Fee</button>");
        html.append("</form>");
        html.append("</div>");

        // Form 2: Register Passenger
        html.append("<div class='card'>");
        html.append("<h2>Register New Passenger</h2>");
        html.append("<form method='POST' action='/api/register-passenger'>");
        html.append("<div style='display:grid;grid-template-columns:1fr 1fr;gap:10px;'>");
        html.append("<div class='form-group'><label>ID:</label><input type='text' name='id' placeholder='STU104 / FAC203' required></div>");
        html.append("<div class='form-group'><label>Type:</label><select name='type'><option value='STUDENT'>STUDENT (20% Subsidy)</option><option value='FACULTY'>FACULTY (10% Subsidy)</option></select></div>");
        html.append("</div>");
        html.append("<div class='form-group'><label>Full Name:</label><input type='text' name='name' required></div>");
        html.append("<div style='display:grid;grid-template-columns:1fr 1fr;gap:10px;'>");
        html.append("<div class='form-group'><label>Phone (10 Digits):</label><input type='text' name='phone' pattern='\\d{10}' required></div>");
        html.append("<div class='form-group'><label>Email:</label><input type='email' name='email' required></div>");
        html.append("</div>");
        html.append("<button type='submit' style='background:#27ae60;'>Register Passenger</button>");
        html.append("</form>");
        html.append("</div>");

        html.append("</div>"); // end grid-2

        // Table 1: Bus Passes
        html.append("<div class='card' style='margin-bottom:25px;'>");
        html.append("<h2>Issued Bus Passes</h2>");
        html.append("<table><thead><tr><th>Pass ID</th><th>Passenger</th><th>Route</th><th>Type</th><th>Fee</th><th>Issued</th><th>Expires</th><th>Status</th></tr></thead><tbody>");
        for (BusPass p : passes) {
            String badgeClass = "badge-active";
            if ("EXPIRED".equalsIgnoreCase(p.getStatus()) || p.isExpired()) badgeClass = "badge-expired";
            if ("CANCELLED".equalsIgnoreCase(p.getStatus())) badgeClass = "badge-cancelled";
            html.append("<tr>");
            html.append("<td><strong>").append(p.getPassId()).append("</strong></td>");
            html.append("<td>").append(p.getPassengerId()).append("</td>");
            html.append("<td>").append(p.getRouteNumber()).append("</td>");
            html.append("<td>").append(p.getPassType()).append("</td>");
            html.append("<td>Rs. ").append(String.format("%.2f", p.getFee())).append("</td>");
            html.append("<td>").append(p.getIssueDate()).append("</td>");
            html.append("<td>").append(p.getExpiryDate()).append("</td>");
            html.append("<td><span class='badge ").append(badgeClass).append("'>").append(p.getStatus()).append("</span></td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("</div>");

        // Table 2: Bus Routes
        html.append("<div class='card' style='margin-bottom:25px;'>");
        html.append("<h2>Bus Route Network Catalog</h2>");
        html.append("<table><thead><tr><th>Route #</th><th>Origin</th><th>Destination</th><th>Boarding Landmark</th><th>Fare</th><th>Status</th></tr></thead><tbody>");
        for (BusRoute r : routes) {
            html.append("<tr>");
            html.append("<td><strong>").append(r.getRouteNumber()).append("</strong></td>");
            html.append("<td>").append(r.getSource()).append("</td>");
            html.append("<td>").append(r.getDestination()).append("</td>");
            html.append("<td>").append(r.getBoardingPoint()).append("</td>");
            html.append("<td>Rs. ").append(String.format("%.2f", r.getFare())).append("</td>");
            html.append("<td>").append(r.isAvailable() ? "<span class='badge badge-active'>ACTIVE</span>" : "<span class='badge badge-expired'>SUSPENDED</span>").append("</td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("</div>");

        html.append("</div></body></html>");
        return html.toString();
    }
}
