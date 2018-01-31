package co.insidesolution.dentist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class Therapy extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapy);


        webView = (WebView)findViewById(R.id.therapyWeb);
        webView.loadUrl("http://www.charmingdent.com.tw/service.html");

    }
}
