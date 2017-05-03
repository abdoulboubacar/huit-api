package controllers;


import play.mvc.Controller;
import play.mvc.Result;

import views.html.*;

/**
 * Created by abdoulbou on 26/04/17.
 */
public class SwaggerController extends Controller {

    public Result index() {
        return ok();
    }

}
