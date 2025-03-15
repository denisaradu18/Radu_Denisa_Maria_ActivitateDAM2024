package com.example.aplicatielicenta;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

public class AccountFragment extends Fragment {

    private Button btnMyListings, btnFavorites;

    public AccountFragment() {
        // Constructor gol necesar pentru Fragment
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Account");
        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inițializează butoanele
        btnMyListings = view.findViewById(R.id.btn_my_listings);
        btnFavorites = view.findViewById(R.id.btn_favorites);

        // Buton pentru anunțurile utilizatorului
        btnMyListings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyListingsActivity.class);
            startActivity(intent);
        });

        // Buton pentru favorite
        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FavoritesActivity.class);
            startActivity(intent);
        });
    }
}
