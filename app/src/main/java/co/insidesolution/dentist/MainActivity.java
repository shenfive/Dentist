package co.insidesolution.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button loginButton;
    Button userNameButton;
    SharedPreferences loginPre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        loginButton = (Button)findViewById(R.id.login);
        userNameButton = (Button) findViewById(R.id.userName);
        loginPre = getSharedPreferences("loginStatus",0);

    }

    @Override
    public void onResume(){
        super.onResume();
        //己登入測試
        modifyLoginStatusUI();
        Tools.updateRevStatus(this);
        Tools.updateRevHistoryStatus(this);
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
                if(!Tools.checkNetworkConnected(this)){return;}
                intent.setClass(this,NewReservationMaster.class);
                break;

            case R.id.myReservation:
                if(!Tools.checkNetworkConnected(this)){return;}
                intent.setClass(this,MyReservation.class);
                break;
            case R.id.login:
                if(!Tools.checkNetworkConnected(this)){return;}
                intent.setClass(this,Login.class);
                break;
            case R.id.userName:
                listDialog();
                return;

            default:
                return;
        }
        startActivity(intent);

    }

    private void modifyLoginStatusUI(){
        //己登入測試

        String loginStatus = loginPre.getString("status","logout");

        if(!loginStatus.equals("logout"))
        {
            userNameButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            String username = loginPre.getString("PatientName","unknow");
            userNameButton.setText("Welcome \n "+username);
        }else {
            userNameButton.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.VISIBLE);
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

    public class GetDoctorsAppointmentAsyncTask extends AsyncTask<String, String, String> {

        Activity context;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //before works
        }
        @Override
        protected String  doInBackground(String... params) {

            Log.d("Location","doInBackground");

            try {
                JSONObject jsonObject = new JSONObject(params[1]);
                URL url = new URL(params[0]); //define the url we have to connect with
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//make connect with url and send request
                urlConnection.setConnectTimeout(13000);//set timeout to 10 seconds
                urlConnection.setReadTimeout(7000);//設置讀取超時為5秒
                urlConnection.setRequestMethod("POST"); //設置請求的方法為POST
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setDoInput(true);//可從伺服器取得資料
                urlConnection.setDoOutput(true);//可寫入資料至伺服器
                urlConnection.setUseCaches (false);//POST方法不能緩存數據,需手動設置使用緩存的值為false

                DataOutputStream wr = new DataOutputStream (urlConnection.getOutputStream());

                byte[] outputBytes = jsonObject.toString().getBytes("UTF-8");
                wr.write(outputBytes);
                wr.flush ();
                wr.close ();

                //Get Response
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();

                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                rd.close();

                publishProgress(response.toString()); // 取得回應後的處理


            }catch (Exception ex){

                Log.d("flag","error:"+ex.toString());
            }
            return null;
        }



        protected void onProgressUpdate(String... progress) {
            progressDialog.cancel();


            JSONObject jsonObject;
            JSONObject header;
            try {
                //display response data
                jsonObject = new JSONObject(progress[0]);
                header = jsonObject.getJSONObject("Header");



                Log.d("Fin:",header.getString("StatusCode"));

                if (header.getString("StatusCode").equals("0000")) {
                    Toast.makeText(getApplicationContext(),getString(R.string.forgetPassworCheckEmail),Toast.LENGTH_LONG).show();
                    context.finish();
                }else{
                    Toast.makeText(getApplicationContext(),header.getString("StatusDesc"),Toast.LENGTH_LONG).show();
                }


            } catch (Exception ex) {
                Log.d("error",ex.getLocalizedMessage());
            }

        }

        protected void onPostExecute(String  result2){


        }

    }



}



