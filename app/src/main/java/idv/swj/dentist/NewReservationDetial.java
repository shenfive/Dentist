package idv.swj.dentist;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class NewReservationDetial extends AppCompatActivity {

    JSONArray data;
    JSONArray allDrList;
    JSONObject dataIndex;
    JSONObject drIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation_detial);
        getSupportActionBar().hide(); //隱藏標題
        Bundle bundle = this.getIntent().getExtras();

        try {
            data = new JSONArray(bundle.getString("data"));
            allDrList = new JSONArray(bundle.getString("allDrList"));
            dataIndex = new JSONObject(bundle.getString("dataIndex"));
            drIndex = new JSONObject("drIndex");
        }catch (Exception e){
            Log.d("get dats",e.getLocalizedMessage());
        }



    }
}
