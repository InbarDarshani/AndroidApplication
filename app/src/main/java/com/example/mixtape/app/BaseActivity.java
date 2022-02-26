package com.example.mixtape.app;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.NavOptions;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.mixtape.IntroActivity;
import com.example.mixtape.MyApplication;
import com.example.mixtape.R;
import com.example.mixtape.login.LoginActivity;
import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.HashSet;
import java.util.Set;

public class BaseActivity extends AppCompatActivity {

    NavController navController;
    AppBarConfiguration appBarConfiguration;
    Toolbar topToolbar;
    BottomNavigationView bottomNav;
    PopupMenu addMenu;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //Get toolbars view
        topToolbar = findViewById(R.id.toolbar_top);
        bottomNav = findViewById(R.id.toolbar_bottom);

        //Setup nav graph
        NavHost navHost = (NavHost) getSupportFragmentManager().findFragmentById(R.id.base_navhost);
        navController = navHost.getNavController();

        //Setup 2 top level destinations of nav graph
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.feedFragment);
        topLevelDestinations.add(R.id.profileFragment);
        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

        //Setup navigation behavior to top toolbar like navigate up and fragment's title
        NavigationUI.setupWithNavController(topToolbar, navController, appBarConfiguration);

        //Get current user id
        currentUserId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("UserId", "");

        //Setup TopToolbar and BottomNavigation
        topBarSetup();
        bottomBarSetup();

        //REMOVEME: Initialize db with code
        //Model.instance.executor.execute(this::initDb);
    }

    private void addPopupMenuSetup() {
        //Initializing the popup menu and giving the reference as current context
        addMenu = new PopupMenu(MyApplication.getContext(), findViewById(R.id.add_icon));

        addMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                bottomNav.setSelectedItemId(R.id.add_icon);
                findViewById(R.id.add_icon).setSelected(true);
                switch (menuItem.getItemId()) {
                    case R.id.new_song:
                        navController.navigate(R.id.action_global_addSongFragment);
                        return true;
                    case R.id.new_mixtape:
                        navController.navigate(R.id.action_global_addMixtapeFragment);
                        return true;
                    default:
                        findViewById(R.id.add_icon).setSelected(false);
                        return false;
                }
            }
        });

        addMenu.setGravity(Gravity.HORIZONTAL_GRAVITY_MASK);
        addMenu.inflate(R.menu.menu_add);
        addMenu.show();
    }

    private void bottomBarSetup() {
        //Setup bottom navigation bar items listeners
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_icon:
                        navController.navigate(R.id.action_global_feedFragment);
                        return true;
                    case R.id.add_icon:
                        return true;
                    case R.id.profile_icon:
                        navController.navigate(R.id.action_global_profileFragment);
                        return true;
                }
                return false;
            }
        });

        //Setup add icon listener separately and override default behavior
        findViewById(R.id.add_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPopupMenuSetup();
            }
        });
    }

    private void topBarSetup() {
        //Setup navigate up on click listener
        topToolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
            fixSelected();
        });

        //Setup menu items on click listener
        topToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case (R.id.logout): {
                    //Set LoginState Observer
                    Model.instance.getUserLoginState().observe(this, loginState -> {
                        if (loginState == Model.LoginState.signedout) {
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    Model.instance.signOut();
                    return true;
                }
            }
            return false;
        });
    }

    //FIXME: back button to exit app not working
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fixSelected();
    }

    //Function to fix bottom navigation bar items states
    private void fixSelected() {
        //Set bottom bar menu selected item in order to apply item state changes
        switch (navController.getCurrentDestination().getId()) {
            case R.id.feedFragment:
                bottomNav.setSelectedItemId(R.id.home_icon);
                break;
            case R.id.profileFragment:
                bottomNav.setSelectedItemId(R.id.profile_icon);
                break;
        }
    }

    private void initDb() {
        Song s1 = new Song("Wandered To LA (with Justin Bieber)", "Juice WRLD", "3151", "");
        Song s2 = new Song("Where Are You Now", "Lost Frequencies", "belgian edm", "");
        Song s3 = new Song("Meet Me At Our Spot", "THE ANXIETY", "", "");
        Song s4 = new Song("Formula", "Labrinth", "indie poptimism", "");
        Song s5 = new Song("Redbone", "Childish Gambino", "atl hip hop", "");
        Song s6 = new Song("Chanel", "Frank Ocean", "alternative r&b", "");
        Song s7 = new Song("IDK You Yet", "Alexander 23", "alt z", "");

        Mixtape m1 = new Mixtape("Ilan", "M1");
        Mixtape m2 = new Mixtape("MyMix1", "M2");
        Mixtape m3 = new Mixtape("Alona's", "M3");
        Mixtape m4 = new Mixtape("MyMix1", "M4");

        String user1 = "wShemi2Ud0PQsm1nJUSeOfgkdSj2";
        String user2 = "bJKFfI49ZrMuXpBZIUpag1ae4q52";

        s1.setUserId(user1);
        s2.setUserId(user1);
        s3.setUserId(user1);
        s4.setUserId(user1);
        m1.setUserId(user1);
        m2.setUserId(user1);

        Model.instance.addMixtape(m1, (mixtapeId) -> {
            Log.d("TAG", "New mixtape added " + mixtapeId);
            s1.setMixtapeId(mixtapeId);
            s2.setMixtapeId(mixtapeId);
            s3.setMixtapeId(mixtapeId);
            Model.instance.addSong(s1, (songId) -> Log.d("TAG", "New song added " + songId));
            Model.instance.addSong(s2, (songId) -> Log.d("TAG", "New song added " + songId));
            Model.instance.addSong(s3, (songId) -> Log.d("TAG", "New song added " + songId));
        });

        Model.instance.addMixtape(m2, (mixtapeId) -> {
            Log.d("TAG", "New mixtape added " + mixtapeId);
            s4.setMixtapeId(mixtapeId);
            Model.instance.addSong(s4, (songId) -> Log.d("TAG", "New song added " + songId));
        });

        s5.setUserId(user2);
        s6.setUserId(user2);
        s7.setUserId(user2);
        m3.setUserId(user2);
        m4.setUserId(user2);

        Model.instance.addMixtape(m3, (mixtapeId) -> {
            Log.d("TAG", "New mixtape added " + mixtapeId);
            s5.setMixtapeId(mixtapeId);
            s6.setMixtapeId(mixtapeId);
            Model.instance.addSong(s5, (songId) -> Log.d("TAG", "New song added " + songId));
            Model.instance.addSong(s6, (songId) -> Log.d("TAG", "New song added " + songId));
        });

        Model.instance.addMixtape(m4, (mixtapeId) -> {
            Log.d("TAG", "New mixtape added " + mixtapeId);
            s7.setMixtapeId(mixtapeId);
            Model.instance.addSong(s7, (songId) -> Log.d("TAG", "New song added " + songId));
        });
    }
}
