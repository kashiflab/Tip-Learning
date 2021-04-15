package com.christianas.tiplearning.Activities.Basket;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.christianas.tiplearning.Model.Basket;
import com.christianas.tiplearning.Model.Course;
import com.christianas.tiplearning.Model.PurchasedCourse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BasketViewModel extends ViewModel {

    private MutableLiveData<List<Course>> course;
    private List<Course> coursesList;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private List<Basket> baskets;

    private String TAG = "BasketViewModel";

    public BasketViewModel(){
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        baskets = new ArrayList<>();

        course = new MutableLiveData<>();
        coursesList = new ArrayList<>();

        getBasketCourses();
    }


    private void getBasketCourses(){
        firestore.collection("users").document(auth.getCurrentUser().getUid())
                .collection("basket")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot snapshot: queryDocumentSnapshots.getDocuments()){
                    baskets.add(new Basket(snapshot.getId(),snapshot.getString("course_id")));
                }
                getCourses();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG,e.getMessage());
            }
        });
    }


    public MutableLiveData<List<Course>> getBasketCourse() {
        return course;
    }

    private void getCourses(){
        firestore.collection("courses")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                for(Basket course1:baskets) {
                                    if(course1.getCourse_id().equals(document.getId())) {
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


}
