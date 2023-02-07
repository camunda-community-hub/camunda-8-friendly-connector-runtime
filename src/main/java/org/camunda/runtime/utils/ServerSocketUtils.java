package org.camunda.runtime.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.camunda.runtime.service.SecretsService;
import org.camunda.runtime.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSocketUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ServerSocketUtils.class);

  private ServerSocket server;
  private SecretsService secretsService;
  private SyncService syncService;
  private static final Map<Integer, ServerSocketUtils> INSTANCES = new HashMap<>();

  private ServerSocketUtils(
      ServerSocket server, SecretsService secretsService, SyncService syncService) {
    this.server = server;
    this.secretsService = secretsService;
    this.syncService = syncService;
  }

  public static ServerSocketUtils getInstance(
      int port, SecretsService secretsService, SyncService syncService) throws IOException {
    if (!INSTANCES.containsKey(port)) {
      ServerSocket server = new ServerSocket(port);
      ServerSocketUtils instance = new ServerSocketUtils(server, secretsService, syncService);
      INSTANCES.put(port, instance);
    }
    return INSTANCES.get(port);
  }

  public void startListening() {
    new ThreadedListener(server).start();
  }

  private class ThreadedListener extends Thread {
    private ServerSocket server;
    // Constructor
    public ThreadedListener(ServerSocket server) {
      this.server = server;
    }

    @Override
    public void run() {
      while (true) {
        Socket socket = null;
        DataInputStream socketInputStream = null;
        DataOutputStream socketOutputStream = null;
        try {
          socket = server.accept();
          socketInputStream = new DataInputStream(socket.getInputStream());
          socketOutputStream = new DataOutputStream(socket.getOutputStream());
          new ClientHandler(socketInputStream, socketOutputStream).start();
        } catch (IOException e) {
          LOG.error("socket communication exception", e);
          try {
            if (socket != null) socket.close();
            if (socketInputStream != null) socketInputStream.close();
            if (socketOutputStream != null) socketOutputStream.close();
          } catch (IOException e2) {
            LOG.error("closing socket exception", e);
          }
        }
      }
    }
  }

  private class ClientHandler extends Thread {
    final DataInputStream socketInputStream;
    final DataOutputStream socketOutputStream;
    boolean running = true;

    // Constructor
    public ClientHandler(DataInputStream socketInputStream, DataOutputStream socketOutputStream) {
      this.socketInputStream = socketInputStream;
      this.socketOutputStream = socketOutputStream;
    }

    @Override
    public void run() {
      while (this.running) {
        try {
          handleIncomingMessage(socketInputStream, socketOutputStream);
        } catch (IOException e) {
          this.running = false;
        }
      }
    }

    private void handleIncomingMessage(
        DataInputStream socketInputStream, DataOutputStream socketOutputStream) throws IOException {
      String message = read();
      if (message.startsWith(SyncService.HEALTHCHECK)) {
        write(SyncService.HEALTHCHECK_RESPONSE);
      } else if (message.startsWith(SyncService.SHARE_SECRETS)) {
        String client = message.substring(SyncService.SHARE_SECRETS.length());
        shareSecrets(client);
      } else if (message.startsWith(SyncService.UPDATE_SECRETS)) {
        String encryptedSecrets = message.substring(SyncService.UPDATE_SECRETS.length());
        syncService.updateSecrets(encryptedSecrets);
      }
    }

    private void shareSecrets(String member) throws IOException {
      byte[] publicKey = syncService.getMember(member);
      String secrets = JsonUtils.toJson(secretsService.getSecrets());
      String encryptedSecrets = CryptoUtils.encrypt(secrets, publicKey);
      write(encryptedSecrets);
    }

    private void write(String message) throws IOException {
      socketOutputStream.writeUTF(message);
    }

    private String read() throws IOException {
      return socketInputStream.readUTF();
    }
  }
}
