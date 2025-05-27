package com.example.noreact.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.noreact.MainActivity;
import com.example.noreact.R;
import com.example.noreact.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
//    private AccountViewModel accountViewModel;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        mAuth = FirebaseAuth.getInstance();

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            binding.textUserEmail.setText(currentUser.getEmail());
        }

        setupLogoutButton();
//        observeViewModel();

        return root;
    }

    private void setupLogoutButton() {
        binding.buttonLogout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}