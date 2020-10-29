package cc.coopersoft.keycloak.authenticators.sms;

import cc.coopersoft.keycloak.authenticators.BaseDirectGrantAuthenticator;
import cc.coopersoft.keycloak.providers.sms.constants.TokenCodeType;
import cc.coopersoft.keycloak.providers.sms.jpa.TokenCode;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Optional;


public class AuthenticationCodeAuthenticator extends BaseDirectGrantAuthenticator {

//    public static final String FAILURE_CHALLENGE = "_failure-challenge";

    private static final Logger logger = Logger.getLogger(AuthenticationCodeAuthenticator.class);

    private final KeycloakSession session;

    public AuthenticationCodeAuthenticator(KeycloakSession session) {
        this.session = session;
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    protected RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction("VERIFICATION_CODE_GRANT_CONFIG");
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (!validateVerificationCode(context)) {
            context.getEvent().user(context.getUser());
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);

            Response challenge = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        context.success();
    }

    private boolean validateVerificationCode(AuthenticationFlowContext context) {
        String phoneNumber = Optional.ofNullable(context.getHttpRequest().getDecodedFormParameters().getFirst("phone_number")).orElse(
                context.getHttpRequest().getDecodedFormParameters().getFirst("phoneNumber"));
        String code = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

//        String kind = null;
//        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
//        if (authenticatorConfig != null && authenticatorConfig.getConfig() != null) {
//            kind = Optional.ofNullable(context.getAuthenticatorConfig().getConfig().get(KIND)).orElse("");
//        }

        try {

            TokenCode entity = getEntityManager()
                    .createNamedQuery("ongoingProcess", TokenCode.class)
                    .setParameter("realmId", context.getRealm().getId())
                    .setParameter("phoneNumber", phoneNumber)
                    .setParameter("now", new Date(), TemporalType.TIMESTAMP)
                    .setParameter("type", TokenCodeType.OTP_MESSAGE.name())
                    .getSingleResult();

            logger.info(String.format("Grant authenticator valid code %s = %s", entity.getCode(), code));

            return entity.getCode().equals(code);
//            Integer veriCode = getEntityManager().createNamedQuery("VerificationCode.validateVerificationCode", Integer.class)
//                    .setParameter("realmId", getRealm().getId())
//                    .setParameter("phoneNumber", phoneNumber)
//                    .setParameter("code", code)
//                    .setParameter("now", new Date(), TemporalType.TIMESTAMP)
//                    .setParameter("kind", kind)
//                    .getSingleResult();
//            if (veriCode == 1) {
//                return true;
//            }
        }
        catch (NoResultException err){ }
        return false;
    }

}