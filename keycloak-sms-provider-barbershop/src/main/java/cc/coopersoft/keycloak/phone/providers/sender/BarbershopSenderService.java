package cc.coopersoft.keycloak.phone.providers.sender;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;

public class BarbershopSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(BarbershopSenderService.class);
    private final Config.Scope config;
    private static final String SERVER_URL="http://localhost:4000";
    public BarbershopSenderService(KeycloakSession session, Config.Scope config) {
        super(session);
        this.config = config;
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        String token = config.get("token");

        logger.debug("Sending phone verification code via https");

        try {
            logger.info(String.format("To: %s >>> %s", phoneNumber, message));
            
            

        try {
            HttpClient httpclient = HttpClients.createDefault();
            SimpleHttp req = SimpleHttp.doGet("http://host.docker.internal:4000", httpclient).acceptJson();
            SimpleHttp.Response res = req.asResponse();

            System.out.println(req);


            if (res.getStatus() == 200) {
                logger.debug(
                        String.format("Sent phone verification code via https with message id %s", res.asJson()));
            } else {
                logger.warn("Failed to send SMS. Status code: " + res.getStatus());
            }
        }  catch (IOException e) {
            logger.warn("Error sending SMS", e);
        }
        } catch (Exception ex) {
            String msg = "Could not send message via localh https server";
            logger.error(msg, ex);
            throw new MessageSendException(msg, ex);
        }
    }

   

    @Override
    public void close() {
    }
}
