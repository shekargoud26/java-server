import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    String STATUS_OK = "HTTP/1.1 200 OK";
    String CONTENT_RESP_HEADER = "Content-Type: text/plain";
    
    try {
      serverSocket = new ServerSocket(4221);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      clientSocket = serverSocket.accept(); // Wait for connection from client.
      System.out.println("accepted new connection");
      int reqlength = clientSocket.getInputStream().available();
      // BufferedReader br = new BufferedReader(new InputStreamReader());
      String fullRequest = new String(clientSocket.getInputStream().readNBytes(reqlength));

      String requestStatus = fullRequest.split("\\r\\n")[0];
      System.out.println("req status: " + requestStatus);

      String METHOD = requestStatus.split(" ")[0];
      String URL = requestStatus.split(" ")[1];
      String PROTOCOL = requestStatus.split(" ")[2];
      


      // List<String> requestLines = fullRequest.split("\\r\\n");
      System.out.println("parsed request");
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
        System.out.println("Response: " + String.format("%s\r\n%s\r\n%s\r\n\r\n%s", STATUS_OK, CONTENT_RESP_HEADER,CONTENT_LEN_HEADER,userAgent));
        clientSocket.getOutputStream().write(String.format("%s\r\n%s\r\n%s\r\n\r\n%s", STATUS_OK, CONTENT_RESP_HEADER,CONTENT_LEN_HEADER,userAgent).getBytes());  
      } else if(URL.startsWith("/echo")){
        String echoStr = URL.split("/")[URL.split("/").length - 1];
        String CONTENT_LEN_HEADER = "Content-Length: " + echoStr.length();
        clientSocket.getOutputStream().write(String.format("%s\r\n%s\r\n%s\r\n\r\n%s", STATUS_OK, CONTENT_RESP_HEADER,CONTENT_LEN_HEADER,echoStr).getBytes());
      }
      else
        clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      clientSocket.close();
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static List<String> parseRequest(BufferedReader br) throws IOException {
    
    String line = br.readLine();
    List<String> lines = new ArrayList<>();
    while(line != null){
      System.out.println(line);

      lines.add( line);
      line = br.readLine();
    }
    return lines;
  }
}
