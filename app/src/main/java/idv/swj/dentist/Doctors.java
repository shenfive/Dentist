package idv.swj.dentist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class Doctors extends AppCompatActivity {


    WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors);


        webView = (WebView)findViewById(R.id.doctorWeb);

        webView.loadUrl("http://www.charmingdent.com.tw/team.html");


    }
}
