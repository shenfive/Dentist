package idv.swj.dentist;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.content.ContextWrapper;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shenf on 2017/5/10.
 */

public class Tools {

    static public boolean checkNetworkConnected(Context context) {
        boolean result = false;
        ConnectivityManager CM = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (CM == null) {
            result = false;
        } else {
            NetworkInfo info = CM.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (!info.isAvailable()) {
                    result = false;
                } else {
                    result = true;
                }
                String TAG = "Net";

                Log.d(TAG, "[目前連線方式]"+info.getTypeName());
                Log.d(TAG, "[目前連線狀態]"+info.getState());
                Log.d(TAG, "[目前網路是否可使用]"+info.isAvailable());
                Log.d(TAG, "[網路是否已連接]"+info.isConnected());
                Log.d(TAG, "[網路是否已連接 或 連線中]"+info.isConnectedOrConnecting());
                Log.d(TAG, "[網路目前是否有問題 ]"+info.isFailover());
                Log.d(TAG, "[網路目前是否在漫遊中]"+info.isRoaming());
            }
        }
        if(!result){
            Toast.makeText(context,context.getString(R.string.networNotWork),Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    static public String[] checkEmailFormat(String emal){


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

    //檢查身份證編碼
    static public String[] checkPID(String id){
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

    static public String[] checkPassword(Context context,String password1,String password2){

        String status[] = {"",""};


        if( (password1.length() < 8 ) || (password1.length() >12 ) ){
            status[0] = "401";
            status[1] = context.getResources().getString(R.string.passwordError001Len);
        }else if(!password1.equals(password2)) {
            status[0] = "402";
            status[1] = "確認密碼必需相同";
        }else {
            status[0] = "200";
            status[1] = "true";
        }

        return status;
    }

    static public void showMessage(Context context,String message){

        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();

    }
    private static byte [] getHash(String password) {
        MessageDigest digest = null ;
        try {
            digest = MessageDigest. getInstance( "SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }

    public static String bin2hex(String strForEncrypt) {
        byte [] data = getHash(strForEncrypt);
        return String.format( "%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    static ProgressDialog wait(Context context){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.wait));
        progressDialog.show();
        return progressDialog;
    }

    static String int2StringDay(int year,int month,int day){
        String yrarS = year+"";

        String monthS = month+"";
        if(monthS.length() == 1 ){ monthS = "0"+ monthS; };

        String dayS = day+"";
        if(dayS.length() == 1){dayS = "0"+dayS; };

        return yrarS+"-"+monthS+"-"+dayS;
    }




//    public class xxxxAsyncTask extends AsyncTask<String, String, String> {
//
//        Activity context;
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            //before works
//        }
//        @Override
//        protected String  doInBackground(String... params) {
//
//
//            Log.d("Location","doInBackground");
//
//            try {
//                JSONObject jsonObject = new JSONObject(params[1]);
//                URL url = new URL(params[0]); //define the url we have to connect with
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//make connect with url and send request
//                urlConnection.setConnectTimeout(13000);//set timeout to 10 seconds
//                urlConnection.setReadTimeout(7000);//設置讀取超時為5秒
//                urlConnection.setRequestMethod("POST"); //設置請求的方法為POST
//                urlConnection.setInstanceFollowRedirects(true);
//                urlConnection.setRequestProperty("Content-Type","application/json");
//                urlConnection.setDoInput(true);//可從伺服器取得資料
//                urlConnection.setDoOutput(true);//可寫入資料至伺服器
//                urlConnection.setUseCaches (false);//POST方法不能緩存數據,需手動設置使用緩存的值為false
//
//                DataOutputStream wr = new DataOutputStream (urlConnection.getOutputStream());
//
//                byte[] outputBytes = jsonObject.toString().getBytes("UTF-8");
//                wr.write(outputBytes);
//                wr.flush ();
//                wr.close ();
//
//                //Get Response
//                InputStream is = urlConnection.getInputStream();
//                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//                String line;
//                StringBuffer response = new StringBuffer();
//
//                while((line = rd.readLine()) != null) {
//                    response.append(line);
//                    response.append('\r');
//                }
//                JSONObject jsonObject1 = new JSONObject(response.toString());
//                String string = jsonObject1.getJSONObject("Header").getString("StatusCode");
//
//                rd.close();
//                publishProgress(jsonObject1.toString()); // 取得回應後的處理
//
//
//            }catch (Exception ex){
//
//                Log.d("flag","error:"+ex.toString());
//            }
//            return null;
//        }
//
//
//
//        protected void onProgressUpdate(String... progress) {
//
//            progressDialog.cancel();
//            JSONObject jsonObject;
//            JSONObject header;
//            JSONObject data;
//            try {
//                //display response data
//                jsonObject = new JSONObject(progress[0]);
//                header = jsonObject.getJSONObject("Header");
//
//
//
//                Log.d("Fin:",header.getString("StatusCode"));
//
//                if (header.getString("StatusCode").equals("0000")) {
//                    context.finish();
//                }else{
//                    Toast.makeText(getApplicationContext(),header.getString("StatusDesc"),Toast.LENGTH_LONG).show();
//                }
//
//
//            } catch (Exception ex) {
//                Log.d("error",ex.getLocalizedMessage());
//            }
//
//        }
//
//        protected void onPostExecute(String  result2){
//
//
//        }
//
//    }

}


