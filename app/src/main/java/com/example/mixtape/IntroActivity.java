package com.example.mixtape;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.mixtape.app.BaseActivity;
import com.example.mixtape.login.LoginActivity;
import com.example.mixtape.model.Model;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Adding the gif here using glide library
        ImageView gif = findViewById(R.id.mixtape_gif_iv);
        Glide.with(this).load(R.drawable.mixtape_loader).into(gif);

        Model.instance.executor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Model.instance.isSignedIn()) {
                Model.instance.mainThread.post(() -> {
                    toFeedActivity();
                });
            } else {
                Model.instance.mainThread.post(() -> {
                    toLoginActivity();
                });
            }
        });
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void toFeedActivity() {
        Intent intent = new Intent(this, BaseActivity.class);
        startActivity(intent);
        finish();
    }
}