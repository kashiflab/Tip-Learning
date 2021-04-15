package com.christianas.tiplearning.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.christianas.tiplearning.ApiService.APIService;
import com.christianas.tiplearning.Config;
import com.christianas.tiplearning.Model.Api;
import com.christianas.tiplearning.Model.Subscription;
import com.christianas.tiplearning.R;
import com.christianas.tiplearning.Utils.ApiUtils;
import com.christianas.tiplearning.Utils.Utils;
import com.christianas.tiplearning.databinding.ActivitySubscriptionBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionActivity extends AppCompatActivity {

    private ActivitySubscriptionBinding binding;

    private String startDate, endDate;
    private String TAG = "SubscriptionActivity";
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private String userId;
    private APIService apiService;
    private String type, price;

    private static PayPalConfiguration payPalConfiguration = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Subscription");
        setSupportActionBar(binding.toolbar);

        apiService = ApiUtils.getAPIService();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();

        startPayPalService();

        binding.weekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "weekly";
                price = "100";
                payPalPayment();
            }
        });
        binding.monthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "monthly";
                price = "150";
                setSubscription("monthly","150");
            }
        });
        binding.yearly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "yearly";
                price = "200";
                setSubscription("yearly","200");
            }
        });
    }

    private void calculateNewDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

        Date date = new Date();

        Log.i(TAG,date+": oldDate");

        startDate = formatter.format(date);

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(date);               // sets calendar time/date
        if(type.equals("weekly")){
            cal.add(Calendar.WEEK_OF_MONTH, 1);      // adds one hour
        }
        else if(type.equals("monthly")){

            cal.add(Calendar.WEEK_OF_MONTH, 4);      // adds one hour
        }else {
            cal.add(Calendar.YEAR, 1);      // adds one hour
        }
        String newDate = new SimpleDateFormat("yyyy/MM/dd").format(cal.getTime());
        Log.i(TAG,newDate +": newDate");

        endDate = newDate;

    }
    private void setSubscription(String type,String price) {
        calculateNewDate();

        Map<String,Object> map = new HashMap<>();
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("type",type);
        map.put("price",price);
        map.put("currency","usd");
        map.put("isSubscribed",true);

        firestore.collection("users").document(userId).update(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showDialog();
                }else{
                    Toast.makeText(SubscriptionActivity.this, "Not Subscribed", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    private void startPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,payPalConfiguration);
        startService(intent);
    }


    private void payPalPayment() {
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal("120"),
                "USD",getString(R.string.app_name),PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,payPalConfiguration);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(intent,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymenDetails = confirmation.toJSONObject().toString(4);
                        Log.i("paymentDetails",paymenDetails);
                        String state = confirmation.toJSONObject().getJSONObject("response").getString("state");
                        String created_at = confirmation.toJSONObject().getJSONObject("response").getString("create_time");
                        String id = confirmation.toJSONObject().getJSONObject("response").getString("id");
                        if(state.equals("approved")) {

                            Utils.initpDialog(this,"Please wait...");
                            Utils.showpDialog();
                            setSubscription(type,price);
                        }else{
                            Toast.makeText(this, "Cannot process payment", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
            Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
    }


    private void showDialog(){
        Utils.hidepDialog();
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Subscribed")
                .setContentText("You have successfully subscribed.")
                .setConfirmText("Okay")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        SubscriptionActivity.super.onBackPressed();
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();
    }

}