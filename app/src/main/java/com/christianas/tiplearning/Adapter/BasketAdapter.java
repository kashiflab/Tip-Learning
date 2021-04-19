package com.christianas.tiplearning.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.christianas.tiplearning.Model.Basket;
import com.christianas.tiplearning.Model.Course;
import com.christianas.tiplearning.Model.PurchasedCourse;
import com.christianas.tiplearning.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.ViewHolder> {

    private Context context;
    private List<Course> courses = new ArrayList<>();
    private List<Basket> baskets = new ArrayList<>();
    private FirebaseAuth auth;

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        notifyDataSetChanged();
    }

    public BasketAdapter(Context context){
        auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subs_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Course course = courses.get(position);

        Picasso.get().load(course.getImg()).into(holder.courseImage);
        holder.title.setText(course.getTitle());
        holder.price.setText("Price: "+course.getPrice());

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(Basket basket:baskets) {
                    if(basket.getCourse_id().equals(course.getId())) {
                        deleteFromBasket(basket.getId());
                        break;
                    }
                }
            }
        });

    }

    private void deleteFromBasket(String basketId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(auth.getCurrentUser().getUid()).collection("basket")
                .document(basketId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                notifyDataSetChanged();
                if(task.isSuccessful()){
                    Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void setBasket(List<Basket> baskets) {
        this.baskets = baskets;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView courseImage;
        private TextView title, price, cancel;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cancel = itemView.findViewById(R.id.cancel);
            courseImage = itemView.findViewById(R.id.courseImage);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
        }
    }
}
