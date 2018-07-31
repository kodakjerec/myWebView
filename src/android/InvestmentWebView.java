package sc.mobile.investment.webview;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.PluginResult;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class InvestmentWebView extends CordovaPlugin {
    public final String ACTION_WEB_URL = "web_url";
    public static CallbackContext myCallbackContent = null;

    private final int CLICK_RESULT = 111;
    private Activity activity = null;
    private Intent intent = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d("InvestmentWebView", "execute :: " + action + ", args: " + args.toString());
        this.myCallbackContent = callbackContext;
        this.activity = this.cordova.getActivity();
        JSONObject jsonObject = new JSONObject("{}");
        if(args.length()>0){
            jsonObject = args.getJSONObject(0);
        }

        switch (action)
        {
            case ACTION_WEB_URL:
                // 開啟WebView
                String url = jsonObject.get("url").toString();
                String title = jsonObject.get("title").toString();
                int type = jsonObject.getInt("type");
                String noteButtonString = jsonObject.get("noteButtonString").toString();

                Log.d("InvestmentWebView", "jsonObject :: " + jsonObject.toString() + "  url :: " + url);
                intent = new Intent(this.activity, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, url);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
                intent.putExtra(WebViewActivity.EXTRA_TYPE, type);
                intent.putExtra(WebViewActivity.EXTRA_NOTE_BUTTON_STRING, noteButtonString);
                this.activity.startActivityForResult(intent, CLICK_RESULT);
                return true;
            case "close":
                // 通知 webView 關閉
                WebViewActivity.myWebViewActivity.finish();
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
}