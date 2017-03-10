package idv.swj.dentist;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //設定 TabView

        TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec tabSpec;


        tabSpec = tabHost.newTabSpec("tab1");
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator(getResources().getString(R.string.tab1));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tab2");
        tabSpec.setContent(R.id.tab2);
        tabSpec.setIndicator(getResources().getString(R.string.tab2));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tab3");
        tabSpec.setContent(R.id.tab3);
        tabSpec.setIndicator(getResources().getString(R.string.tab3));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tab4");
        tabSpec.setContent(R.id.tab4);
        tabSpec.setIndicator(getResources().getString(R.string.tab4));
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);





    }
}
