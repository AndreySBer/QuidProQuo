package hse.beryukhov.quidproquo;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Comments")
/*
A subclass of ParseObject class to represent the following database table structure:
* int id (hidden in Parse)
* int id_post
* string text
* ParseUser author
* timestamp posted (hidden)
 */
public class QuidComment extends ParseObject {
    public String getText() {
        return getString("text");
    }

    public void setText(String value) {
        put("text", value);
    }

    public ParseUser getAuthor() {
        return getParseUser("author");
    }

    public void setAuthor(ParseUser value) {
        put("author", value);
    }

    public String getPostID() {
        return getString("id_post");
    }

    public void setPostID(String value) {
        put("id_post", value);
    }

    public static ParseQuery<QuidComment> getQuery() {
        return ParseQuery.getQuery(QuidComment.class);
    }
}
