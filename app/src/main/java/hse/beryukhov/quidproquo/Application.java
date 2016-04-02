package hse.beryukhov.quidproquo;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.http.ParseHttpRequest;
import com.parse.http.ParseHttpResponse;
import com.parse.http.ParseNetworkInterceptor;
//import com.vk.sdk.VKAccessToken;
//import com.vk.sdk.VKAccessTokenTracker;
//import com.vk.sdk.VKSdk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

//import com.facebook.FacebookSdk;

public class Application extends android.app.Application {
    //public static final String APPTAG = "QuidProQuo";
    // Used to pass location from MainActivity to NewPostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";
    public static final String INTENT_EXTRA_SEARCH_DISTANCE = "searchDist";
    public static final String TELEGRAM_ACCOUNT="TelegramUsername";
    public static final String ISPREMIUM="isPremium";
    public static final String USER_PHOTO ="userPhoto" ;

    /*VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                // VKAccessToken is invalid
            }
        }
    };*/

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(QuidPost.class);
        ParseObject.registerSubclass(QuidComment.class);
        Parse.addParseNetworkInterceptor(new ParseLogInterceptor());
        Parse.initialize(this);
        //vkAccessTokenTracker.startTracking();
        //VKSdk.initialize(this);
    }

    //bugfixer for Android API<19
    private class ParseLogInterceptor implements ParseNetworkInterceptor {
        @Override
        public ParseHttpResponse intercept(Chain chain) throws IOException {
            ParseHttpRequest request = chain.getRequest();

            ParseHttpResponse response = chain.proceed(request);

            // Consume the response body
            ByteArrayOutputStream responseBodyByteStream = new ByteArrayOutputStream();
            int n;
            byte[] buffer = new byte[1024];
            while ((n = response.getContent().read(buffer, 0, buffer.length)) != -1) {
                responseBodyByteStream.write(buffer, 0, n);
            }
            final byte[] responseBodyBytes = responseBodyByteStream.toByteArray();
            Log.i("Response_Body", new String(responseBodyBytes));

            // Make a new response before return the response
            response = new ParseHttpResponse.Builder(response)
                    .setContent(new ByteArrayInputStream(responseBodyBytes))
                    .build();

            return response;
        }
    }
}
