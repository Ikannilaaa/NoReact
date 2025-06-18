package com.example.noreact.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.noreact.R;
import com.example.noreact.adapter.HistoryAdapter;
import com.example.noreact.model.HistoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyStateLayout;
    private HistoryAdapter historyAdapter;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        initFirebase();
        initViews(root);
        setupRecyclerView();
        setupSwipeRefresh();
        loadHistoryData();

        return root;
    }

    private void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initViews(View root) {
        recyclerViewHistory = root.findViewById(R.id.recyclerViewHistory);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        emptyStateLayout = root.findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(getContext());
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.setAdapter(historyAdapter);

        // Set click listener untuk item
        historyAdapter.setOnItemClickListener(historyItem -> {
            // Buka detail history
            Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
            intent.putExtra("history_id", historyItem.getId());
            startActivity(intent);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadHistoryData);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.secondary_color
        );
    }

    private void loadHistoryData() {
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        firestore.collection("history")
                .whereEqualTo("userId", userId)
                .orderBy("scanDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HistoryItem> historyList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HistoryItem historyItem = document.toObject(HistoryItem.class);
                        historyItem.setId(document.getId());
                        historyList.add(historyItem);
                    }

                    if (historyList.isEmpty()) {
                        showEmptyState();
                    } else {
                        showHistoryList(historyList);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memuat riwayat: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        recyclerViewHistory.setVisibility(View.GONE);
    }

    private void showHistoryList(List<HistoryItem> historyList) {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerViewHistory.setVisibility(View.VISIBLE);
        historyAdapter.updateHistory(historyList);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data ketika fragment kembali aktif
        loadHistoryData();
    }
}