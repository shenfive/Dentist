package idv.swj.dentist;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccount extends AppCompatActivity {

    EditText account,name,phone,email,password1,password2,nID;
    RadioButton male,female;
    RadioGroup radioGroup;
    DatePicker datePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        account = (EditText)findViewById(R.id.account);
        name = (EditText)findViewById(R.id.name);
        phone = (EditText)findViewById(R.id.phone);
        email = (EditText)findViewById(R.id.email);
        password1 = (EditText)findViewById(R.id.password1);
        password2 = (EditText)findViewById(R.id.password2);
        male = (RadioButton)findViewById(R.id.male);
        female = (RadioButton)findViewById(R.id.female);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        nID = (EditText)findViewById(R.id.nID);


        getSupportActionBar().hide(); //隱藏標題

//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態

        account.requestFocus();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                //將鍵盤收起來

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(group.getWindowToken(), 0);
                datePicker.requestFocus();
            }
        });

        long today = new Date().getTime();


        datePicker.setMaxDate(today - 1000);
        datePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);



    }


    //用正規表示法檢查是否 合於 Email 格式
    public String[] checkEmailFormat(String emal){


        String status[] = {"",""};

        //用正規表示法檢查是否包含英數字
        Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emal);
        boolean matchFound = matcher.find();

        if(matchFound){
            status[0] = "200";
            status[1] = "格式正確";
        }
        else {
            status[0] = "400";
            status[1] = "電子郵件格式不正確";
        }
        return status;


    }

    public String[] checkPassword(String password1,String password2){

        String status[] = {"",""};


        //用正規表示法檢查是否包含英數字
//        String patternStr1 = "[a-zA-Z]{1}";
//        Pattern pattern1 = Pattern.compile(patternStr1);
//        Matcher matcher1 = pattern1.matcher(password1);
//        boolean matchFound1 = matcher1.find();
//
//        String patternStr2 = "[0-9]{1}";
//        Pattern pattern2 = Pattern.compile(patternStr2);
//        Matcher matcher2 = pattern2.matcher(password1);
//        boolean matchFound2 = matcher2.find();
//        boolean matchFound = matchFound1 & matchFound2;


        if( (password1.length() < 8 ) || (password1.length() >12 ) ){
            status[0] = "401";
            status[1] = getResources().getString(R.string.passwordError001Len);
//        }else if( !matchFound ){
//            status[0] = "402";
//            status[1] = getResources().getString(R.string.passwordError002Character);
//        }else if( !password1.equals(password2)) {
//            status[0] = "403";
//            status[1] = getResources().getString(R.string.passwordError003dif);
        }else{
            status[0] = "200";
            status[1] = "true";
        }



        return status;
    }


    //檢查身份證編碼
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



    public void submitCreatAccount(View v) throws JSONException {

        String[] checkStatus = {"",""};
        String acccountS,nameS,password1S,password2S,emailS,phoneS,genderS,birthdayS,idS;


        //帳號不得空白
        acccountS = account.getText().toString();
        if(acccountS.length()<1){
            Toast.makeText(this,getResources().getString(R.string.accountNoEmpty),Toast.LENGTH_SHORT).show();
            account.requestFocus();
            return;
        }


        //檢查身份證格式是否正確
        idS = nID.getText().toString();
        checkStatus = checkPID(idS);
        if(!checkStatus[0].equals("200")){
            Toast.makeText(this,checkStatus[1],Toast.LENGTH_SHORT).show();
            account.requestFocus();
            return;
        }

        nameS = name.getText().toString();
        if(nameS.length()<1){
            Toast.makeText(this,getResources().getString(R.string.nameNoEmpty),Toast.LENGTH_SHORT).show();
            name.requestFocus();
            return;
        }


        password1S = password1.getText().toString();
        password2S = password2.getText().toString();
        checkStatus = checkPassword(password1S,password2S);
        if (!checkStatus[0].equals("200")) {
            Toast.makeText(this, checkStatus[1], Toast.LENGTH_SHORT).show();
            if (checkStatus[0].equals("403")){
                password2.requestFocus();
            }else {
                password1.requestFocus();
            }
            return;
        }

        phoneS = phone.getText().toString();
        if(phoneS.length()<10){
            Toast.makeText(this,getResources().getString(R.string.phoneTooShort),Toast.LENGTH_SHORT).show();
            phone.requestFocus();
            return;
        }



        emailS = email.getText().toString();
        checkStatus = checkEmailFormat(emailS);
        if(!checkStatus[0].equals("200")) {
            Toast.makeText(this, checkStatus[1], Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }

        if(male.isChecked()){
            genderS = "male";
        }else if(female.isChecked()){
            genderS = "female";
        }else {
            Toast.makeText(this, getResources().getString(R.string.gender), Toast.LENGTH_SHORT).show();
            radioGroup.requestFocus();
            return;
        }

        String year = (datePicker.getYear()-1911)+"";
        if(year.length()==1){ year = "00"+year;};
        if(year.length()==2){ year = "0"+year;};

        String month = (datePicker.getMonth()+1)+"";
        if(month.length()==1){ month = "0"+month;}

        String day = datePicker.getDayOfMonth()+"";
        if(day.length()==1){ day = "0"+day;}

        birthdayS = year+month+day;
        Toast.makeText(this, birthdayS+"檢查完", Toast.LENGTH_SHORT).show();


        // 接下來要打 API 了
        JSONObject parameter = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();

        header.put("Version","1.0");
        header.put("CompanyId","4881017701");
        header.put("ActionMode","AddPatient");

        data.put("Account",acccountS);
        data.put("PatientSN",idS);
        data.put("PatientName",nameS);
        data.put("PatientMobile",phoneS);
        data.put("PatientPin",password1S);
        data.put("PatientEmail",emailS);
        data.put("Gender",genderS);
        data.put("Birthday",birthdayS);

        parameter.put("Header",header);
        parameter.put("Data",data);

        AddAccount addAccount = new AddAccount();
        addAccount.parameter = parameter;
        addAccount.start();


    }

    // 將西元日期轉換為民國日期
//    private String getROCdateString(Date date){
//
//        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("YYYY");
//        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMdd");
//
//
//        String rocYear = (Integer.parseInt(simpleDateFormat1.format(date)) - 1911) + "";
//        if (rocYear.length() == 2){ rocYear = "0" + rocYear; }
//
//        return rocYear+simpleDateFormat2.format(date);
//    }


    private class AddAccount extends Thread
    {
        //類別裡的成員資料;
        //類別裡的方法;
        CreateAccount mainContext;

        JSONObject parameter;


        String apiLocation = "http://220.135.157.238:1113/api/PatientData/AddPatient";
        URL url;
        public void run()    //改寫Thread類別裡的run()方法
        {
            //以執行緒處理的程序;
            HttpURLConnection connection;
            try {

                url = new URL(apiLocation); //建立 URL
                connection = (HttpURLConnection)url.openConnection(); //開啟 Connection

                connection.setReadTimeout(5000); //設置讀取超時為2.5秒
                connection.setConnectTimeout(10000); //設置連接網路超時為5秒
                connection.setRequestMethod("POST"); //設置請求的方法為POST
                connection.setInstanceFollowRedirects(true);

                connection.setDoInput(true);//可從伺服器取得資料
                connection.setDoOutput(true);//可寫入資料至伺服器
                connection.setRequestMethod("POST"); //設置請求的方法為POST
                connection.setRequestProperty("Content-Type","application/json");
//                connection.setRequestProperty("charset", "utf-8");
                connection.setUseCaches (false);  //POST方法不能緩存數據,需手動設置使用緩存的值為false
                //Send request
                DataOutputStream wr = new DataOutputStream (connection.getOutputStream());

                byte[] outputBytes = parameter.toString().getBytes("UTF-8");
                wr.write(outputBytes);
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
                JSONObject jsonObject1 = new JSONObject(response.toString());
                String string = jsonObject1.getJSONObject("Header").getString("StatusCode");

                rd.close();
                Log.d("API 回應",parameter.toString()+"\n res:"+response.toString() +"\n status:"+string);


            } catch (Exception e) {
                String er = e.getMessage();
                Log.d("網路錯誤:","error"+e);

            }
        }
    }


}
