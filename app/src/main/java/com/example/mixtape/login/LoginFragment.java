package com.example.mixtape.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mixtape.R;
import com.example.mixtape.app.BaseActivity;
import com.example.mixtape.model.Model;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {
    String emailInput;
    String passwordInput;
    MaterialAlertDialogBuilder alert;
    TextView titleTv;
    Button submitBtn;
    Button secondaryBtn;
    Button backBtn;
    boolean newUser = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //Get views
        submitBtn = view.findViewById(R.id.login_submit_btn);
        secondaryBtn = view.findViewById(R.id.login_createaccount_btn);
        backBtn = view.findViewById(R.id.login_back_btn);
        titleTv = view.findViewById(R.id.title_tv);

        //Setup alert dialog
        alert = new MaterialAlertDialogBuilder(this.getContext());
        alert.setTitle("Login Error");

        //Set LoginState Observer
        Model.instance.getUserLoginState().observeForever(new Observer<Model.LoginState>() {
            @Override
            public void onChanged(Model.LoginState loginState) {
                if (loginState == Model.LoginState.error)
                    alert.setMessage(Model.instance.dbError).show();
                if (loginState == Model.LoginState.completed)
                    toFeedActivity();
            }
        });

        //CreateAccount button setup - edit view to sign-up screen
        secondaryBtn.setOnClickListener(v -> {
            newUser = true;
            titleTv.setText("Create a Mixtaper Account");
            secondaryBtn.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
        });

        //Back button setup - edit view back to sign-in screen
        backBtn.setOnClickListener(v -> {
            newUser = false;
            titleTv.setText("Welcome To Mixtape App!");
            secondaryBtn.setVisibility(View.VISIBLE);
            backBtn.setVisibility(View.GONE);
        });

        //Login button setup
        submitBtn.setOnClickListener(v -> {
            emailInput = ((EditText) view.findViewById(R.id.login_email_et)).getText().toString();
            passwordInput = ((EditText) view.findViewById(R.id.login_password_et)).getText().toString();

            if (validateInput())
                //Perform sign-in or sign-up
                Model.instance.signInsignUp(emailInput, passwordInput, newUser);
        });

        return view;
    }

    private void toFeedActivity() {
        Intent intent = new Intent(getContext(), BaseActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private boolean validateInput() {
        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            alert.setMessage("Please fill both email and password").show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            alert.setMessage("Please fill proper email address").show();
            return false;
        }

        if (newUser) {
            Pattern requirements = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9]).{6,}$");
            Matcher matcher = requirements.matcher(passwordInput);
            if (!matcher.matches()) {
                alert.setMessage("Password must be at least 6 characters\nPassword must contain at east one digit\nPassword must contain at least one character").show();
                return false;
            }
        }

        return true;
    }
}