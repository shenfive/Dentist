package idv.swj.dentist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態



        loginButton = (Button)findViewById(R.id.login);
        userNameButton = (Button) findViewById(R.id.userName);
        loginPre = getSharedPreferences("loginStatus",0);





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
                loginPre.edit().clear().commit();

                Toast.makeText(this,"己登出",Toast.LENGTH_LONG).show();
                loginButton.setVisibility(View.VISIBLE);
                userNameButton.setVisibility(View.INVISIBLE);

                return;

            default:
                return;
        }
        startActivity(intent);

    }

    private void modifyLoginStatusUI(){
        //己登入測試
        String username = loginPre.getString("name","nameError404");

        if(!username.equals("nameError404"))
        {
            userNameButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            userNameButton.setText("Welcome \n "+username);
        }else {
            userNameButton.setVisibility(View.INVISIBLE);
        }

    }



}
