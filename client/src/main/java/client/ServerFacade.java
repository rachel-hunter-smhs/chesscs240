package client;
import com.google.gson.Gson;
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
}
