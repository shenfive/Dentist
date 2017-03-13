package idv.swj.dentist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class News extends AppCompatActivity {

    ListView news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        getSupportActionBar().hide(); //隱藏標題
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態

        news = (ListView)findViewById(R.id.newsList);

        String[] newsList = {"第一則新聞","第二則新聞","第三則新聞"};

        ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,newsList);

        news.setAdapter(newsAdapter);

    }
}
