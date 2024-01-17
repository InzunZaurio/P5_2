//      Ramirez Inzunza Pedro Miguel - 7CM3
//      Sistemas Distribuidos - Proyecto 5
//      Clase WebServer: Encargada de ser la mediadora entre la comunicación con el cliente
// }    web y con los servidores de procesamiento.



import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class WebServer {
    private static List<String> topThreeTexts;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor web iniciado en http://localhost:8080/");
    }

    public static void setTopThreeTexts(List<String> texts) {
        topThreeTexts = texts;
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
            t.getResponseHeaders().add("Access-Control-Max-Age", "86400");
            t.getResponseHeaders().add("Content-Type", "text/plain");
            t.getResponseHeaders().add("Content-Length", "");

            
            String userPhrase = t.getRequestURI().getQuery();
            if (userPhrase != null && userPhrase.startsWith("userPhrase=")) {
                userPhrase = userPhrase.substring("userPhrase=".length());
            }

            System.out.println("Frase recibida en WebServer: " + userPhrase);

            Map<String, Double> recommendationMap = ProcessingServer.processWords(userPhrase.split("\\s+"));

            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("Títulos ordenados por índice de recomendación:\n");
            for (Map.Entry<String, Double> entry : recommendationMap.entrySet()) {
                responseBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            responseBuilder.append("\nTres textos con mayor recommendationIndex:\n");
            if (topThreeTexts != null) {
                for (String textTitle : topThreeTexts) {
                    responseBuilder.append("- ").append(textTitle).append("\n");
                }
            }

            String response = responseBuilder.toString();

            // Establecer la longitud del contenido en bytes
            t.getResponseHeaders().add("Content-Length", Integer.toString(response.getBytes().length));

            // Enviar la respuesta al cliente
            t.sendResponseHeaders(200, response.getBytes().length);
            
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
