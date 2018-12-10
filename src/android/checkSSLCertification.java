package sc.mobile.investment.webview;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.util.Log;
import com.mitake.android.scb.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 基礎Class，後續再依模組(錢包、儲值、紅包、信用卡)實作延伸　Http Request。
 * Http Request 有可能一支API 分別用在不同的地方，所以集中於此.
 *
 * Created by oster on 2016/1/8.
 */
public class checkSSLCertification {
  TrustManagerFactory tmf = null;
  String TAG = "cordovaCheckSSLCertification";
  String folderName = "ca";
  String prefix = "ca_";
  Context myContext;

  // 主要的比對核心
    public boolean checkSSLError(Context context, SslError error){
        myContext = context;
        initKeyStone( myContext, getCerList() );

        boolean passVerify = false;

        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                try {
                    SslCertificate sslCertificate = error.getCertificate();
                    Field f = sslCertificate.getClass().getDeclaredField( "mX509Certificate" );
                    f.setAccessible( true );
                    X509Certificate x509 = (X509Certificate) f.get( sslCertificate );

                    X509Certificate[] chain = {x509};
                    for (TrustManager trustManager : tmf.getTrustManagers()) {
                        if (trustManager instanceof X509TrustManager) {
                            X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
                            try {
                                x509TrustManager.checkServerTrusted( chain, "generic" );
                                passVerify = true;
                                break;
                            } catch (Exception e) {
                                passVerify = false;
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                break;
        }

        if(!InvestmentWebView.sslPinning){
            passVerify = true;
        }

        return passVerify;
    }

  //ForSSLPinning
  private ArrayList<String> getCerList() {

    ArrayList<String> cerList = new ArrayList<>();
    AssetManager assetManager = myContext.getAssets();
      try {
        String[] assetsIWant = assetManager.list(folderName);

        for(int i = 0; i < assetsIWant.length; i++) {
            if(assetsIWant[i].contains(prefix)) {
              cerList.add( assetsIWant[i] );
            }
        }
      } catch (IllegalArgumentException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      }
      return cerList;
  }

  // 把所有的證書都輸入進去 TrustManagerFactory
  private void initKeyStone(Context context, ArrayList<String> creIDs) {
    try {
      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keyStore  = KeyStore.getInstance( keyStoreType );
      keyStore.load( null, null );

      int index = 1;
      for (String creID : creIDs) {
        CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
        // Generate the certificate using the certificate file under res/raw/cert.cer
        // InputStream caInput = new BufferedInputStream( context.getResources().openRawResource( creID ) );
        InputStream caInput =myContext.getAssets().open(folderName+"/"+creID);
        Certificate ca = cf.generateCertificate( caInput );
        caInput.close();
        keyStore.setCertificateEntry( "dbs_ca_" + String.valueOf( index ), ca );
        index++;
      }

      // Create a TrustManager that trusts the CAs in our KeyStore
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      tmf = TrustManagerFactory.getInstance( tmfAlgorithm );
      tmf.init( keyStore  );
    } catch (CertificateException e) {
      Log.d(TAG, Log.getStackTraceString(e));
    } catch (IOException e) {
      Log.d(TAG, Log.getStackTraceString(e));
    } catch (NoSuchAlgorithmException e) {
      Log.d(TAG, Log.getStackTraceString(e));
    } catch (KeyStoreException e) {
      Log.d(TAG, Log.getStackTraceString(e));
    }
  }
}
