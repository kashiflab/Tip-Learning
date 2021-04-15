package com.christianas.tiplearning.Activities.Basket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.christianas.tiplearning.Adapter.BasketAdapter;
import com.christianas.tiplearning.ApiService.APIService;
import com.christianas.tiplearning.Config;
import com.christianas.tiplearning.MainViewModel;
import com.christianas.tiplearning.Model.Api;
import com.christianas.tiplearning.Model.Basket;
import com.christianas.tiplearning.Model.Course;
import com.christianas.tiplearning.Model.PurchasedCourse;
import com.christianas.tiplearning.R;
import com.christianas.tiplearning.Utils.ApiUtils;
import com.christianas.tiplearning.Utils.Utils;
import com.christianas.tiplearning.databinding.ActivityBasketBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BasketActivity extends AppCompatActivity {

    private ActivityBasketBinding binding;
    private BasketAdapter adapter;

    private BasketViewModel basketViewModel;
    private int subTotal=0;
    private int tax = 0;
    private int total = 0;
    private String currency = "USD";

    private int totalSize = 0;

    private String TAG = "BasketActivity";

    private String email;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userId;

    private APIService apiService;

    private static PayPalConfiguration payPalConfiguration = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    private List<Course> coursesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        basketViewModel = new ViewModelProvider(this).get(BasketViewModel.class);

        startPayPalService();

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        email = auth.getCurrentUser().getEmail();
        firestore = FirebaseFirestore.getInstance();
        apiService = ApiUtils.getAPIService();

        binding.basketRV.setLayoutManager(new LinearLayoutManager(this));
        binding.basketRV.setHasFixedSize(true);
        adapter = new BasketAdapter(this);
        binding.basketRV.setAdapter(adapter);

        basketViewModel.getBasketCourse().observe(this, new Observer<List<Course>>() {
            @Override
            public void onChanged(List<Course> courses) {
                coursesList = courses;
                adapter.setCourses(courses);
                totalSize = courses.size();
                for(Course course:courses){
                    subTotal += Integer.parseInt(course.getPrice());

                }
                total = subTotal + tax;
                binding.total.setText(String.valueOf(total));
            }
        });

        binding.completeCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(totalSize>0) {
                    payPalPayment();
                }else{
                    Toast.makeText(BasketActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void payPalPayment() {
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(total),
                currency.toUpperCase(),getString(R.string.app_name),PayPalPayment.PAYMENT_INTENT_SALE);

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
                            for(int i=0;i<coursesList.size();i++) {
                                setPurchasedCourse(coursesList.get(i).getId(),id, state,i,coursesList.get(i).getTitle());
                            }
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

    private void setPurchasedCourse(String courseId, String id, String state, int size,String courseTitle) {

        Map<String, Object> user = new HashMap<>();
        user.put("purchased_id", id);
        user.put("course_id", courseId);
        user.put("created_at", Utils.getCurrentTimeStamp());
        user.put("state", state);
        //0 for active 1 for deactive
        user.put("status","1");

        // Add a new document with a generated ID
        firestore.collection("users").document(auth.getCurrentUser().getUid())
                .collection("purchasedCourses")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                        sendEmail(documentReference.getId(),size,courseTitle);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.hidepDialog();
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void showDialog(){

        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Email Sent")
                .setContentText("4 digit code is sent to your registered email: "+email+"\nPlease check your email.")
                .setConfirmText("Okay")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    private void saveCode(String id,String code){
        Map<String, Object> map = new HashMap<>();
        map.put("code",code);
        firestore.collection("users").document(auth.getCurrentUser().getUid())
                .collection("purchasedCourses").document(id)
                .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG,"code saved");
                }
            }
        });
    }

    private void sendEmail(String id,int size,String courseName){
        apiService.sendEmail(email,courseName).enqueue(new Callback<Api>() {
            @Override
            public void onResponse(Call<Api> call, Response<Api> response) {
                Utils.hidepDialog();
                if(response.isSuccessful()){
                    if(response.body().getAction().equals("false")){
                        if(size==totalSize) {
                            showDialog();
                        }
                        saveCode(id, response.body().getCode());

                    }else{
                        Log.i(TAG,response.body().getMessage());
                    }
                }else{
                    Log.i(TAG,"Some error occurred");
                }
            }

            @Override
            public void onFailure(Call<Api> call, Throwable t) {
                Utils.hidepDialog();
                Log.i(TAG,"Some error occurred");
            }
        });
    }
    private void startPayPalService() {
        Intent intent = new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,payPalConfiguration);
        startService(intent);
    }
    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();
    }
}