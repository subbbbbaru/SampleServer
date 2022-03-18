package com.example.sampleserver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.widget.Toast;

public class Telephony {

    public static Context context;
    public static String[] mSet;

    public static void Telephony (Context context1) {
        context = context1;
    }

    public static void answerPhoneHeadsethook() {
        // «Нажимаем» и «отпускаем» кнопку на гарнитуре
        Intent buttonDown = new Intent( Intent.ACTION_MEDIA_BUTTON);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

        Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
    }

    public static void SMS(String tel, String text) {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage( tel, null, text, null, null );
            //Toast.makeText( context,
            //        "SMS отправлено!", Toast.LENGTH_LONG ).show();
            }
            catch (Exception e){
                    Toast.makeText(context,
                            "SMS не отправлено, попытайтесь еще! " + e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
            }

    }

    public static void Call(String tel) {
//        Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
//        context.startActivity(call);
        Intent callIntent = new Intent(Intent.ACTION_CALL); //use ACTION_CALL class
        callIntent.setData( Uri.parse("tel:" + tel));    //this is the phone number calling
        //check permission
        //If the device is running Android 6.0 (API level 23) and the app's targetSdkVersion is 23 or higher,
        //the system asks the user to grant approval.
        try{
                context.startActivity(callIntent);  //call activity and make phone call
            }
            catch (android.content.ActivityNotFoundException ex){
                Toast.makeText(context, "did not work" + "\n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
    }

}
