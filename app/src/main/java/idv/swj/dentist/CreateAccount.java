package idv.swj.dentist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccount extends AppCompatActivity {

    EditText account,name,phone,email,password1,password2;
    RadioButton male,female;
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




        getSupportActionBar().hide(); //隱藏標題
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態


    }

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
            status[1] = "格式不正確";
        }
        return status;


    }

    public String[] checkPassword(String password){

        String status[] = {"",""};


        //用正規表示法檢查是否包含英數字
        String patternStr = "[a-zA-Z]{1}[0-9]{1}";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(password);
        boolean matchFound = matcher.find();


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

    public void submitCreatAccount(View v){

        String[] checkStatus = {"",""};
        String acccountS,nameS,password1S,password2S,emailS,phoneS,genderS,birthdayS;

//        acccountS = account.getText().toString();
//        checkStatus = checkPID(acccountS);
//        if(!checkStatus[0].equals("200")){
//            Toast.makeText(this,checkStatus[1],Toast.LENGTH_SHORT).show();
//            account.requestFocus();
//            return;
//        }
//
//        nameS = name.getText().toString();
//        if(nameS.length()<1){
//            Toast.makeText(this,getResources().getString(R.string.nameNoEmpty),Toast.LENGTH_SHORT).show();
//            name.requestFocus();
//            return;
//        }
//
        emailS = email.getText().toString();
        checkStatus = checkEmailFormat(emailS);
        Toast.makeText(this,checkStatus[1],Toast.LENGTH_SHORT).show();

    }


}
