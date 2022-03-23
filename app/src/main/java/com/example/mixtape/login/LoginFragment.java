package com.example.mixtape.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mixtape.MyApplication;
import com.example.mixtape.R;
import com.example.mixtape.app.BaseActivity;
import com.example.mixtape.model.Model;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {
    MaterialAlertDialogBuilder alert;
    String firstnameInput, lastnameInput, emailInput, passwordInput;
    TextView login_title_tv;
    LinearLayout login_fullname_l;
    Button login_submit_btn, login_createaccount_btn, login_back_btn;
    boolean newUser = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //Get views
        login_submit_btn = view.findViewById(R.id.login_submit_btn);
        login_createaccount_btn = view.findViewById(R.id.login_createaccount_btn);
        login_back_btn = view.findViewById(R.id.login_back_btn);
        login_title_tv = view.findViewById(R.id.login_title_tv);
        login_fullname_l = view.findViewById(R.id.login_fullname_l);

        //Setup alert dialog
        alert = new MaterialAlertDialogBuilder(this.getContext());
        alert.setTitle("Login Error");

        //Set LoginState Observer within this login activity lifecycle
        Model.instance.getUserLoginState().observe(getViewLifecycleOwner(), loginState -> {
            if (loginState == Model.LoginState.error)
                alert.setMessage(Model.instance.authError).show();
            if (loginState == Model.LoginState.signedin)
                toFeedActivity();
        });

        //CreateAccount button setup - edit view to sign-up screen
        login_createaccount_btn.setOnClickListener(v -> {
            newUser = true;
            login_title_tv.setText("Create a Mixtaper Account");
            login_createaccount_btn.setVisibility(View.GONE);
            login_back_btn.setVisibility(View.VISIBLE);
            login_fullname_l.setVisibility(View.VISIBLE);
        });

        //Back button setup - edit view back to sign-in screen
        login_back_btn.setOnClickListener(v -> {
            newUser = false;
            login_title_tv.setText("Login With Your Mixtaper Account");
            login_createaccount_btn.setVisibility(View.VISIBLE);
            login_back_btn.setVisibility(View.GONE);
            login_fullname_l.setVisibility(View.GONE);
        });

        //Login button setup
        login_submit_btn.setOnClickListener(v -> {
            emailInput = ((EditText) view.findViewById(R.id.login_email_et)).getText().toString();
            passwordInput = ((EditText) view.findViewById(R.id.login_password_et)).getText().toString();
            firstnameInput = ((EditText) view.findViewById(R.id.login_firstname_et)).getText().toString();
            lastnameInput = ((EditText) view.findViewById(R.id.login_lastname_et)).getText().toString();

            if (validateInput())
                //Perform sign-in or sign-up
                Model.instance.signInsignUp(firstnameInput + " " + lastnameInput, emailInput, passwordInput, newUser);
        });

        return view;
    }

    private void toFeedActivity() {
        Intent intent = new Intent(MyApplication.getContext(), BaseActivity.class);
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
            if (firstnameInput.isEmpty() || lastnameInput.isEmpty()) {
                alert.setMessage("Please fill first and last name").show();
                return false;
            }

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