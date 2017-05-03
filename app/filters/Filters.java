package filters;

import play.mvc.EssentialFilter;
import play.filters.cors.CORSFilter;
import play.http.DefaultHttpFilters;

import javax.inject.Inject;

/**
 * Created by abdoulbou on 28/04/17.
 */
public class Filters extends DefaultHttpFilters {
    @Inject public Filters(CORSFilter corsFilter) {
        super(corsFilter);
    }
}