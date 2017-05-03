package form;

import play.data.validation.Constraints;

/**
 * Created by abdoulbou on 26/04/17.
 */
public class GameForm {
    @Constraints.Required
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
