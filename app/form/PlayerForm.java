package form;

import play.data.validation.Constraints;

/**
 * Created by abdoulbou on 27/04/17.
 */
public class PlayerForm {
    @Constraints.Required
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
