package ch.heigvd.res.examples;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A very simple example of TCP server. When the server starts, it binds a
 * server socket on any of the available network interfaces and on port 2205. It
 * then waits until one (only one!) client makes a connection request. When the
 * client arrives, the server does not even check if the client sends data. It
 * simply writes the current time, every second, during 15 seconds.
 *
 * To test the server, simply open a terminal, do a "telnet localhost 2205" and
 * see what you get back. Use Wireshark to have a look at the transmitted TCP
 * segments.
 *
 * @author Olivier Liechti
 */
public class CalculatorServer {

  static final Logger LOG = Logger.getLogger(CalculatorServer.class.getName());

  private final int LISTEN_PORT = 2020;

  /**
   * This method does the entire processing.
   */
  public void start() {
    LOG.info("Starting server...");

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    BufferedReader reader = null;
    PrintWriter writer = null;

    try {
      LOG.log(Level.INFO, "Creating a server socket and binding it on any of the available network interfaces and on port {0}", new Object[]{Integer.toString(LISTEN_PORT)});
      serverSocket = new ServerSocket(LISTEN_PORT);
      logServerSocketAddress(serverSocket);

      while (true) {
        LOG.log(Level.INFO, "Waiting (blocking) for a connection request on {0} : {1}", new Object[]{serverSocket.getInetAddress(), Integer.toString(serverSocket.getLocalPort())});
        clientSocket = serverSocket.accept();

        LOG.log(Level.INFO, "A client has arrived. We now have a client socket with following attributes:");
        logSocketAddress(clientSocket);

        LOG.log(Level.INFO, "Getting a Reader and a Writer connected to the client socket...");
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream());

        char operand;
        int leftOperator = 0;
        int rightOperator = 0;
        int result = 0;

        LOG.log(Level.INFO,"starting the state communication");
        //first we need the operand
        operand = ((char) reader.read());

        if(operand != '+' && operand != '-')
          throw new InterruptedException("not an understandable operand");

        LOG.log(Level.INFO, "Here we need the first operator");
        writer.write("\nOK : give me the left operand");

        leftOperator = reader.read();

        LOG.log(Level.INFO, "Here we need the second operator");
        writer.write("\nOK : give me the right operand");

        rightOperator = reader.read();

        switch (operand){
          case '+':
            result = leftOperator + rightOperator;
          break;
          case '-':
            result = leftOperator - rightOperator;
            break;
        }

        writer.write("\nYour result is : " + result);

        reader.close();
        writer.close();
        clientSocket.close();

      }

    } catch (IOException | InterruptedException ex) {
      LOG.log(Level.SEVERE, ex.getMessage());
    } finally {
      LOG.log(Level.INFO, "We are done. Cleaning up resources, closing streams and sockets...");
      try {
        reader.close();
      } catch (IOException ex) {
        Logger.getLogger(CalculatorServer.class.getName()).log(Level.SEVERE, null, ex);
      }
      writer.close();
      try {
        clientSocket.close();
      } catch (IOException ex) {
        Logger.getLogger(CalculatorServer.class.getName()).log(Level.SEVERE, null, ex);
      }
      try {
        serverSocket.close();
      } catch (IOException ex) {
        Logger.getLogger(CalculatorServer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

  }

  /**
   * A utility method to print server socket information
   *
   * @param serverSocket the socket that we want to log
   */
  private void logServerSocketAddress(ServerSocket serverSocket) {
    LOG.log(Level.INFO, "       Local IP address: {0}", new Object[]{serverSocket.getLocalSocketAddress()});
    LOG.log(Level.INFO, "             Local port: {0}", new Object[]{Integer.toString(serverSocket.getLocalPort())});
    LOG.log(Level.INFO, "               is bound: {0}", new Object[]{serverSocket.isBound()});
  }

  /**
   * A utility method to print socket information
   *
   * @param clientSocket the socket that we want to log
   */
  private void logSocketAddress(Socket clientSocket) {
    LOG.log(Level.INFO, "       Local IP address: {0}", new Object[]{clientSocket.getLocalAddress()});
    LOG.log(Level.INFO, "             Local port: {0}", new Object[]{Integer.toString(clientSocket.getLocalPort())});
    LOG.log(Level.INFO, "  Remote Socket address: {0}", new Object[]{clientSocket.getRemoteSocketAddress()});
    LOG.log(Level.INFO, "            Remote port: {0}", new Object[]{Integer.toString(clientSocket.getPort())});
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");

    CalculatorServer server = new CalculatorServer();
    server.start();
  }

}