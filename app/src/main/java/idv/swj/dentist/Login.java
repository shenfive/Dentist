package idv.swj.dentist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;



//import java.sql.Date;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Login extends AppCompatActivity {
    EditText account,password;
    SharedPreferences loginPre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide(); //隱藏標題
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態

        account = (EditText)findViewById(R.id.account);
        password = (EditText)findViewById(R.id.password);
        loginPre = getSharedPreferences("loginStatus",0);


    }

    public void submit(View v) {
        String pass = password.getText().toString();
        String[] passwordStatus = checkPassword(pass);

        if (passwordStatus[0] != "200") {
            Toast.makeText(Login.this, "密碼格式錯誤,代碼:"+ passwordStatus[0] + "\n" + passwordStatus[1], Toast.LENGTH_SHORT).show();
            return;
        }

        String acc = account.getText().toString().toUpperCase();
        String[] accountStatus = checkPID(acc);
        if(accountStatus[0] != "200") {
            Toast.makeText(Login.this, "身份證字號錯誤,代碼:" + accountStatus[0] + "\n" + accountStatus[1], Toast.LENGTH_SHORT).show();
            return;
        }

        checkPasswordInput(acc,pass);

    }

    public String[] checkPassword(String password){

        String status[] = {"",""};


        //用正規表示法檢查是否包含英數字
        String patternStr1 = "[a-zA-Z]{1}";
        Pattern pattern1 = Pattern.compile(patternStr1);
        Matcher matcher1 = pattern1.matcher(password);
        boolean matchFound1 = matcher1.find();

        String patternStr2 = "[0-9]{1}";
        Pattern pattern2 = Pattern.compile(patternStr2);
        Matcher matcher2 = pattern2.matcher(password);
        boolean matchFound2 = matcher2.find();
        boolean matchFound = matchFound1 & matchFound2;


        if( (password.length() < 6 ) || (password.length() >8 ) ){
            status[0] = "401";
            status[1] = getResources().getString(R.string.passwordError001Len);
        }else if( !matchFound ){
            status[0] = "402";
            status[1] = getResources().getString(R.string.passwordError002Character);
        }else{
            status[0] = "200";
            status[1] = "true";
        }



        return status;
    }

    public String[] checkPID(String id){
        String[] status = {"",""};
        int[] num=new int[10];
        int[] rdd={10,11,12,13,14,15,16,17,34,18,19,20,21,22,35,23,24,25,26,27,28,29,32,30,31,33};
        id=id.toUpperCase();

        if(id.length() != 10){
            status[0] = "401";
            status[1] = "身份證字號格式錯誤, 長度必需為 10 碼";
            return status;
        }

        if(id.charAt(0)<'A'||id.charAt(0)>'Z'){
            status[0] = "402";
            status[1] = "身份證字號格式錯誤, 第一個字母為英文";
            return status;
        }
        if(id.charAt(1)!='1' && id.charAt(1)!='2'){
            status[0] = "403";
            status[1] = "身份證字號格式錯誤, 第二個字為數字 1 或 2";
            return status;
        }
        for(int i=1;i<10;i++){
            if(id.charAt(i)<'0'||id.charAt(i)>'9'){
                status[0] = "404";
                status[1] = "身份證字號格式錯誤, 第 3~9 個字為數字 0~9";
                return status;
            }
        }
        for(int i=1;i<10;i++){
            num[i]=(id.charAt(i)-'0');
        }
        num[0]=rdd[id.charAt(0)-'A'];
        int sum=((int)num[0]/10+(num[0]%10)*9);
        for(int i=0;i<8;i++){
            sum+=num[i+1]*(8-i);
        }
        if(10-sum%10==num[9]) {
            status[0] = "200";
            status[1] = "身份證字號格式正確";
        }else {
            status[0] = "405";
            status[1] = "身份證字號格式錯誤, 檢查碼錯誤, 請再次核對";
        }
        return status;

    }

    private void checkPasswordInput(String account,String password){

        TheLogin theLogin = new TheLogin();
        theLogin.start();




        if (account.equals("A123456789") && password.equals("a12345") ){
            loginPre.edit().putString("name","王小明")
            .putLong("lastLogin",new Date().getTime())
            .putString("account",account)
            .putString("password",password)
            .commit();
            synchronized (loginPre) {
                Toast.makeText(Login.this,getResources().getText(R.string.loginSuceesfully),Toast.LENGTH_LONG).show();
                this.finish();
            }
        }else {
            Toast.makeText(Login.this,"帳密錯誤, 請重新輸入",Toast.LENGTH_LONG).show();
        }


    }


    public void createAccount(View v){
        Intent intent = new Intent();
        intent.setClass(this,CreateAccount.class);
        startActivity(intent);
    }

    class TheLogin extends Thread
    {
        //類別裡的成員資料;
        //類別裡的方法;

        String apiLocation = "http://220.135.157.238:1113/api/PatientData/AddPatient/";
        URL url;
        String urlParameters = "{\n" +
                "\t\"Header\": {\n" +
                "\t\t\"Version\": \"1.0\",\n" +
                "\t\t\"CompanyId\": \"4881017701 \",\n" +
                "\t\t\"ActionMode\": \"LoginPatient\"\n" +
                "\t},\n" +
                "\t\"Data\": {\n" +
                "\t\t\"Account\": \"Andy\",\n" +
                "\t\t\"PatientPin\":\"1234567890\"\n" +
                "\t}\n" +
                "}";
        public void run()    //改寫Thread類別裡的run()方法
        {
            //以執行緒處理的程序;

            // Http Post 試認證
            HttpURLConnection connection = null;
            try {
                url = new URL(apiLocation); //建立 URL
                connection = (HttpURLConnection)url.openConnection(); //開啟 Connection
                connection.setReadTimeout(5000); //設置讀取超時為5秒
                connection.setConnectTimeout(10000); //設置連接網路超時為10秒

                connection.setDoInput(true);//可從伺服器取得資料
                connection.setDoOutput(true);//可寫入資料至伺服器

                connection.setRequestMethod("POST"); //設置請求的方法為POST

                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setRequestProperty("Content-Language", "zh-TW");
                connection.setUseCaches (false);  //POST方法不能緩存數據,需手動設置使用緩存的值為false


                Log.d("rsulot:","try5");
                //Send request
                DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
                Log.d("rsulot:","try6");
                wr.writeBytes (urlParameters);
                wr.flush ();
                wr.close ();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                rd.close();
                Log.d("rsulot:",response.toString());



            } catch (Exception e) {
                String er = e.getMessage();
                //e.printStackTrace();
                //Log.d("error",e.getLocalizedMessage().toString());
                Log.d("rsulot:","try2"+e);
            }




            Log.d("rsulot:","abc");

        }
    }


}
