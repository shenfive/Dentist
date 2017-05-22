package idv.swj.dentist;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyReservation extends AppCompatActivity {
    ListView myReservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservation);
        myReservationList = (ListView)findViewById(R.id.revListView);
        upDateMyResv();
    }

    private void upDateMyResv(){
        SharedPreferences sharedPreferences = getSharedPreferences("myAppointment",0);
        JSONArray data = new JSONArray();
        try {

            data = new JSONArray(sharedPreferences.getString("res",""));

        }catch (Exception e){
            Log.d("upDateMyResv",e.getLocalizedMessage());
        }
        ArrayList<String> arrayList = new ArrayList<>();
        for(int i=0;i<data.length();i++){
            try {
                JSONObject item = data.getJSONObject(i);

                arrayList.add(item.getString("DrName")+item.getString("AppointmentStartTime"));
                
            }catch (Exception e){Log.d("REV",e.getLocalizedMessage());}
        }
        Log.d("REV",arrayList.toString());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,arrayList);
        myReservationList.setAdapter(arrayAdapter);




    }
}
