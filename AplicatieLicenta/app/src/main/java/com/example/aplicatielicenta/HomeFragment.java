package com.example.aplicatielicenta;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> fullProductList; // lista completă (toate item-urile)
    private EditText searchBar;
    private ImageView searchIcon;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Legăm view-urile
        recyclerView = view.findViewById(R.id.recyclerView);
        searchBar    = view.findViewById(R.id.search_bar);
        searchIcon   = view.findViewById(R.id.search_icon);
        tabLayout    = view.findViewById(R.id.tabLayout);

        // Inițializăm lista completă cu câteva produse de test
        fullProductList = new ArrayList<>();
        fullProductList.add(new Product("Book", "Non-Food", 5.3, R.drawable.ic_launcher_background));
        fullProductList.add(new Product("Apple", "Food", 2.5, R.drawable.ic_launcher_background));
        fullProductList.add(new Product("Bread", "Food", 1.2, R.drawable.ic_launcher_background));
        fullProductList.add(new Product("Notebook", "Non-Food", 3.8, R.drawable.ic_launcher_background));

        // Setăm adapterul pe RecyclerView
        productAdapter = new ProductAdapter(fullProductList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(productAdapter);


        // Listener pentru selectarea tab-urilor
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabText = tab.getText().toString();
                filterByTab(tabText);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // De obicei, aceeași acțiune ca la onTabSelected
                String tabText = tab.getText().toString();
                filterByTab(tabText);
            }
        });

        // Search live (ori de câte ori se tastează)
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });



        return view;
    }

    // Filtrare după Tab (All / Food / Non-Food)
    private void filterByTab(String category) {
        if (category.equalsIgnoreCase("All")) {
            // Afișăm tot
            productAdapter.updateList(fullProductList);
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product product : fullProductList) {
                if (product.getCategory().equalsIgnoreCase(category)) {
                    filtered.add(product);
                }
            }
            productAdapter.updateList(filtered);
        }
    }

    // Filtrare după căutare (nume). Poți combina cu filtrarea de tab dacă vrei.
    private void filterBySearch(String query) {
        // Aflăm tab-ul curent (dacă vrei să combini filtrarea de tab + search)
        String currentTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();

        // 1. Filtrăm întâi după tab
        List<Product> tabFilteredList;
        if (currentTab.equalsIgnoreCase("All")) {
            tabFilteredList = new ArrayList<>(fullProductList);
        } else {
            tabFilteredList = new ArrayList<>();
            for (Product product : fullProductList) {
                if (product.getCategory().equalsIgnoreCase(currentTab)) {
                    tabFilteredList.add(product);
                }
            }
        }

        // 2. Apoi filtrăm după query
        List<Product> finalFilteredList = new ArrayList<>();
        for (Product product : tabFilteredList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                finalFilteredList.add(product);
            }
        }

        // 3. Trimitem lista filtrată către adapter
        productAdapter.updateList(finalFilteredList);
    }
}
