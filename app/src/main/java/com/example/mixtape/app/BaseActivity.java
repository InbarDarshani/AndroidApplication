package com.example.mixtape.app;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.mixtape.MyApplication;
import com.example.mixtape.NavGraphDirections;
import com.example.mixtape.R;
import com.example.mixtape.login.LoginActivity;
import com.example.mixtape.model.Model;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

public class BaseActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    NavHost navHost;
    NavController navController;
    Toolbar topToolbar;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //Get toolbars view
        topToolbar = findViewById(R.id.toolbar_top);
        bottomNav = findViewById(R.id.toolbar_bottom);

        //Setup nav graph
        fragmentManager = getSupportFragmentManager();
        navHost = (NavHost) fragmentManager.findFragmentById(R.id.base_navhost);
        navController = navHost.getNavController();

        //Setup top level destinations of nav graph
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.feedFragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

        //Setup navigation behavior
        NavigationUI.setupWithNavController(topToolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        //Set hardware back button behavior
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //Make sure the current destination fragment is valid
                if (navController.getCurrentBackStackEntry() == null)
                    return;
                int currentDest = navController.getCurrentBackStackEntry().getDestination().getId();

                //Perform navigation up behavior
                //If the current fragment is is not one of the top level destinations
                if (!topLevelDestinations.contains(currentDest)) {
                    navigateUp();
                }
            }
        });

        //Setup TopToolbar and BottomNavigation
        topBarSetup();
        bottomNavBarSetup();
    }

    @SuppressLint("NonConstantResourceId")
    private void bottomNavBarSetup() {
        //Get current User
        String currentUserId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("userId", "");

        //Setup bottom navigation bar items listeners
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.feedFragment:
                    navController.navigate(NavGraphDirections.actionGlobalFeedFragment());
                    return true;
                case R.id.addSongFragment:
                    navController.navigate(NavGraphDirections.actionGlobalAddSongFragment());
                    return true;
                case R.id.profileFragment:
                    navController.navigate(NavGraphDirections.actionGlobalProfileFragment(currentUserId));
                    return true;
            }
            return false;
        });
    }

    private void topBarSetup() {
        //Setup navigate up on click listener
        topToolbar.setNavigationOnClickListener(v -> {
            navigateUp();
        });

        //Set LoginState Observer within this base activity lifecycle ("forever" in app)
        Model.instance.getUserLoginState().observe(this, loginState -> {
            if (loginState == Model.LoginState.signedout) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Setup menu items on click listener
        topToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.logout) {  //Perform sign out
                Model.instance.signOut();
                return true;
            }
            return false;
        });
    }

    private void navigateUp() {
        navController.popBackStack();
    }

}


