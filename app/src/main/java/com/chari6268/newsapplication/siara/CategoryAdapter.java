package com.chari6268.newsapplication.siara;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chari6268.newsapplication.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.categoryName.setText(category.getName());
        holder.categoryImage.setImageResource(category.getImageResource());

        // Update card appearance based on selection state
        if (category.isSelected()) {
            holder.categoryCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorSelectedCategory));
            holder.categoryName.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.categoryCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorUnselectedCategory));
            holder.categoryName.setTextColor(context.getResources().getColor(R.color.black));
        }

        // Set click listener for category selection
        holder.itemView.setOnClickListener(v -> {
            // Toggle selection state
            category.setSelected(!category.isSelected());

            // Update UI to reflect selection
            if (category.isSelected()) {
                holder.categoryCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorSelectedCategory));
                holder.categoryName.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                holder.categoryCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorUnselectedCategory));
                holder.categoryName.setTextColor(context.getResources().getColor(R.color.black));
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView categoryCard;
        ImageView categoryImage;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryCard = itemView.findViewById(R.id.category_card);
            categoryImage = itemView.findViewById(R.id.category_image);
            categoryName = itemView.findViewById(R.id.category_name);
        }
    }
}