package factory;

import form.RegisterForm;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Created by abdoulbou on 26/04/17.
 */
public class UserFactory {

    private static final String DEFAULT_AVATAR = "/assets/images/avatar.png";

    public static void createUser(RegisterForm form) {
        User user = new User();
        user.setEmail(form.getEmail());
        user.setPassword(BCrypt.hashpw(form.getPassword(), BCrypt.gensalt()));
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setAvatarUrl(DEFAULT_AVATAR);
        user.save();
    }
}
