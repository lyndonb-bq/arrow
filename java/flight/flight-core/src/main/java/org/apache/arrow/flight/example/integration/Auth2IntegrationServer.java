package org.apache.arrow.flight.example.integration;

import com.google.common.base.Strings;
import org.apache.arrow.flight.*;
import org.apache.arrow.flight.auth2.BasicCallHeaderAuthenticator;
import org.apache.arrow.flight.auth2.CallHeaderAuthenticator;
import org.apache.arrow.flight.auth2.GeneratedBearerTokenAuthenticator;
import org.apache.arrow.flight.example.InMemoryStore;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.AutoCloseables;

import java.io.IOException;

public class Auth2IntegrationServer {
    private static final int PORT = 31337;
    private static final String USERNAME_1 = "flight1";
    private static final String PASSWORD_1 = "woohoo1";
    private static final String HOST = "localhost";
    private static final BufferAllocator ALLOCATOR = new RootAllocator(Long.MAX_VALUE);
    private static FlightServer server;

    static void launchServer() throws IOException, InterruptedException {
        final Location location = Location.forGrpcInsecure(HOST, PORT);
        final InMemoryStore store = new InMemoryStore(ALLOCATOR, location);
        server = FlightServer.builder(ALLOCATOR, location, store).headerAuthenticator(
                new GeneratedBearerTokenAuthenticator(
                    new BasicCallHeaderAuthenticator(Auth2IntegrationServer::validate))
        ).build().start();
        store.setLocation(Location.forGrpcInsecure("localhost", server.getPort()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\nExiting...");
                AutoCloseables.close(server, ALLOCATOR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        System.out.println("Server running on " + server.getLocation());
        server.awaitTermination();
    }

    private static CallHeaderAuthenticator.AuthResult validate(String username, String password) {
        if (Strings.isNullOrEmpty(username)) {
            throw CallStatus.UNAUTHENTICATED.withDescription("Credentials not supplied.").toRuntimeException();
        }
        final String identity;
        if (USERNAME_1.equals(username) && PASSWORD_1.equals(password)) {
            identity = USERNAME_1;
        } else {
            throw CallStatus.UNAUTHENTICATED.withDescription("Username or password is invalid.").toRuntimeException();
        }
        return () -> identity;
    }

    public static void main(String[] args) {
        try {
            launchServer();
        } catch (Exception e) {
            System.out.println("Launching server failed " + e);
        }
    }
}
