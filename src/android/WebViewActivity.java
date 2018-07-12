package sc.mobile.investment.webview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.util.Log;
import android.view.MotionEvent;

import org.json.JSONException;
import org.json.JSONObject;

public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TYPE= "extra_type";
    public static final String EXTRA_NOTE_BUTTON_STRING = "extra_note_button_string";
    public static boolean fromTitleButton = false;   //最後觸摸時間
    public static JSONObject callbackContent;   // 額外呼叫的activity要跟investmentWebView溝通, 只能透過自定變數
    protected WebView webView;
    protected ProgressBar progressBar;
    protected String url = null;
    private String packageName;
    private int type;
    private BroadcastReceiver receiver=null;
    private boolean receiversRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 強制設定直立方向, soft
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        packageName = getApplicationContext().getPackageName();
        int view_id = getResources().getIdentifier("activity_web_view", "layout", packageName);
        setContentView(view_id);
        String title = "";
        String noteButtonString = "";
        if(getIntent() != null){
            url = getIntent().getStringExtra(EXTRA_URL);
            title = getIntent().getStringExtra(EXTRA_TITLE);
            type = getIntent().getIntExtra(EXTRA_TYPE, 1);
            noteButtonString = getIntent().getStringExtra(EXTRA_NOTE_BUTTON_STRING);
        }

        // 設定標題
        setCenterTitle(title);

        // 設定按鈕
        int button_id = getResources().getIdentifier("button", "id", packageName);
        Button noteButton = (Button) findViewById(button_id);
        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNote();
            }
        });
        if(!TextUtils.isEmpty(noteButtonString)) {
            noteButton.setText(noteButtonString);
        }
        else {
            // 沒有說明文字就隱藏按鈕
            noteButton.setVisibility(View.GONE);
        }

        int progress_bar_id = getResources().getIdentifier("progress_bar", "id", packageName);
        progressBar = (ProgressBar) findViewById(progress_bar_id);

        int webview_id = getResources().getIdentifier("webview", "id", packageName);
        webView = (WebView) findViewById(webview_id);
        // 設定自訂的WebViewClient，讓超連結都能在WebView中開啟，當網頁開始與結束讀取時，顯示或隱藏進度條
        webView.setWebViewClient(new CustomWebViewClient());
        // 設定自訂的WebChromeClient，取得讀取進度
        webView.setWebChromeClient(new CustomWebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        // 使用內建縮放功能
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        // 讓圖片調整到適合WebView大小
        webView.getSettings().setUseWideViewPort(true);
        webView.loadUrl(url);
        // add touch event
        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent event) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("type", "touch");
                    obj.put("lastTouchTime",String.valueOf(System.currentTimeMillis()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendUpdate(obj);
                return false;
            }
        });

        // add buttonclick event
        webView.addJavascriptInterface(new MyJsToAndroid(),"my");

        // add broadcast, 接受investmentWebView傳來的消息
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("close")) {
                    finish();
                }
            }
        };
        registerReceivers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int home_id = getResources().getIdentifier("home", "id", "android");
        if(item.getItemId() == home_id){
            if(webView.canGoBack()){
                webView.goBack();
            }else {
                fromTitleButton=true;
                onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
         if(fromTitleButton){
             fromTitleButton=false;
             super.onBackPressed();
         }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        webView.clearCache(true);
    }

    @Override
    protected void onPause() {
        unregisterReceivers();
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceivers();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        registerReceivers();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    private class CustomWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // 回傳網頁變更通知
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "loadstart");
                obj.put("url",url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendUpdate(obj);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // 載入 buttonclick event
            webView.evaluateJavascript(addMyClickCallBackJs(), null);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e("Error", "Received SSL error"+ error.toString());
            handler.proceed();
        }
    }

    // action bar
    public void setCenterTitle(String title) {
        int toolbar_id = getResources().getIdentifier("toolbar", "id", packageName);
        Toolbar toolbar = (Toolbar) findViewById(toolbar_id);
        if (toolbar != null) {
            toolbar.setTitle("");
            int tcustom_title_id = getResources().getIdentifier("custom_title", "id", packageName);
            TextView textViewTitle = (TextView) toolbar.findViewById(tcustom_title_id);
            textViewTitle.setText(title);
            setSupportActionBar(toolbar);

            int ic_header_back_id = getResources().getIdentifier("ic_header_back", "drawable", packageName);
            getSupportActionBar().setHomeAsUpIndicator(ic_header_back_id);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if(TextUtils.isEmpty(title)){
            toolbar.setVisibility(View.GONE);
        }
    }

    private void goToNote() {
        String noteURL;
        switch (type) {
            
            case 2:
                noteURL = "file:///android_res/raw/investment_note_2.html";
                break;
            case 3:
                noteURL = "file:///android_res/raw/investment_note_3.html";
                break;
            case 4:
                noteURL = "file:///android_res/raw/investment_note_4.html";
                break;
            case 5:
                noteURL = "file:///android_res/raw/investment_note_5.html";
                break;
            case 6:
                noteURL = "file:///android_res/raw/investment_note_6.html";
                break;
            case 7:
                noteURL = "file:///android_res/raw/investment_note_7.html";
                break;
            case 20:
                noteURL = "file:///android_res/raw/investment_note_20.html";
                break;
            default:  //1
                noteURL = "file:///android_res/raw/investment_note_1.html";
                break;

        }

        webView.loadUrl(noteURL);
    }

    // 設定回傳元素
    private void sendUpdate(JSONObject obj){
        callbackContent = obj;
        InvestmentWebView.sendUpdate(obj);
    }

    // 廣播
    public void registerReceivers() {
        // Only register if not already registered
        if (!receiversRegistered) {
            IntentFilter intentFilter = new IntentFilter("close");
            registerReceiver(receiver, intentFilter);
            receiversRegistered = true;
        }
    }
    public void unregisterReceivers() {
        // Only register if not already registered
        if (receiversRegistered) {
            unregisterReceiver(receiver);
            receiversRegistered = false;
        }
    }

    // buttonclick
    // JsCallBack
    class MyJsToAndroid extends Object{
        @JavascriptInterface
        public void myClick(String idOrClass) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "buttonclick");
                obj.put("id",idOrClass);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendUpdate(obj);
        }
    }
    // JS--
    public static String addMyClickCallBackJs() {
        String js = "javascript:";
        js += "function myClick(t){if(null!=t.target.id){var e=t.target;\"button\"==e.tagName.toLowerCase()?my.myClick(e.id):\"button\"==(e=e.parentElement).tagName.toLowerCase()&&my.myClick(e.id)}}document.addEventListener(\"click\",myClick,!0);";

            /*
            function myClick(event){
            if(event.target.id != null){ 
                var obj = event.target;
                if(obj.tagName.toLowerCase()==\"button\") {
                    my.myClick(obj.id);
                }
                else {
                    obj = obj.parentElement;
                    if(obj.tagName.toLowerCase()==\"button\") {
                        my.myClick(obj.id);
                    }
                }
                }
            } 
            document.addEventListener(\"click\",myClick,true);
            */
        return js;
    }
}