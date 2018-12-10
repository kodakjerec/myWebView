package sc.mobile.investment.webview;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.PluginResult;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class InvestmentWebView extends CordovaPlugin {
    public final String ACTION_WEB_URL = "web_url";
    public static CallbackContext myCallbackContent = null;
    public static boolean sslPinning;

    private final int CLICK_RESULT = 111;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d("InvestmentWebView", "execute :: " + action + ", args: " + args.toString());

        JSONObject jsonObject = new JSONObject("{}");
        if(args.length()>0){
            jsonObject = args.getJSONObject(0);
        }

        switch (action)
        {
            case ACTION_WEB_URL:
                this.myCallbackContent = callbackContext;

                // 開啟WebView
                String url = jsonObject.get("url").toString();
                String title = jsonObject.get("title").toString();
                int type = jsonObject.getInt("type");
                String noteButtonString = jsonObject.get("noteButtonString").toString();
                sslPinning = jsonObject.getBoolean("sslPinning");

                Log.d("InvestmentWebView", "jsonObject :: " + jsonObject.toString() + "  url :: " + url);
                Intent intent = new Intent(cordova.getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, url);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
                intent.putExtra(WebViewActivity.EXTRA_TYPE, type);
                intent.putExtra(WebViewActivity.EXTRA_NOTE_BUTTON_STRING, noteButtonString);
                cordova.getActivity().startActivityForResult(intent, CLICK_RESULT);
                return true;
            case "close":
                // 通知 webView 關閉
                try {
                    WebViewActivity.myWebViewActivity.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case "remindAlertDialog":
                // 開啟WebView內部的alertDialog
                String remindAlertDialog_message = jsonObject.get("message").toString();
                String remindAlertDialog_title = jsonObject.get("title").toString();
                String remindAlertDialog_buttonName = jsonObject.get("buttonName").toString();
                WebViewActivity.remindAlertDialog( remindAlertDialog_message,remindAlertDialog_title,remindAlertDialog_buttonName,callbackContext );
                return true;
            case "language":
                WebViewActivity.languageJson = args.getJSONObject( 0 );
                WebViewActivity.locale = args.getJSONObject( 0 ).get("locale").toString();
                return true;
            case "openInSystem":
                openExternal(args.getJSONObject( 0 ).get("url").toString());
                return true;
        }

        return false;
    }

    // 發送更新給前台UI, 提供給 webView 專用
    public static void sendUpdate(JSONObject obj){
        if (obj != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            myCallbackContent.sendPluginResult(result);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            myCallbackContent.sendPluginResult(result);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CLICK_RESULT) {
            try {
                this.cordova.getActivity().finish();
            } catch (Throwable throwable) {
//                throwable.printStackTrace();
            }
        }
    }

    public String openExternal(String url) {
        try {
            Intent intent = null;
            intent = new Intent(Intent.ACTION_VIEW);
            // Omitting the MIME type for file: URLs causes "No Activity found to handle Intent".
            // Adding the MIME type to http: URLs causes them to not be handled by the downloader.
            Uri uri = Uri.parse(url);
            if ("file".equals(uri.getScheme())) {
                intent.setDataAndType(uri, webView.getResourceApi().getMimeType(uri));
            } else {
                intent.setData(uri);
            }
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, cordova.getActivity().getPackageName());
            this.cordova.getActivity().startActivity(intent);
            return "";
            // not catching FileUriExposedException explicitly because buildtools<24 doesn't know about it
        } catch (java.lang.RuntimeException e) {
            LOG.d("InvestmentWebView", "InAppBrowser: Error loading url "+url+":"+ e.toString());
            return e.toString();
        }
    }
}