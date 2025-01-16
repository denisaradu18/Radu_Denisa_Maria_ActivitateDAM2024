package com.example.seminar13;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddObjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_object);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setContentView(R.layout.activity_add_object);

        EditText etBookTitle = findViewById(R.id.et_book_title);
        EditText etBookAuthor = findViewById(R.id.et_book_author);
        EditText etBookYear = findViewById(R.id.et_book_year);
        CheckBox cbAvailableOnline = findViewById(R.id.cb_available_online);
        Button btnAddBook = findViewById(R.id.btn_add_book);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference booksRef = database.getReference("books");

        btnAddBook.setOnClickListener(v -> {
            String title = etBookTitle.getText().toString();
            String author = etBookAuthor.getText().toString();
            String yearStr = etBookYear.getText().toString();
            boolean isAvailableOnline = cbAvailableOnline.isChecked();

            if (!title.isEmpty() && !author.isEmpty() && !yearStr.isEmpty()) {
                try {
                    int year = Integer.parseInt(yearStr);
                    Book book = new Book(title, author, year, isAvailableOnline);
                    booksRef.push().setValue(book)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Carte adăugată cu succes!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Anul trebuie să fie un număr valid!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Toate câmpurile sunt obligatorii!", Toast.LENGTH_SHORT).show();
            }
        });

        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Toast.makeText(AddObjectActivity.this, "S-au produs modificări în baza de date!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddObjectActivity.this, "Eroare la ascultarea modificărilor: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}