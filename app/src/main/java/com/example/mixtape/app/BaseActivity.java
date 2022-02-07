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

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.mixtape.R;
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

        //Setup TopToolbar and BottomNavigation
        topBarSetup();
        bottomBarSetup();
    }

    private void addPopupMenuSetup() {
        //Initializing the popup menu and giving the reference as current context
        addMenu = new PopupMenu(BaseActivity.this.getApplicationContext(), bottomNav.findViewById(R.id.add_icon));

        //Inflating popup menu from xml file
        addMenu.getMenuInflater().inflate(R.menu.menu_add, addMenu.getMenu());
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
        //Showing the popup menu
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
        topToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: figure whats better
                navController.navigateUp();
                //navController.popBackStack();
                //onBackPressed();
                fixSelected();
            }
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
}
