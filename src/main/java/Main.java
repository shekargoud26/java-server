import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  public static final String carr = "%s\r\n%s\r\n%s\r\n\r\n%s";
  public static String STATUS_OK = "HTTP/1.1 200 OK";
  public static String CONTENT_RESP_HEADER = "Content-Type: text/plain";

  public static void main(String[] args) {

    ExecutorService pool = Executors.newCachedThreadPool();
    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);

      while(true) {
        Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
        pool.submit(() -> handleConnection(clientSocket));
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static void handleConnection(Socket clientSocket) {
    try(clientSocket){
      {
        System.out.println("accepted new connection");
        int reqlength = clientSocket.getInputStream().available();

        String fullRequest = new String(clientSocket.getInputStream().readNBytes(reqlength));

        String requestHeader = fullRequest.split("\\r\\n")[0];
        System.out.println("req header: " + requestHeader);

        String METHOD = requestHeader.split(" ")[0];
        String URL = requestHeader.split(" ")[1];
        String PROTOCOL = requestHeader.split(" ")[2];

        List<String> requestLines = Arrays.asList(fullRequest.split("\\r\\n"));
        requestLines.stream().forEach(System.out::println);
        if("/".equals(URL))
          clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        else if (URL.startsWith("/user-agent")) {
          StringBuilder buffer = new StringBuilder();
          Arrays.asList(fullRequest.split("\\r\\n"))
                  .forEach(line -> {
                    if(line.startsWith("User-Agent"))
                      buffer.append(line.split(":")[1].trim());
                  });

          String userAgent = buffer.toString();
          System.out.println("userAgent: " + userAgent);

          String CONTENT_LEN_HEADER = "Content-Length: " + userAgent.length();
          System.out.println("Response: "
              + String.format(carr, STATUS_OK, CONTENT_RESP_HEADER,CONTENT_LEN_HEADER,userAgent));
          clientSocket.getOutputStream()
            .write(String.format(carr, STATUS_OK, CONTENT_RESP_HEADER,CONTENT_LEN_HEADER,userAgent).getBytes());
        } else if(URL.startsWith("/echo")){
          String echoStr = URL.split("/")[URL.split("/").length - 1];
          String CONTENT_LEN_HEADER = "Content-Length: " + echoStr.length();
          clientSocket.getOutputStream()
            .write(String.format(carr, STATUS_OK, CONTENT_RESP_HEADER,CONTENT_LEN_HEADER,echoStr).getBytes());
        }
        else
          clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        clientSocket.close();
      }
    } catch (IOException ex) {
      System.out.println("Something went wrong" + ex.fillInStackTrace());
    }
  }
}
