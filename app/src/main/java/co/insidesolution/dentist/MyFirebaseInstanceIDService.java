package co.insidesolution.dentist;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.Preference;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    public MyFirebaseInstanceIDService() {
    }

    private static final String TAG = "MyFirebaseIIDService";

    //@Override
    //public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    //    throw new UnsupportedOperationException("Not yet implemented");
    //}

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        SharedPreferences sharedPref = getSharedPreferences("pushToken",0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("FCMpushToken", refreshedToken);
        editor.commit();
        sendRegistrationToServer(refreshedToken);
    }
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

}