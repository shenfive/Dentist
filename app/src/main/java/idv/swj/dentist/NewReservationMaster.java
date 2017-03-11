package idv.swj.dentist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NewReservationMaster extends AppCompatActivity {
    Spinner doctorListSpinner;
    String[] doctorList;
    SharedPreferences loginPre;
    TextView loginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation_master);

        getSupportActionBar().hide(); //隱藏標題
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態


        doctorListSpinner = (Spinner)findViewById(R.id.doctoerList);
        loginName = (TextView)findViewById(R.id.loginName);
        loginPre = getSharedPreferences("loginStatus",0);



        // 設定醫生選單

        doctorList = getDoctor();
        ArrayAdapter<String> doctorListAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,doctorList);
        doctorListSpinner.setAdapter(doctorListAdapter);





    }

    @Override
    public void onResume(){
        super.onResume();
        checkLoingStatus();
        Log.d("the"," Resume");
    }


    private String[] getDoctor(){
        //取得醫師清單
        String[] List = {"不指定醫師","張三丰","李四端","王五刀"};
        return List;
    }


    private void checkLoingStatus(){
        //己登入測試
        String username = loginPre.getString("name","nameError404");

        Toast.makeText(this,username,Toast.LENGTH_SHORT).show();


        if(username.equals("nameError404"))
        {
            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);

            MyAlertDialog.setTitle("Alert");

            MyAlertDialog.setMessage("您尚未登入");
            DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setClass(NewReservationMaster.this,Login.class);
                    startActivity(intent);
                }

            };

            MyAlertDialog.setPositiveButton(getResources().getString(R.string.login),okClick);
            MyAlertDialog.setNegativeButton(getResources().getString(R.string.cancel),null);
            MyAlertDialog.show();
        }else {
            loginName.setText("Welcome:\n"+username);
        }

    }


}
