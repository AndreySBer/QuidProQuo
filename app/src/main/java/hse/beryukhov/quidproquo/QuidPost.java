package hse.beryukhov.quidproquo;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


@ParseClassName("Posts")
/*
A subclass of ParseObject class to represent the following database table structure:
* int id (hidden in Parse)
* string name
* ParseUser author
* string text
* ParseGeoObject location
* PostState state
* timestamp posted (hidden)
* photos, boolean is_promo, string promo_properties ** not implemented at this stage
 */
public class QuidPost extends ParseObject {
    public String getName() {
        return getString("name");
    }

    public void setName(String value) {
        put("name", value);
    }

    public ParseUser getAuthor() {
        return getParseUser("author");
    }

    public void setAuthor(ParseUser value) {
        put("author", value);
    }

    public String getText() {
        return getString("text");
    }

    public void setText(String value) {
        put("text", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }


    public enum PostState {
        ASK("ASK"), SUGGEST("SUGGEST"), ADVERT("ADVERT");
        private String abr;

        private PostState(String abr) {
            this.abr = abr;
        }

        public String getString() {
            return abr;
        }
    }

    public PostState getState() {
        return PostState.valueOf(PostState.class, getString("state"));
    }

    public void setState(PostState value) {
        put("state", value.getString());
    }

    public static ParseQuery<QuidPost> getQuery() {
        return ParseQuery.getQuery(QuidPost.class);
    }
}
