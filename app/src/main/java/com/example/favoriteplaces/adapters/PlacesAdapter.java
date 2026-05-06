package com.example.favoriteplaces.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.favoriteplaces.databinding.ItemPlaceBinding;
import com.example.favoriteplaces.models.PlaceModel;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {

    public interface OnPlaceClickListener {
        void onPlaceClick(PlaceModel place, int position);
        void onPlaceLongClick(PlaceModel place, int position);
    }

    private final Context context;
    private List<PlaceModel> placeList;
    private OnPlaceClickListener listener;

    public PlacesAdapter(Context context, List<PlaceModel> placeList) {
        this.context   = context;
        this.placeList = placeList;
    }

    public void setOnPlaceClickListener(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<PlaceModel> newList) {
        this.placeList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        placeList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, placeList.size());
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlaceBinding binding = ItemPlaceBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new PlaceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        PlaceModel place = placeList.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return placeList != null ? placeList.size() : 0;
    }

    // ─── ViewHolder ──────────────────────────────────────────────────────────────

    class PlaceViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlaceBinding binding;

        PlaceViewHolder(ItemPlaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PlaceModel place) {
            binding.tvPlaceTitle.setText(place.getTitle());
            binding.tvPlaceAddress.setText(
                    place.getAddress() != null && !place.getAddress().isEmpty()
                    ? place.getAddress()
                    : String.format("%.4f, %.4f", place.getLatitude(), place.getLongitude()));

            // Load thumbnail
            if (place.getImageUri() != null && !place.getImageUri().isEmpty()) {
                try {
                    binding.ivPlaceThumbnail.setImageURI(Uri.parse(place.getImageUri()));
                } catch (Exception e) {
                    binding.ivPlaceThumbnail.setImageResource(
                            android.R.drawable.ic_menu_gallery);
                }
            } else {
                binding.ivPlaceThumbnail.setImageResource(
                        android.R.drawable.ic_menu_gallery);
            }

            // Click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceClick(place, getAdapterPosition());
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceLongClick(place, getAdapterPosition());
                }
                return true;
            });
        }
    }
}
