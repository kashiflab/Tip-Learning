package com.christianas.tiplearning.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.christianas.tiplearning.ApiService.APIService;
import com.christianas.tiplearning.App;
import com.christianas.tiplearning.Config;
import com.christianas.tiplearning.MainViewModel;
import com.christianas.tiplearning.Model.Api;
import com.christianas.tiplearning.Model.Course;
import com.christianas.tiplearning.Model.PurchasedCourse;
import com.christianas.tiplearning.R;
import com.christianas.tiplearning.Utils.ApiUtils;
import com.christianas.tiplearning.Utils.Utils;
import com.christianas.tiplearning.VideoActivity;
import com.christianas.tiplearning.databinding.ActivityCourseDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.grpc.okhttp.internal.Util;
import me.ibrahimsn.lib.OnItemSelectedListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.VideoInfo;

public class CourseDetailActivity extends AppCompatActivity {

    private ActivityCourseDetailBinding binding;

    private String currency, courseId;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private String TAG = "CourseDetailActivity";

    private String email, title;

    private APIService apiService;

    private MainViewModel mainViewModel;

    private String purchasedCode="", purchasedId = "";

    private boolean isSubscribed = false, isPurchased = false;

    private static PayPalConfiguration payPalConfiguration = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    private boolean isEmailVerified;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isSubscribed = App.isIsSubscribed();

        Utils.initpDialog(this,"Loading...");
        Utils.showpDialog();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        checkBasket();

        title = getIntent().getStringExtra("title");

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        isPurchased = getIntent().getBooleanExtra("isPurchasedCourse",false);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        email = auth.getCurrentUser().getEmail();
        isEmailVerified = auth.getCurrentUser().isEmailVerified();

