package com.christianas.tiplearning.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.christianas.tiplearning.Activities.CourseDetailActivity;
import com.christianas.tiplearning.Model.Course;
import com.christianas.tiplearning.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> implements Filterable {

    private Context context;
    private boolean isPurchasedCourse;

    private List<Course> courseList = new ArrayList<>();

    public CourseAdapter(Context context,boolean isPurchasedCourse){
        this.context = context;
        this.isPurchasedCourse = isPurchasedCourse;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_item,parent,false);
        return new ViewHolder(view);
    }

    public void setCourseList(List<Course> courseList){
        this.courseList = courseList;
        filteredCategoryList = courseList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Course course = courseList.get(position);
        if(isPurchasedCourse){
            holder.price.setVisibility(View.GONE);
        }else{
            holder.price.setVisibility(View.VISIBLE);
        }
        String currency = "";
        if(course.getCurrency().equals("usd")){
            currency = "$";
        }else if(course.getCurrency().equals("eur")){
            currency = "€";
        }

        Picasso.get().load(course.getImg()).into(holder.courseImage);

        holder.title.setText(course.getTitle());
        holder.price.setText(currency+course.getPrice());
        holder.desc.setText(course.getShort_desc());

        String finalCurrency = currency;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, CourseDetailActivity.class)
                .putExtra("id",course.getId()).putExtra("title",course.getTitle())
                        .putExtra("price",course.getPrice()).putExtra("short_desc",course.getShort_desc())
                        .putExtra("long_desc",course.getLong_desc()).putExtra("img",course.getImg())
                        .putExtra("created_at",course.getCreated_at()).putExtra("currency", finalCurrency)
                .putExtra("isPurchasedCourse",isPurchasedCourse));
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView courseImage;
        private TextView title, price, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            courseImage = itemView.findViewById(R.id.courseImage);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            desc = itemView.findViewById(R.id.desc);
        }
    }
    private List<Course> filteredCategoryList;

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Course> filteredList = new ArrayList<>();

            if(charSequence.toString().isEmpty()){
                courseList = filteredCategoryList;
                filteredList.addAll(courseList);
            }else {
                for(Course course: courseList) {
                    if(course.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase()) ||
                            course.getShort_desc().toLowerCase().contains(charSequence.toString().toLowerCase()) ||
                            course.getLong_desc().toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filteredList.add(course);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            categoryModels.clear();
//            categoryModels.addAll((Collection<? extends CategoryModel>) filterResults.values);
            courseList = (List<Course>) filterResults.values;
            notifyDataSetChanged();
        }
    };
}
