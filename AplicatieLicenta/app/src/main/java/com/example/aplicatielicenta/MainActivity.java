package com.example.aplicatielicenta;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.aplicatielicenta.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Mutăm operația intensivă (ex: încărcarea datelor) într-un thread de fundal.
        new Thread(() -> {
            // Operație intensivă pe fundal, de exemplu:
            performHeavyOperation();

            // După finalizarea operației, actualizează UI-ul.
            runOnUiThread(() -> {
                replaceFragment(new HomeFragment());
            });
        }).start();

        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.map) {
                replaceFragment(new MapFragment());
                return true;
            } else if (itemId == R.id.inbox) {
                replaceFragment(new InboxFragment());
                return true;
            } else if (itemId == R.id.account) {
                replaceFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        binding.fab.setOnClickListener(v -> {
            // Construiește un dialog cu două opțiuni
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose Product Type");
            builder.setItems(new CharSequence[]{"Food", "Non-Food"}, (dialog, which) -> {
                if (which == 0) {
                    // Utilizatorul a ales "Food"
                    Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
                    startActivity(intent);
                } else {
                    // Utilizatorul a ales "Non-Food"
                    Intent intent = new Intent(MainActivity.this, AddNonFoodActivity.class);
                    startActivity(intent);
                }
            });
            builder.show();
        });
    }

    // Metodă ce simulează o operație intensivă de fundal
    private void performHeavyOperation() {
        // De exemplu, o operație care durează câteva secunde.
        try {
            Thread.sleep(3000); // Simulează o întârziere de 3 secunde
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Aici ai putea să încarci date dintr-o bază de date sau din rețea
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}