        binding.videoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPurchased || isSubscribed) {
                    VideoInfo videoInfo = new VideoInfo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
                            .setTitle("Demo video") //config title
                            .setShowTopBar(true) //show mediacontroller top bar
                            .setPortraitWhenFullScreen(true);//portrait when full screen

                    GiraffePlayer.play(CourseDetailActivity.this, videoInfo);
                }else{
                    Utils.showSnackBar(binding.mainLayout,"Get a subscription or pay now to watch video");
                }
            }
        });

        courseId = getIntent().getStringExtra("id");
        currency = getIntent().getStringExtra("currency");
        String img = getIntent().getStringExtra("img");

        if(isPurchased || isSubscribed){
            binding.bottomBar.setVisibility(View.GONE);
        }

        Picasso.get().load(img).into(binding.courseImage);
        binding.title.setText(title);
        binding.shortDesc.setText(getIntent().getStringExtra("short_desc"));
        binding.price.setText(currency+getIntent().getStringExtra("price"));
        binding.longDesc.setText(getIntent().getStringExtra("long_desc"));

        apiService = ApiUtils.getAPIService();

        mainViewModel.getPurchasedCourse().observe(this, new Observer<List<Course>>() {
            @Override
            public void onChanged(List<Course> courses) {
                for(Course course:courses){
                    if(course.getId().equals(courseId)){
                        binding.bottomBar.setVisibility(View.GONE);
                    }
                }
                Utils.hidepDialog();
            }
        });

        mainViewModel.getPurchasedCoursesLive().observe(this, new Observer<List<PurchasedCourse>>() {
            @Override
            public void onChanged(List<PurchasedCourse> purchasedCourses) {
                for(PurchasedCourse purchasedCourse:purchasedCourses){
                    if(purchasedCourse.getStatus().equals("1")&&purchasedCourse.getCourse_id().equals(courseId)){
                        purchasedCode = purchasedCourse.getCode();
                        purchasedId = purchasedCourse.getId();
                    }else if(purchasedCourse.getStatus().equals("0")&&purchasedCourse.getCourse_id().equals(courseId)){
                        binding.bottomBar.setVisibility(View.GONE);
                    }
                }
            }
        });


        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {

                switch (i){
                    case 0:
                        if(isEmailVerified) {
                            if (basket.contains(courseId)) {
                                Utils.showSnackBar(binding.mainLayout, "Already added");
                            } else {
                                addToBasket();
                            }
                        }else{
                            Utils.showSnackBar(binding.mainLayout,"Please verify your email");
                        }
                    break;
                    case 1:
                        if(isEmailVerified) {
                            payPalPayment();
                        }else{
                            Utils.showSnackBar(binding.mainLayout,"Please verify your email");
                        }
                        break;
                    case 2:
                        if(isEmailVerified) {
                            showCustomDialog();
                        }else{
                            Utils.showSnackBar(binding.mainLayout,"Please verify your email");
                        }
                        break;
                }
                return false;
            }
        });

        startPayPalService();

    }

    private void addToBasket() {

        Map<String,Object> map = new HashMap<>();
        map.put("course_id",courseId);

        firestore.collection("users").document(auth.getCurrentUser().getUid())
                .collection("basket")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i(TAG,"added");
                        Utils.showSnackBar(binding.mainLayout,"Added to basket");
                        checkBasket();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG,e.getMessage());
            }
        });
    }

    private AlertDialog dialog;
    private void showCustomDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.enter_code,null);
        dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        Button codeBtn = view.findViewById(R.id.codeBtn);
        TextInputEditText code = view.findViewById(R.id.code);

        codeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(code.getText().toString().length()==4) {
                    checkCode(code.getText().toString());
                }else{
                    Toast.makeText(CourseDetailActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    private void checkCode(String code) {
        if(purchasedCode.equals(code)){
            Utils.initpDialog(this,"Please wait...");
            Utils.showpDialog();
            updateFirestorePurchasedStatus();
        }else{
            Toast.makeText(CourseDetailActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFirestorePurchasedStatus() {
        Map<String,Object> map = new HashMap<>();
        map.put("status","0");
        firestore.collection("users").document(auth.getCurrentUser().getUid())
                .collection("purchasedCourses").document(purchasedId)
                .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Utils.hidepDialog();
                if(task.isSuccessful()){
                    binding.bottomBar.setVisibility(View.GONE);
                    Utils.showSnackBar(binding.mainLayout,"Successfully Unlocked");
                }else{
                    Utils.showSnackBar(binding.mainLayout,"Some error occurred");
                }
            }
        });
    }


    private void startPayPalService() {
        Intent intent = new Intent(this,PayPalService.class);
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
                            setPurchasedCourse(id, state);
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

    private void setPurchasedCourse(String id, String state) {

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

                        sendEmail(documentReference.getId());

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
                .setContentText("4 digit code is sent to your registered email:"+email+"\nPlease check your email.")
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

    private void sendEmail(String id){
        apiService.sendEmail(email,title).enqueue(new Callback<Api>() {
            @Override
            public void onResponse(Call<Api> call, Response<Api> response) {
                Utils.hidepDialog();
                if(response.isSuccessful()){
                    if(response.body().getAction().equals("false")){
                        showDialog();
                        saveCode(id,response.body().getCode());
                    }else{
                        Utils.showSnackBar(binding.mainLayout,response.body().getMessage());
                    }
                }else{
                    Utils.showSnackBar(binding.mainLayout,"Some error occurred!");
                }
            }

            @Override
            public void onFailure(Call<Api> call, Throwable t) {
                Utils.hidepDialog();
                Utils.showSnackBar(binding.mainLayout,"No Internet");
            }
        });
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();
    }
    private List<String> basket;
    private void checkBasket(){
        firestore.collection("users").document(auth.getCurrentUser().getUid())
                .collection("basket")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                basket = new ArrayList<>();
                for(DocumentSnapshot documentReference:queryDocumentSnapshots.getDocuments()){
                    basket.add(documentReference.getString("course_id"));
                }
            }
        });

    }
}