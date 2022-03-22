package com.example.mixtape;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.mixtape.app.BaseActivity;
import com.example.mixtape.login.LoginActivity;
import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;

import java.util.Map;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Model.instance.executor.execute(() -> {
            //initDb(); //TOREMOVE: Initialize db with code
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Model.instance.isSignedIn())
                Model.instance.mainThread.post(this::toFeedActivity);
            else
                Model.instance.mainThread.post(this::toLoginActivity);
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

    private void initDb() {
        String alona = "FS7zbeuGIZZlG0nbFwMsexOTdDs1";
        String ilan = "azWzEbuM5mVWW5PL0GXrLrqSQBl1";

        Song s1 = new Song("8702", "tobi lou", "", alona);
        Song s2 = new Song("A-O-K", "Tai Verdes", "", alona);
        Song s3 = new Song("Acapulco", "Jason Derulo", "", alona);
        Song s4 = new Song("Alone", "OM1D", "", alona);
        Song s5 = new Song("Alone", "Eddy Rock", "", alona);
        Song s6 = new Song("B.Q.E", "Kota the Friend", "", alona);
        Song s7 = new Song("Bitter", "Kota the Friend", "", alona);
        Song s8 = new Song("Bonnie and Clyde", "K1ngsam", "", alona);
        Song s9 = new Song("Breathe", "Kota the Friend", "", alona);
        Song s10 = new Song("Buff Baby", "tobi lou", "", alona);

        Song s11 = new Song("CAN YOU HEAR THE MOON", "Grady", "", alona);
        Song s12 = new Song("CRUSH", "SEBASTIAN PAUL", "", alona);
        Song s13 = new Song("California (feat. Warren Hue)", "88rising", "", alona);
        Song s14 = new Song("Casanova", "Blake Rose", "", alona);
        Song s15 = new Song("Celestial", "Dawin", "", alona);

        Song s16 = new Song("Chanel", "Frank Ocean", "", ilan);
        Song s17 = new Song("Cheap Vacations (feat. Facer)", "tobi lou", "", ilan);
        Song s18 = new Song("Cherry Beach", "Kota the Friend", "", ilan);
        Song s19 = new Song("Chicken Tenders", "Dominic Fike", "", ilan);
        Song s20 = new Song("Cigarettes On Patios", "BabyJake", "", ilan);
        Song s21 = new Song("Colorado", "Kota the Friend", "", ilan);
        Song s22 = new Song("Colorado", "Milky Chance", "", ilan);
        Song s23 = new Song("DRUGS", "Tai Verdes", "", ilan);
        Song s24 = new Song("Darlin'", "tobi lou", "", ilan);
        Song s25 = new Song("Day By Day", "Frank Walker", "", ilan);

        Song s26 = new Song("Dear Fear", "Kota the Friend", "", ilan);
        Song s27 = new Song("Designer Boi", "A$AP NAST", "", ilan);
        Song s28 = new Song("E", "Jean Carter", "", ilan);
        Song s29 = new Song("Endorphins", "tobi lou", "", ilan);
        Song s30 = new Song("Face It", "Kota the Friend", "", ilan);

        Mixtape m1 = new Mixtape("Alona's Mix", "M1", alona);
        Mixtape m2 = new Mixtape("MyMix", "M2", alona);
        Mixtape m3 = new Mixtape("Ilan's Mix", "M3", ilan);
        Mixtape m4 = new Mixtape("MyMixtape", "M4", ilan);


        Model.instance.addMixtape(m1, mixtape -> {
            Log.d("TAG", "New mixtape added " + mixtape.getMixtapeId());
            s1.setMixtapeId(mixtape.getMixtapeId());
            s2.setMixtapeId(mixtape.getMixtapeId());
            s3.setMixtapeId(mixtape.getMixtapeId());
            s4.setMixtapeId(mixtape.getMixtapeId());
            s5.setMixtapeId(mixtape.getMixtapeId());
            s6.setMixtapeId(mixtape.getMixtapeId());
            s7.setMixtapeId(mixtape.getMixtapeId());
            s8.setMixtapeId(mixtape.getMixtapeId());
            s9.setMixtapeId(mixtape.getMixtapeId());
            s10.setMixtapeId(mixtape.getMixtapeId());

            Model.instance.addSong(s1, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s2, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s3, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s4, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s5, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s6, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s7, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s8, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s9, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s10, song -> Log.d("TAG", "New song added " + song.getSongId()));
        });

        Model.instance.addMixtape(m2, mixtape -> {
            Log.d("TAG", "New mixtape added " + mixtape.getMixtapeId());
            s11.setMixtapeId(mixtape.getMixtapeId());
            s12.setMixtapeId(mixtape.getMixtapeId());
            s13.setMixtapeId(mixtape.getMixtapeId());
            s14.setMixtapeId(mixtape.getMixtapeId());
            s15.setMixtapeId(mixtape.getMixtapeId());

            Model.instance.addSong(s11, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s12, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s13, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s14, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s15, song -> Log.d("TAG", "New song added " + song.getSongId()));
        });


        Model.instance.addMixtape(m3, mixtape -> {
            Log.d("TAG", "New mixtape added " + mixtape.getMixtapeId());
            s16.setMixtapeId(mixtape.getMixtapeId());
            s17.setMixtapeId(mixtape.getMixtapeId());
            s18.setMixtapeId(mixtape.getMixtapeId());
            s19.setMixtapeId(mixtape.getMixtapeId());
            s20.setMixtapeId(mixtape.getMixtapeId());
            s21.setMixtapeId(mixtape.getMixtapeId());
            s22.setMixtapeId(mixtape.getMixtapeId());
            s23.setMixtapeId(mixtape.getMixtapeId());
            s24.setMixtapeId(mixtape.getMixtapeId());
            s25.setMixtapeId(mixtape.getMixtapeId());

            Model.instance.addSong(s16, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s17, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s18, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s19, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s20, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s21, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s22, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s23, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s24, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s25, song -> Log.d("TAG", "New song added " + song.getSongId()));
        });

        Model.instance.addMixtape(m4, mixtape -> {
            Log.d("TAG", "New mixtape added " + mixtape.getMixtapeId());
            s26.setMixtapeId(mixtape.getMixtapeId());
            s27.setMixtapeId(mixtape.getMixtapeId());
            s28.setMixtapeId(mixtape.getMixtapeId());
            s29.setMixtapeId(mixtape.getMixtapeId());
            s30.setMixtapeId(mixtape.getMixtapeId());

            Model.instance.addSong(s26, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s27, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s28, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s29, song -> Log.d("TAG", "New song added " + song.getSongId()));
            Model.instance.addSong(s30, song -> Log.d("TAG", "New song added " + song.getSongId()));
        });
    }
}