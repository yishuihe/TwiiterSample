package com.zhixin.twittersample.model.twitter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Zhixin on 4/20/2015.
 */
public class TwitterManager {
    private static final String TAG = TwitterManager.class.getSimpleName();
    private static TwitterManager mInstance = null;
    private static Context mContext = null;
    private static String mSearchTag = null;

    public interface LoadTweetListener {
        public void onTweetLoaded(ArrayList<Tweet> tweet);
    }
    private LoadTweetListener mLoadTweetListener;

    public static TwitterManager createInstance(Context ctx, String searchTag) {
        if (mInstance == null) {
            mInstance = new TwitterManager();
        }
        mContext = ctx;
        mSearchTag = searchTag;
        return mInstance;
    }


    public void downloadTweets(LoadTweetListener listener) {
        if(listener != null) {
            mLoadTweetListener = listener;
        }

        ConnectivityManager connMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "downloadTweets: trigger async task to download tweets");
            new DownloaderTask().execute(mSearchTag);

        } else {
            Log.e(TAG, "downloadTweets: Network not availabe ");
        }
    }

    private class DownloaderTask extends AsyncTask<String, Void, String> {
        final static String CONSUMER_KEY = "CtS9iDRcO62QEmyHc1yYMIq1J";
        final static String CONSUMER_SECRET = "2unErUNxTyk6PYgUxTIkosiK93X75kn4uZmOEtdrq3llqlKPXO";
        final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
        final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=";

        @Override
        protected String doInBackground(String... screenNames) {
            String result = null;

            if (screenNames.length > 0) {
                Log.d(TAG, "doInBackground: lets go download now");
                result = getTwitterStream(screenNames[0]);
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            Twitter twits = jsonToTwitter(result);

            // lets write the results to the console as well
            ArrayList<Tweet> ret = new ArrayList<>();
            for (Tweet tweet : twits) {
                Log.i(TAG, tweet.getText());
//                ret.add(tweet.getText());
                ret.add(tweet);
            }

            // send the tweets to the adapter for rendering
            if(mLoadTweetListener != null) {
                mLoadTweetListener.onTweetLoaded(ret);
            }
//            ArrayAdapter<Tweet> adapter = new ArrayAdapter<Tweet>(activity, android.R.layout.simple_list_item_1, twits);
//            setListAdapter(adapter);

        }

        private String getTwitterStream(String screenName) {
            String results = null;

            // Step 1: Encode consumer key and secret
            try {
                // URL encode the consumer key and secret
                String urlApiKey = URLEncoder.encode(CONSUMER_KEY, "UTF-8");
                String urlApiSecret = URLEncoder.encode(CONSUMER_SECRET, "UTF-8");

                // Concatenate the encoded consumer key, a colon character, and the
                // encoded consumer secret
                String combined = urlApiKey + ":" + urlApiSecret;

                // Base64 encode the string
                String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

                // Step 2: Obtain a bearer token
                HttpPost httpPost = new HttpPost(TwitterTokenURL);
                httpPost.setHeader("Authorization", "Basic " + base64Encoded);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
                String rawAuthorization = getResponseBody(httpPost);
                Authenticated auth = jsonToAuthenticated(rawAuthorization);

                // Applications should verify that the value associated with the
                // token_type key of the returned object is bearer
                if (auth != null && auth.token_type.equals("bearer")) {

                    // Step 3: Authenticate API requests with bearer token
                    HttpGet httpGet = new HttpGet(TwitterStreamURL + screenName);

                    // construct a normal HTTPS request and include an Authorization
                    // header with the value of Bearer <>
                    httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                    httpGet.setHeader("Content-Type", "application/json");
                    // update the results with the body of the response
                    results = getResponseBody(httpGet);
                }
            } catch (UnsupportedEncodingException ex) {
            } catch (IllegalStateException ex1) {
            }
            return results;
        }

        private String getResponseBody(HttpRequestBase request) {
            StringBuilder sb = new StringBuilder();
            try {

                DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
                Log.d(TAG, "@@@@@@@@~~~~ request is: " + request.getRequestLine());
                HttpResponse response = httpClient.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                String reason = response.getStatusLine().getReasonPhrase();

                if (statusCode == 200) {

                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();

                    BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    String line = null;
                    while ((line = bReader.readLine()) != null) {
                        sb.append(line);
                    }
                } else {
                    sb.append(reason);
                }
            } catch (UnsupportedEncodingException ex) {
            } catch (ClientProtocolException ex1) {
            } catch (IOException ex2) {
            }
            return sb.toString();
        }

        // converts a string of JSON data into a Twitter object
        private Twitter jsonToTwitter(String result) {
            Twitter twits = null;
            if (result != null && result.length() > 0) {
                try {
                    Gson gson = new Gson();
                    Log.d(TAG, "jsonToTwitter: " + gson.toString());
                    twits = gson.fromJson(result, Twitter.class);
                } catch (IllegalStateException ex) {
                    // just eat the exception
                }
            } else {
                Log.d(TAG, "jsonToTwitter: resul length is zero?: ");
            }
            
            return twits;
        }

        // convert a JSON authentication object into an Authenticated object
        private Authenticated jsonToAuthenticated(String rawAuthorization) {
            Authenticated auth = null;
            if (rawAuthorization != null && rawAuthorization.length() > 0) {
                try {
                    Gson gson = new Gson();
                    auth = gson.fromJson(rawAuthorization, Authenticated.class);
                } catch (IllegalStateException ex) {
                    // just eat the exception
                }
            }
            return auth;
        }
    }

}
