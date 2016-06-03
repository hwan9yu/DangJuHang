package ku.im.dangjuhang;

import android.app.Activity;
import android.content.Context;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Gyu on 2016-05-23.
 */
public class Client {
    OAuthLogin mOAuthLoginModule;
    OAuthLoginHandler handler;
    Context context;
    DefaultHttpClient httpClient = new DefaultHttpClient();

    class MyAuthHandler extends OAuthLoginHandler
    {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginModule.getAccessToken(context);
                String refreshToken = mOAuthLoginModule.getRefreshToken(context);
                long expiresAt = mOAuthLoginModule.getExpiresAt(context);
                String tokenType = mOAuthLoginModule.getTokenType(context);
                //   mOauthAT.setText(accessToken);
                //   mOauthRT.setText(refreshToken);
                //   mOauthExpires.setText(String.valueOf(expiresAt));
                //   mOauthTokenType.setText(tokenType);
                //   mOAuthState.setText(mOAuthLoginModule.getState(getApplicationContext()).toString());
                String info = mOAuthLoginModule.requestApi(context, accessToken, "https://apis.naver.com/nidlogin/nid/getUserProfile.xml");
                String in = info;
                info = info + "";

            } else {
                String errorCode = mOAuthLoginModule.getLastErrorCode(context).getCode();
                String errorDesc = mOAuthLoginModule.getLastErrorDesc(context);
//                Toast.makeText(context, "errorCode:" + errorCode
//                        + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    }

    void NaverLogin(Activity act)
    {
        context = act.getApplicationContext();
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
                context
                , "S6nKrH6CGNXyt2TKEYZY"
                , "DJtUt0iCNx"
                , "DangJuHang"
                // SDK 4.1.4 버전부터는 OAUTH_CALLBACK_INTENT변수를 사용하지 않습니다.
        );

        handler = new MyAuthHandler();
        mOAuthLoginModule.startOauthLoginActivity(act, handler);
        new RequestApiTask().execute();
    }

    class RequestPlace extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = null;
            try {
                url = "https://openapi.naver.com/v1/search/local.xml?query="+ URLEncoder.encode(params[0], "UTF-8") +"&display=10&start=1&sort=vote";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Content-Type", "application/json");
            httpget.setHeader("Accept", "*/*");
            httpget.setHeader("X-Naver-Client-Id", "S6nKrH6CGNXyt2TKEYZY");
            httpget.setHeader("X-Naver-Client-Secret", "DJtUt0iCNx");
            String result = "";
            try {
                HttpResponse response = httpClient.execute(httpget);
                String line = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                while((line = br.readLine()) != null){
                    result += line;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    };


    class RequestCoord extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = null;
            try {
                url = "https://openapi.naver.com/v1/map/geocode?encoding=utf-8&coord=latlng&output=json&query=" + URLEncoder.encode(params[0], "EUC-KR");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Content-Type", "application/json");
            httpget.setHeader("Accept", "*/*");
            httpget.setHeader("X-Naver-Client-Id", "S6nKrH6CGNXyt2TKEYZY");
            httpget.setHeader("X-Naver-Client-Secret", "DJtUt0iCNx");
            String result = "";
            try {
                HttpResponse response = httpClient.execute(httpget);
                String line = null;
                     BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                     while((line = br.readLine()) != null){
                             result += line;
                     }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    };

    boolean SearchCoord(String search, Float lat, Float longi)
    {
        new RequestPlace().execute(search);
        new RequestCoord().execute(search);
        return true;
    }

    String XMLParsing(String content)
    {
        String result = "";
        XmlPullParserFactory factory = null;
        XmlPullParser xpp;
        int eventType;
        try {
            factory = XmlPullParserFactory.newInstance();
            xpp = factory.newPullParser();
            xpp.setInput(new StringReader(content));
            eventType = xpp.getEventType();
            boolean bSet = false;
            String tag_name = "default";
            while(eventType != XmlPullParser.END_DOCUMENT){
                if(eventType==XmlPullParser.START_DOCUMENT){
                    ;
                }else if(eventType== XmlPullParser.START_TAG){
                    tag_name = xpp.getName();
                    //if(tag_name.equals("id")||tag_name.equals("name")|| tag_name.equals("birthday")|| tag_name.equals("gender")|| tag_name.equals("age"))
                    {
                        bSet=true;
                    }
                }else if(eventType==XmlPullParser.TEXT){
                    if(bSet){
                        String data = xpp.getText();
                        result += (tag_name + " : " + data+"\n");
                        bSet = false;
                    }
                }else if(eventType==XmlPullParser.END_TAG){
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected String doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/getUserProfile.xml";
            String at = mOAuthLoginModule.getAccessToken(context);

            String content = mOAuthLoginModule.requestApi(context, at, url);
            String result = XMLParsing(content);
            return result;
        }
        protected void onPostExecute(String content) {
//            Toast.makeText(context, content, Toast.LENGTH_LONG).show();
        }
    }
}
