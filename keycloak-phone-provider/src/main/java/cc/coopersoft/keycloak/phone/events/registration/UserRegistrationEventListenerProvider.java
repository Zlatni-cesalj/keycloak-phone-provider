package cc.coopersoft.keycloak.phone.events.registration;

import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class UserRegistrationEventListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(UserRegistrationEventListenerProvider.class);

    private final KeycloakSession keycloakSession;

    public UserRegistrationEventListenerProvider(KeycloakSession session) {
        this.keycloakSession = session;
    }

    @Override
    public void onEvent(Event event) {
        if (EventType.REGISTER.equals(event.getType())) {
            RealmModel realm = keycloakSession.getContext().getRealm();
            UserModel user = keycloakSession.users().getUserById(realm, event.getUserId());

            String phoneNumber = user.getUsername();
            String id = user.getId();
            HashMap<String, Object> body = new HashMap<String, Object>();
            body.put("keycloakId", id);
            body.put("phone", phoneNumber);

            log.info("registering user with phone number: " + phoneNumber);
            log.info("private key is: " + System.getenv("REQUEST_PRIVATE_KEY"));

            HttpClient httpclient = HttpClients.createDefault();
            SimpleHttp.doPost("http://host.docker.internal:4001/client/registered", httpclient)
            .header("authorization", System.getenv("REQUEST_PRIVATE_KEY"))
            .json(body);
       
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {

    }
}