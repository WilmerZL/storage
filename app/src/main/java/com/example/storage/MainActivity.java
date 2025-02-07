package com.example.storage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Button btnSelectImage, btnUploadImage;
    private ProgressBar progressBar;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<String> imageUrls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Inicialización de Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("imagenes");
        // Referencias a los elementos UI
        imageView = findViewById(R.id.imageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageUrls = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageUrls, storageRef);
        recyclerView.setAdapter(imageAdapter);
        // Botón para seleccionar imagen
        btnSelectImage.setOnClickListener(v -> openFileChooser());
        // Botón para subir imagen
        btnUploadImage.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImage();
            } else {
                Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            }
        });
        // Cargar imágenes existentes
        loadImages();
    }
    // Método para abrir el selector de archivos
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    // Método para manejar el resultado de la selección de imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }
    // Método para subir la imagen seleccionada a Firebase Storage
    private void uploadImage() {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    imageAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    imageView.setImageResource(R.drawable.img);
                    imageView.setContentDescription("Agregar imágenes");
                    Toast.makeText(MainActivity.this, "Imagen subida", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }
    // Método para cargar imágenes desde Firebase Storage
    private void loadImages() {
        storageRef.listAll().addOnSuccessListener(listResult -> {
            imageUrls.clear();
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    imageAdapter.notifyDataSetChanged();
                });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(MainActivity.this, "Error al cargar imágenes", Toast.LENGTH_SHORT).show()
        );
    }
}

