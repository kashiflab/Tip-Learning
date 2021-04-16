package com.christianas.tiplearning.Fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.christianas.tiplearning.Activities.Basket.BasketActivity;
import com.christianas.tiplearning.Activities.LoginActivity;
import com.christianas.tiplearning.Activities.SubscriptionActivity;
import com.christianas.tiplearning.App;
import com.christianas.tiplearning.MainViewModel;
import com.christianas.tiplearning.Model.Subscription;
import com.christianas.tiplearning.Model.User;
import com.christianas.tiplearning.Utils.Utils;
import com.christianas.tiplearning.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private FirebaseAuth auth;

    private MainViewModel mainViewModel;
    private User user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(getLayoutInflater());

        Utils.initpDialog(getActivity(), "Please wait...");
        Utils.showpDialog();

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        auth = FirebaseAuth.getInstance();

        binding.materialCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), BasketActivity.class));
            }
        });

        mainViewModel.getSubscriptionLive().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                user = users.get(0);
                binding.username.setText(users.get(0).getFirst_name());
                binding.email.setText(users.get(0).getEmail());

                binding.typeTv.setText(users.get(0).getType());
                binding.startDateTv.setText(users.get(0).getStartDate());
                binding.endDateTv.setText(users.get(0).getEndDate());
                binding.priceTv.setText(users.get(0).getPrice());


                calculateDateDiff(users.get(0).getEndDate(),users.get(0).isSubscribed());

                Utils.hidepDialog();
                if (users.get(0).isSubscribed()) {
                    binding.subscribe.setText("Subscribed");
                    App.setIsSubscribed(true);
                } else {
                    App.setIsSubscribed(false);
                }
            }
        });

        binding.subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.isIsSubscribed()) {

                    Utils.showSnackBar(binding.mainLayout, "Already Subscribed");
                } else {
                    startActivity(new Intent(getActivity(), SubscriptionActivity.class)
                    .putExtra("email",user.getEmail()));
                }
            }
        });

        binding.logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                getActivity().finishAffinity();
            }
        });

        if (!auth.getCurrentUser().isEmailVerified()) {
            binding.resendEmail.setVisibility(View.VISIBLE);
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Verification")
                    .setContentText("Please verify your email address.")
                    .setConfirmText("Verify")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {

                            sDialog.dismissWithAnimation();
                        }
                    })
                    .show();
        } else {
            binding.resendEmail.setVisibility(View.GONE);
        }

        binding.resendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Utils.showSnackBar(binding.mainLayout, "Email sent. Please check your email!");
                        } else {
                            Utils.showSnackBar(binding.mainLayout, "Some error occurred. Please Try again!");
                        }
                    }
                });
            }
        });


        return binding.getRoot();

    }

    private void calculateDateDiff(String endDate, boolean isSubscribed) {

        try {
            Date date = new Date(System.currentTimeMillis());

            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

            int diff = (int) getDateDiff(new SimpleDateFormat("yyyy/MM/dd"), endDate, dateFormat1.format(date));

            if (diff > 0 && isSubscribed) {
                Map<String, Object> map = new HashMap<>();
                map.put("startDate", "");
                map.put("endDate", "");
                map.put("type", "");
                map.put("price", "");
                map.put("currency", "");
                map.put("isSubscribed", false);
                map.put("subscriptionCode", "");

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("users").document(auth.getCurrentUser().getUid())
                        .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Subscription Details")
                                    .setContentText("Your subscription is ended. Please resubscribe to enjoy it. Thanks")
                                    .setConfirmButton("Okay", new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismissWithAnimation();
                                        }
                                    }).show();
                            Log.i("TAG", "Subscription Ended");
                        } else {
                            Log.i("TAG", "error");
                        }
                    }
                });
            }
            if (diff == 0 && isSubscribed) {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Subscription Details")
                        .setContentText("It's your last day of subscription. Please subscribe again to enjoy your videos.")
                        .setConfirmButton("Okay", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        }).show();
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.i("Exception",e.getMessage());
        }

    }

    /**
     * Get a diff between two dates
     *
     * @param oldDate the old date
     * @param newDate the new date
     * @return the diff value, in the days
     */
    public static long getDateDiff(SimpleDateFormat format, String oldDate, String newDate) {
        try {
            return TimeUnit.DAYS.convert(format.parse(newDate).getTime() - format.parse(oldDate).getTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        auth.getCurrentUser().reload();
    }
}