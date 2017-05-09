package idv.swj.dentist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button loginButton;
    Button userNameButton;
    SharedPreferences loginPre;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSupportActionBar().hide(); //隱藏標題
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態



        loginButton = (Button)findViewById(R.id.login);
        userNameButton = (Button) findViewById(R.id.userName);
        loginPre = getSharedPreferences("loginStatus",0);

        modifyLoginStatusUI();

    }

    @Override
    public void onResume(){
        super.onResume();
        //己登入測試
        modifyLoginStatusUI();
    }


    public void buttonOnClick(View v){

        int viewID = v.getId();
        Intent intent = new Intent();

        switch (viewID){

            case R.id.tab1b:
                intent.setClass(this,Intro.class);
                break;

            case R.id.tab2b:
                intent.setClass(this,News.class);
                break;

            case R.id.tab3b:
                intent.setClass(this,Therapy.class);
                break;

            case R.id.tab4b:
                intent.setClass(this,Doctors.class);
                break;

            case R.id.newReservation:
                intent.setClass(this,NewReservationMaster.class);
                break;

            case R.id.myReservation:
                intent.setClass(this,MyReservation.class);
                break;
            case R.id.login:
                intent.setClass(this,Login.class);
                break;
            case R.id.userName:
                //TODO  建立 spanner 前往不同的 Activety
                listDialog();
                return;

            default:
                return;
        }
        startActivity(intent);

    }

    private void modifyLoginStatusUI(){
        //己登入測試
        String username = loginPre.getString("PatientName","unknow");
        String loginStatus = loginPre.getString("status","logout");

        if(!loginStatus.equals("logout"))
        {
            userNameButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            userNameButton.setText("Welcome \n "+username);
        }else {
            userNameButton.setVisibility(View.INVISIBLE);
        }
    }
    private void listDialog(){
        final ArrayList arrayList= new ArrayList<>();
        arrayList.add(getString(R.string.modifyYouData));
        arrayList.add(getString(R.string.logout));



        new AlertDialog.Builder(MainActivity.this)
                .setItems((CharSequence[]) arrayList.toArray(new String[arrayList.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = (String) arrayList.get(which);
                        Toast.makeText(getApplicationContext(), name+which , Toast.LENGTH_SHORT).show();
                        switch (which){
                            case 0:
                                Intent intent = new Intent(MainActivity.this,ModifyData.class);
                                startActivity(intent);
                                break;
                            case 1: //登出
                                loginPre.edit().clear().commit();
                                Toast.makeText(MainActivity.this,getString(R.string.logoutSuceesfully),Toast.LENGTH_LONG).show();
                                loginButton.setVisibility(View.VISIBLE);
                                userNameButton.setVisibility(View.INVISIBLE);
                                break;
                        }
                    }
                })
                .show();
    }


}
