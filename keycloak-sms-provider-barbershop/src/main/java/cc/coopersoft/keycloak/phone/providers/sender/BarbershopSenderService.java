package cc.coopersoft.keycloak.phone.providers.sender;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jboss.logging.Logger;
import org.keycloak.Config;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;

public class BarbershopSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(BarbershopSenderService.class);
    private final Config.Scope config;

    @SuppressWarnings("deprecation")
    public BarbershopSenderService(String realmDisplay, Config.Scope config) {
        super(realmDisplay);
        this.config = config;
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        String token = config.get("token");

        logger.debug("Sending phone verification code via aws sns");

        try {
            
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:5000/send-sms"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(
                        "phoneNumber=" + phoneNumber + "&message=" + message))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.debug(
                        String.format("Sent phone verification code via aws sns with message id %s", response.body()));
            } else {
                logger.warn("Failed to send SMS. Status code: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while sending SMS", e);
        } catch (IOException e) {
            logger.warn("Error sending SMS", e);
        }
        } catch (Exception ex) {
            String msg = "Could not send message via aws sns";
            logger.error(msg, ex);
            throw new MessageSendException(msg, ex);
        }
    }

   

    @Override
    public void close() {
    }
}
