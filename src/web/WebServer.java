package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.BusPass;
import model.BusRoute;
import model.Passenger;
import model.User;
import service.AuthService;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Embedded Localhost Web Server (Java Standard Library HttpServer).
 * Faithfully matches the UniBus Desktop UI mockup design at http://localhost:8080
 */
public class WebServer {
    private final AuthService authService;
    private final PassengerService passengerService;
    private final RouteService routeService;
    private final BusPassService passService;
    private HttpServer server;

    private static final Map<String, User> activeSessions = new ConcurrentHashMap<>();

    public WebServer(AuthService authService, PassengerService passengerService, 
                     RouteService routeService, BusPassService passService) {
        this.authService = authService;
        this.passengerService = passengerService;
        this.routeService = routeService;
        this.passService = passService;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new DashboardHandler());
        server.createContext("/login", new LoginRegisterPageHandler());
        server.createContext("/auth/login", new AuthLoginHandler());
        server.createContext("/auth/register", new AuthRegisterHandler());
        server.createContext("/auth/logout", new AuthLogoutHandler());
        server.createContext("/api/register-passenger", new RegisterPassengerHandler());
        server.createContext("/api/add-route", new AddRouteHandler());
        server.createContext("/api/issue-pass", new IssuePassHandler());
        server.createContext("/api/renew-pass", new RenewPassHandler());
        server.createContext("/api/cancel-pass", new CancelPassHandler());
        server.setExecutor(null);
        server.start();
        System.out.println(">>> Localhost Web Server running at: http://localhost:" + port + " <<<");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private User getSessionUser(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String c : cookies) {
                String[] parts = c.trim().split("=");
                if (parts.length == 2 && "AUTH_TOKEN".equals(parts[0])) {
                    return activeSessions.get(parts[1]);
                }
            }
        }
        return null;
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            User user = getSessionUser(exchange);
            if (user == null) {
                redirect(exchange, "/login");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                String msg = "";
                String err = "";
                if (query != null) {
                    for (String p : query.split("&")) {
                        String[] kv = p.split("=");
                        if (kv.length == 2) {
                            if ("msg".equals(kv[0])) msg = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                            if ("err".equals(kv[0])) err = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        }
                    }
                }

                String html = buildDashboardHtml(user, msg, err);
                sendHtmlResponse(exchange, 200, html);
            } catch (Exception e) {
                e.printStackTrace();
                sendHtmlResponse(exchange, 500, "Server Error: " + e.getMessage());
            }
        }
    }

    private class LoginRegisterPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String msg = "";
            String err = "";
            if (query != null) {
                for (String p : query.split("&")) {
                    String[] kv = p.split("=");
                    if (kv.length == 2) {
                        if ("msg".equals(kv[0])) msg = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        if ("err".equals(kv[0])) err = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    }
                }
            }
            String html = buildLoginPageHtml(msg, err);
            sendHtmlResponse(exchange, 200, html);
        }
    }

    private class AuthLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String u = params.get("username");
                String p = params.get("password");

                try {
                    User user = authService.login(u, p);
                    if (user != null) {
                        String token = UUID.randomUUID().toString();
                        activeSessions.put(token, user);
                        exchange.getResponseHeaders().add("Set-Cookie", "AUTH_TOKEN=" + token + "; Path=/; HttpOnly");
                        redirect(exchange, "/");
                    } else {
                        redirect(exchange, "/login?err=" + urlEncode("Invalid username or password"));
                    }
                } catch (Exception ex) {
                    redirect(exchange, "/login?err=" + urlEncode(ex.getMessage()));
                }
            } else {
                redirect(exchange, "/login");
            }
        }
    }

    private class AuthRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String u = params.get("username");
                String p = params.get("password");
                String name = params.get("fullName");
                String email = params.get("email");
                String role = params.get("role");

                try {
                    authService.register(u, p, name, email, role, null);
                    redirect(exchange, "/login?msg=" + urlEncode("Registration successful! Please sign in with your new credentials."));
                } catch (Exception ex) {
                    redirect(exchange, "/login?err=" + urlEncode(ex.getMessage()));
                }
            } else {
                redirect(exchange, "/login");
            }
        }
    }

    private class AuthLogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookieHeader != null) {
                for (String c : cookieHeader.split(";")) {
                    String[] parts = c.trim().split("=");
                    if (parts.length == 2 && "AUTH_TOKEN".equals(parts[0])) {
                        activeSessions.remove(parts[1]);
                    }
                }
            }
            exchange.getResponseHeaders().add("Set-Cookie", "AUTH_TOKEN=; Path=/; Max-Age=0");
            redirect(exchange, "/login?msg=" + urlEncode("Signed out successfully."));
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
                    String route = params.get("route");
                    String passType = params.get("passType");
                    int validity = "SEMESTER".equalsIgnoreCase(passType) ? 180 : 30;

                    passengerService.registerPassenger(id, name, phone, email, type, validity);

                    // Auto issue pass if route is selected
                    if (route != null && !route.isEmpty()) {
                        try {
                            passService.issuePass(id, route, passType);
                        } catch (Exception ignored) {}
                    }

                    redirect(exchange, "/?msg=" + urlEncode("Passenger " + name + " (" + id + ") registered and pass issued successfully!"));
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
                    boolean avail = "1".equals(params.get("available")) || "true".equalsIgnoreCase(params.get("available")) || "on".equalsIgnoreCase(params.get("available"));

                    routeService.addRoute(rNum, src, dest, boarding, fare, avail);
                    redirect(exchange, "/?msg=" + urlEncode("Bus Route " + rNum + " added to catalog successfully!"));
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
                    redirect(exchange, "/?msg=" + urlEncode("Bus Pass #" + pass.getPassId() + " generated! Calculated Fee: Rs. " + String.format("%.2f", pass.getFee())));
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
                    redirect(exchange, "/?msg=" + urlEncode("Pass #" + pass.getPassId() + " renewed until " + pass.getExpiryDate() + " (Fee: Rs. " + pass.getFee() + ")"));
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
                    redirect(exchange, "/?msg=" + urlEncode("Pass #" + passId + " marked as CANCELLED."));
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

    private void sendHtmlResponse(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
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

    private String buildLoginPageHtml(String msg, String err) {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
               "<title>Sign In - UniBus | Pass Manager</title>" +
               "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>" +
               "<style>" +
               "* { box-sizing: border-box; font-family: 'Inter', sans-serif; }" +
               "body { background: #0f172a; margin: 0; min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
               ".login-window { background: #1e293b; border: 1px solid #334155; border-radius: 12px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); width: 440px; overflow: hidden; }" +
               ".window-bar { background: #0f172a; padding: 12px 16px; display: flex; align-items: center; gap: 8px; border-bottom: 1px solid #334155; }" +
               ".dot { width: 12px; height: 12px; border-radius: 50%; }" +
               ".dot-red { background: #ef4444; } .dot-yellow { background: #f59e0b; } .dot-green { background: #10b981; }" +
               ".window-title { margin-left: auto; margin-right: auto; font-size: 12px; color: #94a3b8; font-weight: 500; }" +
               ".login-content { padding: 32px 28px; }" +
               ".brand { display: flex; align-items: center; gap: 10px; margin-bottom: 24px; }" +
               ".brand-icon { width: 36px; height: 36px; background: #0ea5e9; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px; font-weight: bold; }" +
               ".brand-text h2 { margin: 0; font-size: 18px; color: #f8fafc; font-weight: 700; }" +
               ".brand-text p { margin: 2px 0 0 0; font-size: 12px; color: #94a3b8; }" +
               ".tabs { display: flex; border-bottom: 1px solid #334155; margin-bottom: 20px; }" +
               ".tab-btn { flex: 1; text-align: center; padding: 10px; cursor: pointer; font-size: 13px; font-weight: 600; color: #94a3b8; border-bottom: 2px solid transparent; }" +
               ".tab-btn.active { color: #38bdf8; border-bottom-color: #38bdf8; }" +
               ".form-group { margin-bottom: 16px; }" +
               "label { display: block; font-size: 12px; font-weight: 500; margin-bottom: 6px; color: #cbd5e1; }" +
               "input, select { width: 100%; padding: 10px 12px; background: #0f172a; border: 1px solid #334155; border-radius: 6px; color: #f8fafc; font-size: 13px; outline: none; transition: border 0.2s; }" +
               "input:focus, select:focus { border-color: #38bdf8; }" +
               "button.submit-btn { background: #0284c7; color: white; border: none; padding: 12px; border-radius: 6px; font-size: 14px; font-weight: 600; width: 100%; cursor: pointer; transition: background 0.2s; margin-top: 8px; }" +
               "button.submit-btn:hover { background: #0369a1; }" +
               ".banner { padding: 10px 14px; border-radius: 6px; font-size: 12px; margin-bottom: 16px; line-height: 1.4; }" +
               ".banner-success { background: #064e3b; color: #6ee7b7; border: 1px solid #059669; }" +
               ".banner-error { background: #7f1d1d; color: #fca5a5; border: 1px solid #dc2626; }" +
               ".hint { margin-top: 20px; padding: 12px; background: #0f172a; border-radius: 6px; font-size: 11px; color: #94a3b8; line-height: 1.6; border: 1px solid #1e293b; }" +
               "</style></head><body>" +
               "<div class='login-window'>" +
               "<div class='window-bar'>" +
               "<div class='dot dot-red'></div><div class='dot dot-yellow'></div><div class='dot dot-green'></div>" +
               "<div class='window-title'>UniBus | Desktop Sign In</div>" +
               "</div>" +
               "<div class='login-content'>" +
               "<div class='brand'>" +
               "<div class='brand-icon'>[U]</div>" +
               "<div class='brand-text'><h2>UniBus | Pass Manager</h2><p>University Transport Portal</p></div>" +
               "</div>" +
               (msg.isEmpty() ? "" : "<div class='banner banner-success'>" + msg + "</div>") +
               (err.isEmpty() ? "" : "<div class='banner banner-error'>" + err + "</div>") +
               "<div class='tabs'>" +
               "<div class='tab-btn active' onclick='showTab(\"login\")'>Sign In</div>" +
               "<div class='tab-btn' onclick='showTab(\"register\")'>Create Account</div>" +
               "</div>" +
               "<form id='loginForm' method='POST' action='/auth/login'>" +
               "<div class='form-group'><label>Username</label><input type='text' name='username' value='admin' required></div>" +
               "<div class='form-group'><label>Password</label><input type='password' name='password' value='admin123' required></div>" +
               "<button type='submit' class='submit-btn'>Sign In to Dashboard</button>" +
               "</form>" +
               "<form id='regForm' method='POST' action='/auth/register' style='display:none;'>" +
               "<div class='form-group'><label>Username</label><input type='text' name='username' placeholder='e.g. rahul101' required></div>" +
               "<div class='form-group'><label>Password</label><input type='password' name='password' placeholder='Min 4 characters' required></div>" +
               "<div class='form-group'><label>Full Name</label><input type='text' name='fullName' placeholder='e.g. Rahul Sharma' required></div>" +
               "<div class='form-group'><label>Email ID</label><input type='email' name='email' placeholder='rahul@university.edu' required></div>" +
               "<div class='form-group'><label>Role</label><select name='role'><option value='STUDENT'>STUDENT (20% Subsidy)</option><option value='FACULTY'>FACULTY (10% Subsidy)</option><option value='ADMIN'>ADMINISTRATOR</option></select></div>" +
               "<button type='submit' class='submit-btn' style='background:#10b981;'>Register Account</button>" +
               "</form>" +
               "<div class='hint'>" +
               "<strong>Default Accounts:</strong><br>" +
               "• Admin: <code>admin</code> / <code>admin123</code><br>" +
               "• Student: <code>aarav</code> / <code>student123</code><br>" +
               "• Faculty: <code>ramesh</code> / <code>faculty123</code>" +
               "</div>" +
               "</div></div>" +
               "<script>" +
               "function showTab(t) {" +
               "  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));" +
               "  if (t === 'login') {" +
               "    document.querySelectorAll('.tab-btn')[0].classList.add('active');" +
               "    document.getElementById('loginForm').style.display = 'block';" +
               "    document.getElementById('regForm').style.display = 'none';" +
               "  } else {" +
               "    document.querySelectorAll('.tab-btn')[1].classList.add('active');" +
               "    document.getElementById('loginForm').style.display = 'none';" +
               "    document.getElementById('regForm').style.display = 'block';" +
               "  }" +
               "}" +
               "</script>" +
               "</body></html>";
    }

    private String buildDashboardHtml(User user, String msg, String err) throws SQLException {
        List<Passenger> passengers = passengerService.getAllPassengers();
        List<BusRoute> routes = routeService.getAllRoutes();
        List<BusPass> passes = passService.getAllPasses();
        List<BusPass> expired = passService.getExpiredPasses();
        List<BusPass> expiringSoon = passService.getPassesExpiringSoon(7);

        int activePassesCount = (int) passes.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()) && !p.isExpired()).count();
        int expiringCount = expiringSoon.size();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        html.append("<title>UniBus | Bus Pass Management System</title>");
        html.append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>");
        html.append("<style>");
        html.append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif; }");
        html.append("body { background: #0f172a; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }");

        html.append(".app-window { width: 1280px; height: 860px; background: #f8fafc; border-radius: 12px; box-shadow: 0 25px 60px rgba(0,0,0,0.45); display: flex; flex-direction: column; overflow: hidden; border: 1px solid #334155; }");
        html.append(".window-bar { background: #ffffff; padding: 10px 16px; display: flex; align-items: center; gap: 8px; border-bottom: 1px solid #e2e8f0; }");
        html.append(".dot { width: 11px; height: 11px; border-radius: 50%; }");
        html.append(".dot-red { background: #ff5f56; } .dot-yellow { background: #ffbd2e; } .dot-green { background: #27c93f; }");

        html.append(".app-body { display: flex; flex: 1; height: calc(100% - 32px); overflow: hidden; }");

        html.append(".sidebar { width: 230px; background: #192231; color: #94a3b8; display: flex; flex-direction: column; justify-content: space-between; padding: 18px 0; shrink: 0; }");
        html.append(".brand-section { padding: 0 18px 20px 18px; display: flex; align-items: center; gap: 10px; border-bottom: 1px solid #283548; }");
        html.append(".brand-icon { width: 34px; height: 34px; background: #0ea5e9; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-size: 14px; font-weight: bold; }");
        html.append(".brand-text { color: #f8fafc; font-size: 13px; font-weight: 700; line-height: 1.2; }");

        html.append(".nav-menu { padding: 16px 10px; display: flex; flex-direction: column; gap: 4px; }");
        html.append(".nav-item { display: flex; align-items: center; gap: 10px; padding: 9px 12px; border-radius: 6px; font-size: 12.5px; font-weight: 500; color: #94a3b8; text-decoration: none; cursor: pointer; transition: all 0.2s; }");
        html.append(".nav-item:hover { color: #f8fafc; background: #232f42; }");
        html.append(".nav-item.active { background: #28384f; color: #38bdf8; font-weight: 600; }");

        html.append(".sidebar-footer { padding: 0 18px; }");
        html.append(".logout-btn { display: flex; align-items: center; gap: 8px; color: #ef4444; text-decoration: none; font-size: 12.5px; font-weight: 600; padding: 8px 0; cursor: pointer; }");
        html.append(".logout-btn:hover { color: #f87171; }");

        html.append(".main-content { flex: 1; display: flex; flex-direction: column; background: #f1f5f9; overflow-y: auto; }");

        html.append(".top-header { background: #ffffff; padding: 14px 28px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e2e8f0; }");
        html.append(".header-title { display: flex; align-items: center; gap: 12px; }");
        html.append(".header-logo { width: 34px; height: 34px; background: #0f172a; border-radius: 6px; display: flex; align-items: center; justify-content: center; color: white; font-size: 13px; font-weight: bold; }");
        html.append(".header-text h1 { font-size: 15px; color: #0f172a; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; margin: 0; }");
        html.append(".header-text p { font-size: 11px; color: #64748b; font-weight: 600; margin: 2px 0 0 0; }");
        html.append(".header-user { display: flex; align-items: center; gap: 12px; }");
        html.append(".user-avatar { width: 34px; height: 34px; border-radius: 50%; background: #e2e8f0; border: 2px solid #0ea5e9; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; color: #0f172a; }");
        html.append(".user-info { text-align: right; }");
        html.append(".user-name { font-size: 12.5px; font-weight: 600; color: #0f172a; }");
        html.append(".user-role { font-size: 10px; font-weight: 700; color: #0284c7; text-transform: uppercase; }");
        html.append(".bell-icon { font-size: 15px; color: #64748b; cursor: pointer; margin-left: 6px; font-weight: bold; }");

        html.append(".dashboard-grid { padding: 18px 24px; display: grid; grid-template-columns: 1fr 1fr; gap: 18px; flex: 1; }");
        html.append(".card { background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.04); display: flex; flex-direction: column; overflow: hidden; }");

        html.append(".card-header { padding: 10px 16px; background: #192231; color: white; display: flex; align-items: center; justify-content: space-between; }");
        html.append(".card-header.teal { background: #185a66; }");
        html.append(".card-title { font-size: 12.5px; font-weight: 700; letter-spacing: 0.4px; display: flex; align-items: center; gap: 6px; }");
        html.append(".card-btn { background: #0f172a; color: white; border: 1px solid #334155; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; cursor: pointer; }");
        html.append(".card-btn:hover { background: #1e293b; }");

        html.append(".card-body { padding: 14px 16px; flex: 1; display: flex; flex-direction: column; font-size: 12px; }");
        html.append(".form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 10px; }");
        html.append(".form-group { margin-bottom: 9px; }");
        html.append("label { display: block; font-size: 11px; font-weight: 600; color: #475569; margin-bottom: 3px; }");
        html.append("label .req { color: #ef4444; }");
        html.append("input, select { width: 100%; padding: 6.5px 9px; border: 1px solid #cbd5e1; border-radius: 4px; font-size: 11.5px; color: #0f172a; background: #ffffff; outline: none; }");
        html.append("input:focus, select:focus { border-color: #0284c7; }");
        html.append(".btn-teal { background: #187785; color: white; border: none; padding: 8px 14px; border-radius: 4px; font-size: 12px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; gap: 6px; }");
        html.append(".btn-teal:hover { background: #13606c; }");

        html.append(".table-wrap { overflow-x: auto; flex: 1; max-height: 250px; }");
        html.append("table { width: 100%; border-collapse: collapse; font-size: 11.5px; }");
        html.append("th { background: #f8fafc; color: #475569; font-weight: 600; text-align: left; padding: 7px 9px; border-bottom: 1px solid #e2e8f0; font-size: 11px; }");
        html.append("td { padding: 7px 9px; border-bottom: 1px solid #f1f5f9; color: #1e293b; }");
        html.append(".badge { display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: 700; text-transform: uppercase; }");
        html.append(".badge-active { background: #dcfce7; color: #15803d; }");
        html.append(".badge-inactive { background: #fee2e2; color: #b91c1c; }");

        html.append(".monitor-strip { background: #185a66; color: white; padding: 8px 12px; border-radius: 4px; font-size: 11px; font-weight: 600; margin-bottom: 10px; display: flex; justify-content: space-between; }");

        html.append(".alert-bar { margin: 12px 24px 0 24px; padding: 8px 14px; border-radius: 6px; font-size: 12px; font-weight: 500; }");
        html.append(".alert-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }");
        html.append(".alert-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }");
        html.append("</style></head><body>");

        html.append("<div class='app-window'>");

        html.append("<div class='window-bar'>");
        html.append("<div class='dot dot-red'></div><div class='dot dot-yellow'></div><div class='dot dot-green'></div>");
        html.append("</div>");

        html.append("<div class='app-body'>");

        // Sidebar
        html.append("<div class='sidebar'>");
        html.append("<div>");
        html.append("<div class='brand-section'>");
        html.append("<div class='brand-icon'>[U]</div>");
        html.append("<div class='brand-text'>UniBus | Pass Manager</div>");
        html.append("</div>");

        html.append("<div class='nav-menu'>");
        html.append("<a class='nav-item active'>Dashboard</a>");
        html.append("<a class='nav-item' onclick='document.getElementById(\"studentIdInput\").focus();'>Passenger Registration</a>");
        html.append("<a class='nav-item' onclick='document.getElementById(\"routeTable\").scrollIntoView();'>Route Catalog</a>");
        html.append("<a class='nav-item' onclick='document.getElementById(\"calcPassengerId\").focus();'>Fee Management</a>");
        html.append("<a class='nav-item' onclick='document.getElementById(\"validityTable\").scrollIntoView();'>Validity Monitor</a>");
        html.append("<a class='nav-item'>Reports</a>");
        html.append("<a class='nav-item'>Settings</a>");
        html.append("</div></div>");

        html.append("<div class='sidebar-footer'>");
        html.append("<a href='/auth/logout' class='logout-btn'>LOGOUT</a>");
        html.append("</div></div>");

        // Right Content
        html.append("<div class='main-content'>");

        // Top Header
        html.append("<div class='top-header'>");
        html.append("<div class='header-title'>");
        html.append("<div class='header-logo'>[U]</div>");
        html.append("<div class='header-text'>");
        html.append("<h1>UNIVERSITY OF TECH</h1>");
        html.append("<p>BUS PASS MANAGEMENT SYSTEM</p>");
        html.append("</div></div>");

        html.append("<div class='header-user'>");
        html.append("<div class='user-avatar'>AD</div>");
        html.append("<div class='user-info'>");
        html.append("<div class='user-name'>").append(user.getFullName()).append("</div>");
        html.append("<div class='user-role'>").append(user.getRole()).append(" PANEL</div>");
        html.append("</div>");
        html.append("<div class='bell-icon' title='Notifications'>[!]</div>");
        html.append("</div></div>");

        if (!msg.isEmpty()) {
            html.append("<div class='alert-bar alert-success'>[SUCCESS] ").append(msg).append("</div>");
        }
        if (!err.isEmpty()) {
            html.append("<div class='alert-bar alert-error'>[ALERT] ").append(err).append("</div>");
        }

        // 2x2 Grid Content
        html.append("<div class='dashboard-grid'>");

        // CARD 1: PASSENGER REGISTRATION FORM (Top-Left)
        html.append("<div class='card'>");
        html.append("<div class='card-header'>");
        html.append("<div class='card-title'>PASSENGER REGISTRATION FORM</div>");
        html.append("</div>");
        html.append("<div class='card-body'>");
        html.append("<form method='POST' action='/api/register-passenger'>");
        html.append("<div class='form-row'>");
        html.append("<div class='form-group'><label>Student ID <span class='req'>(required)</span></label><input type='text' id='studentIdInput' name='id' placeholder='e.g. STU105' required></div>");
        html.append("<div class='form-group'><label>Full Name</label><input type='text' name='name' placeholder='Full Name' required></div>");
        html.append("</div>");

        html.append("<div class='form-row'>");
        html.append("<div class='form-group'><label>Contact Number</label><input type='text' name='phone' placeholder='10-digit number' pattern='\\d{10}' required></div>");
        html.append("<div class='form-group'><label>Email Address</label><input type='email' name='email' placeholder='name@university.edu' required></div>");
        html.append("</div>");

        html.append("<div class='form-row'>");
        html.append("<div class='form-group'><label>Passenger Type</label><select name='type'><option value='STUDENT'>STUDENT (20% Subsidy)</option><option value='FACULTY'>FACULTY (10% Subsidy)</option></select></div>");
        html.append("<div class='form-group'><label>Course / Dept</label><select name='dept'><option>B.Tech CSE</option><option>B.Tech ECE</option><option>B.Tech Mech</option><option>MBA</option><option>Faculty Staff</option></select></div>");
        html.append("</div>");

        html.append("<div class='form-row'>");
        html.append("<div class='form-group'><label>Route</label><select name='route'>");
        for (BusRoute r : routes) {
            if (r.isAvailable()) {
                html.append("<option value='").append(r.getRouteNumber()).append("'>").append(r.getRouteNumber())
                    .append(" - ").append(r.getSource()).append("</option>");
            }
        }
        html.append("</select></div>");
        html.append("<div class='form-group'><label>Pass Type (Dropdown)</label><select name='passType'><option value='MONTHLY'>Monthly (30 Days)</option><option value='SEMESTER'>Semester (180 Days)</option></select></div>");
        html.append("</div>");

        html.append("<div class='form-group'><label>Address / Local Stoppage</label><input type='text' name='address' placeholder='Enter pickup location or address'></div>");
        html.append("<div style='margin-top:auto; padding-top:8px;'><button type='submit' class='btn-teal' style='width:100%;'>Register Passenger</button></div>");
        html.append("</form>");
        html.append("</div></div>");

        // CARD 2: ROUTE CATALOG (Top-Right)
        html.append("<div class='card'>");
        html.append("<div class='card-header teal'>");
        html.append("<div class='card-title'>ROUTE CATALOG</div>");
        html.append("<button type='button' class='card-btn' onclick='showNewRouteModal()'>+ New Route</button>");
        html.append("</div>");
        html.append("<div class='card-body'>");
        html.append("<div class='table-wrap' id='routeTable'>");
        html.append("<table><thead><tr><th>Route ID</th><th>Origin</th><th>Destination</th><th>Stoppages</th><th>Fare</th><th>Status</th></tr></thead><tbody>");
        for (BusRoute r : routes) {
            html.append("<tr>");
            html.append("<td><strong>").append(r.getRouteNumber()).append("</strong></td>");
            html.append("<td>").append(r.getSource()).append("</td>");
            html.append("<td>").append(r.getDestination()).append("</td>");
            html.append("<td>").append(r.getBoardingPoint()).append("</td>");
            html.append("<td>Rs. ").append(String.format("%.1f", r.getFare())).append("</td>");
            html.append("<td>").append(r.isAvailable() ? "<span class='badge badge-active'>Active</span>" : "<span class='badge badge-inactive'>Inactive</span>").append("</td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("</div></div></div>");

        // CARD 3: PASS FEE CALCULATION (Bottom-Left)
        html.append("<div class='card'>");
        html.append("<div class='card-header'>");
        html.append("<div class='card-title'>PASS FEE CALCULATION</div>");
        html.append("</div>");
        html.append("<div class='card-body'>");
        html.append("<form method='POST' action='/api/issue-pass'>");
        html.append("<div class='form-row'>");
        html.append("<div class='form-group'><label>Select Route</label><select id='calcRoute' name='routeNumber' onchange='computePreviewFee()'>");
        for (BusRoute r : routes) {
            if (r.isAvailable()) {
                html.append("<option value='").append(r.getRouteNumber()).append("' data-fare='").append(r.getFare()).append("'>")
                    .append(r.getRouteNumber()).append(" (Base Rs. ").append(r.getFare()).append(")</option>");
            }
        }
        html.append("</select></div>");
        html.append("<div class='form-group'><label>Pass Type</label><select id='calcPassType' name='passType' onchange='computePreviewFee()'><option value='MONTHLY'>Monthly</option><option value='SEMESTER'>Semester</option></select></div>");
        html.append("</div>");

        html.append("<div class='form-row'>");
        html.append("<div class='form-group'><label>Passenger ID</label><input type='text' id='calcPassengerId' name='passengerId' placeholder='e.g. STU101' required></div>");
        html.append("<div class='form-group'><label>Student/Passenger Type</label><select id='calcUserType' onchange='computePreviewFee()'><option value='STUDENT'>Regular (Student)</option><option value='FACULTY'>Faculty Member</option></select></div>");
        html.append("</div>");

        html.append("<div style='background:#f8fafc; border:1px dashed #cbd5e1; border-radius:6px; padding:10px 14px; margin:8px 0; display:flex; align-items:center; justify-content:space-between;'>");
        html.append("<div><span style='font-size:11px; color:#64748b; font-weight:600;'>Calculated Fee:</span><div id='feeDisplay' style='font-size:20px; font-weight:700; color:#0f172a; margin-top:2px;'>Rs. 440.00</div></div>");
        html.append("<button type='submit' class='btn-teal'>Calculate & Issue</button>");
        html.append("</div>");
        html.append("</form>");
        html.append("</div></div>");

        // CARD 4: VALIDITY MONITOR (Bottom-Right)
        html.append("<div class='card'>");
        html.append("<div class='card-header teal'>");
        html.append("<div class='card-title'>VALIDITY MONITOR</div>");
        html.append("</div>");
        html.append("<div class='card-body'>");
        html.append("<div class='monitor-strip'>");
        html.append("<span>Active Passes: <strong>").append(activePassesCount).append("</strong></span>");
        html.append("<span>Expiring Soon: <strong>").append(expiringCount).append("</strong> (Today/7d)</span>");
        html.append("<span>Passes Issued: <strong>").append(passes.size()).append("</strong></span>");
        html.append("</div>");

        html.append("<div class='table-wrap' id='validityTable'>");
        html.append("<table><thead><tr><th>Student Name / ID</th><th>Pass ID</th><th>Expiry Date</th><th>Status</th><th>Actions</th></tr></thead><tbody>");
        for (BusPass p : passes) {
            String badgeClass = "badge-active";
            String statusText = "Active";
            if (p.isExpired() || "EXPIRED".equalsIgnoreCase(p.getStatus())) {
                badgeClass = "badge-inactive";
                statusText = "Inactive";
            }
            if ("CANCELLED".equalsIgnoreCase(p.getStatus())) {
                badgeClass = "badge-inactive";
                statusText = "Inactive";
            }
            html.append("<tr>");
            html.append("<td><strong>").append(p.getPassengerId()).append("</strong></td>");
            html.append("<td>").append(p.getPassId()).append("</td>");
            html.append("<td>").append(p.getExpiryDate()).append("</td>");
            html.append("<td><span class='badge ").append(badgeClass).append("'>").append(statusText).append("</span></td>");
            html.append("<td><form method='POST' action='/api/renew-pass' style='display:inline;'><input type='hidden' name='passId' value='").append(p.getPassId()).append("'><input type='hidden' name='passType' value='").append(p.getPassType()).append("'><button type='submit' style='background:#0284c7; color:white; border:none; padding:3px 7px; border-radius:3px; font-size:10px; cursor:pointer;'>Renew</button></form></td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("</div></div></div>");

        html.append("</div>"); // end dashboard-grid

        html.append("</div>"); // end main-content
        html.append("</div>"); // end app-body
        html.append("</div>"); // end app-window

        // Modal for New Route
        html.append("<div id='newRouteModal' style='display:none; position:fixed; inset:0; background:rgba(0,0,0,0.6); z-index:999; align-items:center; justify-content:center;'>");
        html.append("<div style='background:white; border-radius:8px; padding:24px; width:380px; box-shadow:0 20px 25px -5px rgba(0,0,0,0.5);'>");
        html.append("<h3 style='margin:0 0 14px 0; font-size:15px; color:#0f172a;'>Add New Bus Route</h3>");
        html.append("<form method='POST' action='/api/add-route'>");
        html.append("<div class='form-group'><label>Route Number</label><input type='text' name='routeNumber' placeholder='e.g. R-106' required></div>");
        html.append("<div class='form-group'><label>Origin / Source</label><input type='text' name='source' placeholder='e.g. South Extension' required></div>");
        html.append("<div class='form-group'><label>Destination</label><input type='text' name='destination' value='College Campus' required></div>");
        html.append("<div class='form-group'><label>Boarding Landmark</label><input type='text' name='boardingPoint' placeholder='e.g. Ring Road Stop' required></div>");
        html.append("<div class='form-group'><label>Base Fare (Rs)</label><input type='number' step='0.5' name='fare' value='30' required></div>");
        html.append("<div style='display:flex; justify-content:flex-end; gap:8px; margin-top:16px;'>");
        html.append("<button type='button' onclick='hideNewRouteModal()' style='background:#e2e8f0; color:#334155; border:none; padding:8px 12px; border-radius:4px; font-size:12px; font-weight:600; cursor:pointer;'>Cancel</button>");
        html.append("<button type='submit' class='btn-teal'>Save Route</button>");
        html.append("</div></form></div></div>");

        // Interactive Fee Script
        html.append("<script>");
        html.append("function showNewRouteModal() { document.getElementById('newRouteModal').style.display = 'flex'; }");
        html.append("function hideNewRouteModal() { document.getElementById('newRouteModal').style.display = 'none'; }");
        html.append("function computePreviewFee() {");
        html.append("  var sel = document.getElementById('calcRoute');");
        html.append("  var opt = sel.options[sel.selectedIndex];");
        html.append("  var fare = opt ? parseFloat(opt.getAttribute('data-fare') || '30') : 30;");
        html.append("  var type = document.getElementById('calcPassType').value;");
        html.append("  var userType = document.getElementById('calcUserType').value;");
        html.append("  var nominal = (type === 'SEMESTER') ? (fare * 90) : (fare * 22);");
        html.append("  var discount = (userType === 'STUDENT') ? 0.20 : 0.10;");
        html.append("  var total = nominal * (1 - discount);");
        html.append("  document.getElementById('feeDisplay').innerText = 'Rs. ' + total.toFixed(2);");
        html.append("}");
        html.append("window.onload = computePreviewFee;");
        html.append("</script>");

        html.append("</body></html>");
        return html.toString();
    }
}
