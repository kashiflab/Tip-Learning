package com.christianas.tiplearning.Fragments;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.christianas.tiplearning.Adapter.CourseAdapter;
import com.christianas.tiplearning.MainViewModel;
import com.christianas.tiplearning.Model.Course;
import com.christianas.tiplearning.R;
import com.christianas.tiplearning.databinding.FragmentTutorialsBinding;

import java.util.List;

public class TutorialsFragment extends Fragment {

    private FragmentTutorialsBinding binding;

    private CourseAdapter adapter;
    private MainViewModel mainViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTutorialsBinding.inflate(getLayoutInflater());

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.courseRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.courseRecyclerView.setHasFixedSize(true);
        adapter = new CourseAdapter(getActivity(),false);
        binding.courseRecyclerView.setAdapter(adapter);


        mainViewModel.getModel().observe(getViewLifecycleOwner(), new Observer<List<Course>>() {
            @Override
            public void onChanged(List<Course> courses) {
                adapter.setCourseList(courses);
            }
        });

        //searchview
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                TextChanged(newText);
                return true;
            }
        });
        return binding.getRoot();
    }
    public void TextChanged(String newText){
        if(TextUtils.isEmpty(newText)){
            adapter.getFilter().filter("");
        }else {
            adapter.getFilter().filter(newText);
        }
    }

}