// 代码生成时间: 2025-09-23 16:58:45
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.freemarker.FreeMarkerEngine;
import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

public class LoginService {
    // Initialize the user data store (for demonstration purposes only)
    private static final Map<String, String> userData = new HashMap<>();

    static {
        // Pre-populate user data
        userData.put("user1", "password1");
        userData.put("user2", "password2");
    }

    public static void main(String[] args) {
        port(4567); // Set the port to be used by Spark

        // Set up route to serve the login page
        get("/login", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                return new FreeMarkerEngine().render(new HashMap<>(), "login.ftl");
            }
        });

        // Set up route to handle login request
        post("/login", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String username = request.queryParams("username");
            String password = request.queryParams("password");

            try {
                // Perform login validation
                if (userData.containsKey(username) && userData.get(username).equals(password)) {
                    model.put("message", "Login successful!");
                    response.status(200);
                } else {
                    model.put("message", "Invalid username or password.");
                    response.status(401);
                }
            } catch (Exception e) {
                model.put("message", "Error during login: " + e.getMessage());
                response.status(500);
            }

            return new FreeMarkerEngine().render(model, "loginResult.ftl");
        });
    }
}
