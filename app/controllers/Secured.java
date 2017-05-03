package controllers;

import models.User;
import org.apache.commons.lang3.StringUtils;
import play.mvc.*;
import play.mvc.Security;

/**
 * Created by abdoulbou on 26/04/17.
 */
public class Secured extends Security.Authenticator {
    @Override
    public String getUsername(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get("Authorization");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (StringUtils.isNotBlank(authTokenHeaderValues[0].replaceAll("Bearer ", "")))) {
            User user = User.findByAuthToken(authTokenHeaderValues[0].replaceAll("Bearer ", ""));
            if (user != null) {
                ctx.args.put("user", user);

                return user.getEmail();
            }
        }

        return null;

    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return unauthorized();
    }
}
