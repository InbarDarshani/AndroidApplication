package com.example.mixtape.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import android.os.Bundle;
import com.example.mixtape.R;

public class LoginActivity extends AppCompatActivity {
    NavController navCtl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Setup nav graph
        NavHost navHost = (NavHost) getSupportFragmentManager().findFragmentById(R.id.login_navhost);
        navCtl = navHost.getNavController();
    }
}