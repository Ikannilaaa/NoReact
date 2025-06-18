package com.example.noreact.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.noreact.MainActivity;
import com.example.noreact.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    private TextView textUserEmail;
    private Button buttonLogout;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        mAuth = FirebaseAuth.getInstance();

        textUserEmail = root.findViewById(R.id.text_user_email);
        buttonLogout = root.findViewById(R.id.button_logout);

        // Display current user's email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            textUserEmail.setText(currentUser.getEmail());
        }

        buttonLogout.setOnClickListener(v -> logout());

        return root;
    }

    private void logout() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).logout();
        }
    }
}