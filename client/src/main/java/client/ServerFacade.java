package client;
import com.google.gson.Gson;
import model.AuthData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final String serverLink;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverLink = "http://localhost:" + port;
        this.httpClient = HttpClient.newHttpClient();
    }
    public sendRequest register(String username, String password, String email){
        var request= new RegistrationRequest(username,password,email);
        return sendRequest("POST", "/user", request, AuthData.class, null);
    }
    private <T> T sendRequest(String method,
                              String path,
                              Object requestObject,
                              Class<T> responseClass,
                              String authToken) throws Exception {

        String url = serverLink + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        if (authToken != null && !authToken.isEmpty()) {
            builder.header("authorization", authToken);
        }
        String requestBody = requestObject == null ? "" : gson.toJson(requestObject);
        builder.header("Content-Type", "application/json");

        switch (method) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(requestBody));
            case "DELETE" -> {
                if (requestObject == null) {
                    builder.DELETE();
                } else {
                    builder.method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody));
                }
            }
            default -> throw new IllegalArgumentException("Unsupported method " + method);
        }

        HttpRequest httpRequest = builder.build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String body = response.body();

        if (status / 100 != 2) {
            if (body != null && !body.isEmpty()) {
                try {
                    ErrorResponse error = gson.fromJson(body, ErrorResponse.class);
                    if (error != null && error.message != null && !error.message.isEmpty()) {
                        throw new Exception(error.message);
                    }
                } catch (Exception ignored) {
                }
            }
            throw new Exception("Request failed with status " + status);
        }

        if (responseClass == null) {
            return null;
        }

        if (body == null || body.isEmpty()) {
            return null;
        }

        return gson.fromJson(body, responseClass);
    }
    public record AuthData(String username, String authToken) {}

    public record RegistrationRequest(String username, String password, String email) {}

    public record LoginRequest(String username, String password) {}

    public record CreateGameRequest(String gameName) {}

    public record JoinGameRequest(String playerColor, int gameID) {}

    public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName) {}

    public record GameListResult(GameData[] games) {}

    public static class ErrorResponse {
        public String message;
    }
}
