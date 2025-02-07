package com.example.storage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<String> imageUrls;
    private StorageReference storageReference;
    public ImageAdapter(List<String> imageUrls, StorageReference storageReference) {
        this.imageUrls = imageUrls;
        this.storageReference = storageReference;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Context context = holder.itemView.getContext();
        // Cargar la imagen con Glide
        Glide.with(context).load(imageUrl).into(holder.imageView);
        // Eliminar imagen de Firebase Storage
        holder.btnDelete.setOnClickListener(v -> {
            // Obtener el nombre del archivo de la URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("%2F") + 3, imageUrl.indexOf("?"));

            // Crear la referencia correcta
            StorageReference imagenesRef = storageReference.child(fileName);
            imagenesRef.delete().addOnSuccessListener(aVoid -> {
                imageUrls.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Imagen eliminada", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e ->
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
            );
        });
    }
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button btnDelete;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}