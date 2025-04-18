package com.chari6268.newsapplication.siara;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chari6268.newsapplication.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectionActivity extends AppCompatActivity {
    private RecyclerView categoriesRV;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private Button continueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.siara_activity_category_selection);

        categoriesRV = findViewById(R.id.categories_recycler_view);
        continueBtn = findViewById(R.id.continue_btn);

        // Initialize category list
        categoryList = new ArrayList<>();
        categoryList.add(new Category(1, "Politics", false, R.drawable.ic_politics));
        categoryList.add(new Category(2, "Technology", false, R.drawable.ic_technology));
        categoryList.add(new Category(3, "Sports", false, R.drawable.ic_sports));
        categoryList.add(new Category(4, "Entertainment", false, R.drawable.ic_entertainment));
        categoryList.add(new Category(5, "Business", false, R.drawable.ic_business));
        categoryList.add(new Category(6, "Health", false, R.drawable.ic_health));

        // Set up adapter
        categoryAdapter = new CategoryAdapter(categoryList, this);
        categoriesRV.setLayoutManager(new GridLayoutManager(this, 2));
        categoriesRV.setAdapter(categoryAdapter);

        continueBtn.setOnClickListener(v -> {
            List<Category> selectedCategories = new ArrayList<>();
            for (Category category : categoryList) {
                if (category.isSelected()) {
                    selectedCategories.add(category);
                }
            }

            if (selectedCategories.isEmpty()) {
                Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save selected categories to SharedPreferences
            saveSelectedCategories(selectedCategories);

            // Navigate to News Feed
            startActivity(new Intent(CategorySelectionActivity.this, NewsFeedActivity.class));
            finish();
        });
    }

    private void saveSelectedCategories(List<Category> selectedCategories) {
        SharedPreferences preferences = getSharedPreferences("NewsAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(selectedCategories);
        editor.putString("selectedCategories", json);
        editor.apply();
    }
}