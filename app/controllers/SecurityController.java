package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;
import factory.UserFactory;
import form.LoginForm;
import form.RegisterForm;
import models.User;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import play.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;

public class SecurityController extends Controller {

    @Inject
    private FormFactory formFactory;

    public static final String AUTH_TOKEN = "authToken";

    public static User getUser() {
        return (User)Http.Context.current().args.get("user");
    }

    public Result authenticate() {
        Form<LoginForm> loginForm = formFactory.form(LoginForm.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(loginForm.errorsAsJson());
        }
        session().clear();
        User user = User.findByEmail(loginForm.get().email);
        if (user != null && BCrypt.checkpw(loginForm.get().password, user.getPassword())) {
            String authToken = user.createToken();
            ObjectNode authTokenJson = Json.newObject();
            authTokenJson.put(AUTH_TOKEN, authToken);
            response().setCookie(Http.Cookie.builder(AUTH_TOKEN, authToken).withSecure(ctx().request().secure()).build());

            return ok(Json.toJson(user)).as(MediaType.JSON_UTF_8.toString());
        }

        return badRequest();
    }

    @Security.Authenticated(Secured.class)
    public Result logout() {
        response().discardCookie(AUTH_TOKEN);
        getUser().deleteAuthToken();

        return ok();
    }

    public Result register() {
        Form<RegisterForm> registerForm = formFactory.form(RegisterForm.class).bindFromRequest();
        if (registerForm.hasErrors()) {
            return badRequest(registerForm.errorsAsJson());
        } else {
            RegisterForm userForm = registerForm.get();
            UserFactory.createUser(userForm);
            User user = User.findByEmail(userForm.getEmail());

            String authToken = user.createToken();
            ObjectNode authTokenJson = Json.newObject();
            authTokenJson.put(AUTH_TOKEN, authToken);
            response().setCookie(Http.Cookie.builder(AUTH_TOKEN, authToken).withSecure(ctx().request().secure()).build());

            return ok(Json.toJson(user)).as(MediaType.JSON_UTF_8.toString());
        }
    }
}
