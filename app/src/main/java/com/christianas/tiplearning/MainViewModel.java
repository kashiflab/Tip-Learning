package com.christianas.tiplearning;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.christianas.tiplearning.Model.Basket;
import com.christianas.tiplearning.Model.Course;
import com.christianas.tiplearning.Model.PurchasedCourse;
import com.christianas.tiplearning.Model.Subscription;
import com.christianas.tiplearning.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer2.VideoView;

public class MainViewModel extends ViewModel {

    private MutableLiveData<List<Course>> model;
    private List<Course> categories;


    private MutableLiveData<List<Course>> course;
    private List<Course> coursesList;

    private MutableLiveData<List<PurchasedCourse>> purchasedCoursesLive;
    private List<PurchasedCourse> purchasedCourses;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private MutableLiveData<List<User>> subscriptionLive;
    private List<User> subscription;

    private String TAG = "MainViewModel";

    public MutableLiveData<List<Course>> getModel() {
        return model;
    }

    public MutableLiveData<List<User>> getSubscriptionLive() {
        return subscriptionLive;
    }

    public MainViewModel(){
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        subscriptionLive = new MutableLiveData<>();
        subscription = new ArrayList<>();

        purchasedCoursesLive = new MutableLiveData<>();
        purchasedCourses = new ArrayList<>();
        course = new MutableLiveData<>();
        coursesList = new ArrayList<>();
        model = new MutableLiveData<>();
        categories = new ArrayList<>();

        getSubscription();
        fetchPurchasedCourses();
        getCourses();
    }

    private void fetchPurchasedCourses() {
        firestore.collection("users")
                .document(auth.getCurrentUser().getUid()).collection("purchasedCourses")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        purchasedCourses.add(new PurchasedCourse(document.getId(),document.getString("created_at"),
                                document.getString("course_id"),document.getString("purchased_id"),
                                document.getString("state"),document.getString("status"),
                                document.getString("code")));
                    }
                    purchasedCoursesLive.setValue(purchasedCourses);
                    getPurchasedCourses();
                }
            }
        });
    }

    public MutableLiveData<List<PurchasedCourse>> getPurchasedCoursesLive() {
        return purchasedCoursesLive;
    }

    public MutableLiveData<List<Course>> getPurchasedCourse() {
        return course;
    }

    private void getPurchasedCourses(){
        firestore.collection("courses")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                for(PurchasedCourse course1:purchasedCourses) {
                                    if(course1.getCourse_id().equals(document.getId()) && course1.getStatus().equals("0")) {
                                        coursesList.add(new Course(document.getId(), document.getString("title"),
                                                document.getString("price"), document.getString("img"),
                                                document.getString("short_desc"), document.getString("long_desc"),
                                                document.getString("created_at"), document.getString("currency")));
                                    }
                                }
                            }
                            course.setValue(coursesList);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void getCourses() {
        firestore.collection("courses")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                categories.add(new Course(document.getId(),document.getString("title"),
                                        document.getString("price"),document.getString("img"),
                                        document.getString("short_desc"),document.getString("long_desc"),
                                        document.getString("created_at"),document.getString("currency")));
                                Log.d(TAG, document.getId() + " => " + document.getData());

                            }
                            model.setValue(categories);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void getSubscription(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(auth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.exists()) {
                        subscription.add(new User(snapshot.getId(),snapshot.getString("first_name"),
                                snapshot.getString("last_name"),snapshot.getString("email"),
                                snapshot.getString("created_at"),
                                snapshot.getString("startDate"),
                                snapshot.getString("endDate"),snapshot.getString("type"),
                                snapshot.getString("price"),snapshot.getString("currency"),
                                snapshot.getBoolean("isSubscribed"),snapshot.getString("subscriptionCode")));
//                        }
                        subscriptionLive.setValue(subscription);
                        Log.d("TAG", snapshot.getString("first_name")); //Print the name
                    } else {
                        Log.d("MainActivity", "No such document");
                    }
                } else {
                    Log.d("MainActivity", "get failed with ", task.getException());
                }
            }
        });

    }

}
