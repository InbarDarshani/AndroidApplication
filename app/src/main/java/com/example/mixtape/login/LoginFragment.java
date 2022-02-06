package com.example.mixtape.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mixtape.R;
import com.example.mixtape.app.BaseActivity;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.User;


public class LoginFragment extends Fragment {
    View view;
    String emailInput;
    String passwordInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);

        //Login button setup
        Button loginBtn = view.findViewById(R.id.login_login_btn);
        loginBtn.setOnClickListener(v -> {
            emailInput = ((EditText) view.findViewById(R.id.login_email_et)).getText().toString();
            passwordInput = ((EditText) view.findViewById(R.id.login_password_et)).getText().toString();

            if (validateInput() && authenticate()) {
                toFeedActivity();
            }
        });
        return view;
    }

    private void toFeedActivity() {
        Intent intent = new Intent(getContext(), BaseActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private boolean validateInput() {
        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            Toast.makeText(this.getContext(), "Please fill both email and password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            Toast.makeText(this.getContext(), "Please fill proper email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean authenticate() {
        //Perform sign in
        Model.instance.signIn(emailInput, passwordInput);
        //"Wait" for result
        LiveData<User> u = Model.instance.getUser();
        if (u == null){
            Toast.makeText(this.getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}