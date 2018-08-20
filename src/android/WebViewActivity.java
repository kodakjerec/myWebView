package sc.mobile.investment.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
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
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.util.Log;
import android.view.MotionEvent;
import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;

public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TYPE= "extra_type";
    public static final String EXTRA_NOTE_BUTTON_STRING = "extra_note_button_string";
    public static boolean fromTitleButton = false;   //判斷返回鍵是軟體或是硬體觸發
    public static JSONObject callbackContent;   // 額外呼叫的activity要跟investmentWebView溝通, 只能透過自定變數
    protected WebView webView;
    protected ProgressBar progressBar;
    protected String url = null;
    private String packageName;
    private int type;
    private FrameLayout mWebContainer;
    public static Activity myWebViewActivity;
    public static JSONObject languageJson; // 系統預設中英字串
    public static String locale= "tw";  // vue.js設定的語系
    String TAG="https";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myWebViewActivity = this;

        // 強制設定直立方向, soft
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        super.onCreate(savedInstanceState);
        packageName = getApplicationContext().getPackageName();
        int view_id = getResources().getIdentifier("activity_web_view", "layout", packageName);
        setContentView(view_id);
        mWebContainer = findViewById( getResources().getIdentifier("frameLayout", "id", packageName));
        String title = "";
        String noteButtonString = "";
        if(getIntent() != null){
            try {
                URL tempurl = new URL( getIntent().getStringExtra( EXTRA_URL ) );
                URI uri = new URI( tempurl.getProtocol(), tempurl.getUserInfo(), tempurl.getHost(), tempurl.getPort(), tempurl.getPath(), tempurl.getQuery(), tempurl.getRef() );
                tempurl = uri.toURL();
                url = tempurl.toString();
                Log.d(TAG,url);
            }catch (Exception e) {
                showAlertDialog( getLanguageText( "confirmTitle" ),e.toString() );
                return;
            }
            title = getIntent().getStringExtra(EXTRA_TITLE);
            type = getIntent().getIntExtra(EXTRA_TYPE, 1);
            noteButtonString = getIntent().getStringExtra(EXTRA_NOTE_BUTTON_STRING);
        }

        // 設定標題
        setCenterTitle(title);

        // 設定按鈕
        int button_id = getResources().getIdentifier("button", "id", packageName);
        Button noteButton = findViewById(button_id);
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
        progressBar = findViewById(progress_bar_id);

        int webview_id = getResources().getIdentifier("webview", "id", packageName);
        webView = findViewById(webview_id);
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
        // 避免轉向導致reload url
        if(savedInstanceState == null) {
            webView.loadUrl( url );
        }
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        // 避免轉向導致reload url
        super.onSaveInstanceState( outState, outPersistentState );
        webView.saveState( outState );
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // 避免轉向導致reload url
        super.onRestoreInstanceState( savedInstanceState );
        webView.restoreState( savedInstanceState );
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
        webView.clearCache(true);
        mWebContainer.removeView(webView);
        webView.removeAllViews();
        webView.destroy();
        webView=null;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class CustomWebChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        @Nullable
        @Override
        public Bitmap getDefaultVideoPoster() {
            if (WebViewActivity.this == null) {
                return null;
            }
            return BitmapFactory.decodeResource(WebViewActivity.this.getApplicationContext().getResources(), 2130837573);
        }

        @Override
        public void onHideCustomView()
        {
            // 讓影片可以FullScreen
            ((FrameLayout)WebViewActivity.this.getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            WebViewActivity.this.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            WebViewActivity.this.setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        @Override
        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            // 讓影片可以FullScreen
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = WebViewActivity.this.getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = WebViewActivity.this.getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)WebViewActivity.this.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            WebViewActivity.this.getWindow().getDecorView().setSystemUiVisibility(3846);
            WebViewActivity.this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE );
        }

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

//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            Log.d(TAG, "onReceivedSslError "+String.valueOf(error.getPrimaryError()));
//
//            new Thread( new Runnable() {
//                @Override
//                public void run() {
//                    // 檢查合理性
//                    checkSSLCertification obj = new checkSSLCertification();
//                    boolean isOK = obj.checkSSLError(getApplicationContext(), error);
//                    if(isOK) {
//                        handler.proceed();
//                    } else {
//                        handler.cancel();
//                        showAlertDialog( getLanguageText( "confirmTitle" ), "Certification is untrusted." );
//                    }
//                }
//            } ).start();
//        }
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
        switch (locale){
            case "en":
                switch (type) {
                    case 2:
                        noteURL = "file:///android_res/raw/investment_note_2_en.html";
                        break;
                    case 3:
                        noteURL = "file:///android_res/raw/investment_note_3_en.html";
                        break;
                    case 4:
                        noteURL = "file:///android_res/raw/investment_note_4_en.html";
                        break;
                    case 5:
                        noteURL = "file:///android_res/raw/investment_note_5_en.html";
                        break;
                    case 6:
                        noteURL = "file:///android_res/raw/investment_note_6_en.html";
                        break;
                    case 7:
                        noteURL = "file:///android_res/raw/investment_note_7_en.html";
                        break;
                    case 20:
                        noteURL = "file:///android_res/raw/investment_note_20_en.html";
                        break;
                    default:  //1
                        noteURL = "file:///android_res/raw/investment_note_1_en.html";
                        break;
                }
                break;
            default:
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
                break;
        }

        webView.loadUrl(noteURL);
    }

    // 設定回傳元素
    private void sendUpdate(JSONObject obj){
        callbackContent = obj;
        InvestmentWebView.sendUpdate(obj);
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
    private static String addMyClickCallBackJs() {
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

    // 自訂提醒視窗, webView發生錯誤 專用
    private void showAlertDialog(String title, String message){
        // 顯示返回
        int toolbar_id = getResources().getIdentifier("toolbar", "id", packageName);
        Toolbar toolbar = (Toolbar) findViewById(toolbar_id);
        toolbar.setVisibility(View.VISIBLE);


        this.runOnUiThread( new Runnable() {
            @Override
            public void run() {
                // 跳出提醒dialog
                AlertDialog.Builder alertDialogBuilde = new android.app.AlertDialog.Builder( myWebViewActivity )
                        .setTitle( title )
                        .setMessage( message )
                        .setPositiveButton( getLanguageText("confirmOK"),new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }  )
                        .setCancelable( false );

                AlertDialog alertDialogr = alertDialogBuilde.create();
                alertDialogr.show();
            }
        } );

    }

    //9分鐘提醒視窗, 返回效果與showAlertDialog不同
    public static void remindAlertDialog(String message, String title, String buttonName, CallbackContext callbackContext){
        try {
            new android.app.AlertDialog.Builder( myWebViewActivity )
                    .setTitle( title )
                    .setMessage( message )
                    .setPositiveButton( buttonName, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            callbackContext.success();
                        }
                    } )
                    .setOnCancelListener( new AlertDialog.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                            callbackContext.success();
                        }
                    } )
                    .setCancelable( true )
                    .show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 取得對應的文字
    private  String getLanguageText(String key){
        String result = languageJson.optString( key,key );
        return result;
    }
}