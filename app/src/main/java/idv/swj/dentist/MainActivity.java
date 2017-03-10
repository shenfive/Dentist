package idv.swj.dentist;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button loginButton;
    Button userNameButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSupportActionBar().hide(); //隱藏標題
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態



        loginButton = (Button)findViewById(R.id.login);
        userNameButton = (Button) findViewById(R.id.userName);



        //己登入測試
        loginButton.setVisibility(View.INVISIBLE);


    }

    public void tabPage(View v){

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
                loginButton.setVisibility(View.VISIBLE);
                return;

            default:
                return;
        }
        startActivity(intent);

    }



}
