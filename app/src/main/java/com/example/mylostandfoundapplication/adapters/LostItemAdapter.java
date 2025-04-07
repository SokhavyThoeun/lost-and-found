package com.example.mylostandfoundapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylostandfoundapplication.R;
import com.example.mylostandfoundapplication.models.LostItem;
import com.example.mylostandfoundapplication.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class LostItemAdapter extends RecyclerView.Adapter<LostItemAdapter.LostItemViewHolder> {
    private List<LostItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LostItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LostItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost, parent, false);
        return new LostItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LostItemViewHolder holder, int position) {
        LostItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<LostItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    class LostItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView itemTitle;
        private TextView itemDescription;
        private TextView itemLocation;
        private TextView itemDate;

        public LostItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemLocation = itemView.findViewById(R.id.itemLocation);
            itemDate = itemView.findViewById(R.id.itemDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        public void bind(LostItem item) {
            itemTitle.setText(item.getTitle());
            itemDescription.setText(item.getDescription());
            itemLocation.setText(item.getLocation());
            itemDate.setText(item.getDate());

            if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
                itemImage.setImageBitmap(ImageUtils.base64ToBitmap(item.getImageBase64()));
            } else {
                itemImage.setImageResource(R.drawable.ic_image_placeholder);
            }
        }
    }
} 