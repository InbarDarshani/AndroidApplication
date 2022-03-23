package com.example.mixtape.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mixtape.MyApplication;
import com.example.mixtape.R;
import com.example.mixtape.model.Mixtape;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.viewmodels.EditMixtapeViewModel;
import com.example.mixtape.viewmodels.EditMixtapeViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.LinkedList;
import java.util.List;

public class EditMixtapeFragment extends Fragment {
    EditMixtapeViewModel viewModel;
    MaterialAlertDialogBuilder alert;
    EditText mixtape_name_et, mixtape_description_et;
    TextView mixtape_add_edit_songs_empty_tv;
    ListAdapter adapter;
    RecyclerView songsList;
    Button mixtape_submit_btn;
    ProgressBar progressBar;
    String inputName, inputDescription, currentUserId;
    List<Song> inputChosenSongs = new LinkedList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String mixtapeId = EditMixtapeFragmentArgs.fromBundle(getArguments()).getMixtapeId();
        viewModel = new ViewModelProvider(this, new EditMixtapeViewModelFactory(mixtapeId)).get(EditMixtapeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_edit_mixtape, container, false);
        //Edit page view
        ((TextView) view.findViewById(R.id.mixtape_add_edit_page_title_tv)).setText("Edit Mixtape");
        ((Button) view.findViewById(R.id.mixtape_submit_btn)).setText("Save");

        //Get views
        mixtape_name_et = view.findViewById(R.id.mixtape_name_et);
        mixtape_description_et = view.findViewById(R.id.mixtape_description_et);
        mixtape_submit_btn = view.findViewById(R.id.mixtape_submit_btn);
        progressBar = view.findViewById(R.id.mixtape_progressbar);
        songsList = view.findViewById(R.id.mixtape_add_edit_songs_rv);
        mixtape_add_edit_songs_empty_tv = view.findViewById(R.id.mixtape_add_edit_songs_empty_tv);

        //Setup alert dialog
        alert = new MaterialAlertDialogBuilder(this.getContext());
        alert.setTitle("Input Error");

        //Observe user's mixtapes live data
        viewModel.getMixtapeItems().observe(getViewLifecycleOwner(), mixtapeItems -> {
            //Bind mixtape's data
            mixtape_name_et.setText(viewModel.getMixtape().getName());
            mixtape_description_et.setText(viewModel.getMixtape().getDescription());

            //Setup submit button
            mixtape_submit_btn.setEnabled(true);
            mixtape_submit_btn.setOnClickListener(v -> validateAndSave());
        });

        //Observe user's songs live data
        viewModel.getSongItems().observe(getViewLifecycleOwner(), songItems -> {
            //Initial selected songs input
            inputChosenSongs = viewModel.getMixtapeSongs();

            //Treat an empty list
            if (songItems.isEmpty())
                mixtape_add_edit_songs_empty_tv.setVisibility(View.VISIBLE);

            //Setup mixtape's song list with multiple selection option
            songsList.setHasFixedSize(true);
            songsList.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
            adapter = new ListAdapter();
            songsList.setAdapter(adapter);

            adapter.setOnItemClickListener((v, position, isChecked) -> {
                Song clicked = viewModel.getUserSongs().get(position);
                if (isChecked)
                    inputChosenSongs.add(clicked);
                else
                    inputChosenSongs.remove(clicked);
            });
        });

        return view;
    }

    private void validateAndSave() {
        inputName = mixtape_name_et.getText().toString();
        inputDescription = mixtape_description_et.getText().toString();
        currentUserId = viewModel.getCurrentUser().getUserId();

        //Validate Input
        if (inputName.isEmpty())
            alert.setMessage("Please enter mixtapes's title").show();
        else if (viewModel.existingMixtapeName(inputName)) {
            alert.setMessage("a Mixtape with this title already exists").show();
            mixtape_name_et.setText(viewModel.getMixtape().getName());
        } else
            saveToDb();
    }

    private void saveToDb() {
        progressBar.setVisibility(View.VISIBLE);
        mixtape_submit_btn.setEnabled(false);
        adapter.setEnabled(false);

        Mixtape mixtape = viewModel.getMixtape();
        mixtape.setName(inputName);
        mixtape.setDescription(inputDescription);

        Model.instance.updateMixtape(mixtape, dbMixtape -> {
            if (inputChosenSongs.isEmpty() || inputChosenSongs.equals(viewModel.getMixtapeSongs()))
                back();
            else {
                inputChosenSongs.forEach(s -> s.setMixtapeId(mixtape.getMixtapeId()));
                Model.instance.updateSongs(inputChosenSongs, lastSong -> back());
            }
        });
    }

    private void back() {
        Navigation.findNavController(mixtape_name_et).navigateUp();
    }


    //______________________ List Listeners Interface ______________________________________
    //Interface wrapper for a list item listeners
    interface OnItemClickListener {
        void onItemClick(View v, int position, boolean isChecked);
    }

    //______________________ Recycler View Holder Class _____________________________
    //Holds song's row view items and links them to the view resources
    class RowHolder extends RecyclerView.ViewHolder {
        TextView mixtaperow_song_tv;
        CheckBox mixtaperow_song_checkable_cb;
        boolean isChecked = false;

        public RowHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            //Set row view resources
            mixtaperow_song_tv = itemView.findViewById(R.id.mixtaperow_song_checkable_tv);
            mixtaperow_song_checkable_cb = itemView.findViewById(R.id.mixtaperow_song_checkable_cb);

            //Sets row listeners
            itemView.setOnClickListener(v -> {
                isChecked = !isChecked;
                mixtaperow_song_checkable_cb.setChecked(isChecked);
                listener.onItemClick(v, getAdapterPosition(), isChecked);
            });
        }

        //Bind Song data of this mixtape
        @SuppressLint("SetTextI18n")
        void bind(Song song) {
            //Treat an existing song of this mixtape
            if (viewModel.getMixtapeSongs().contains(song)) {
                //TODO?: if song removed add to default mixtape instead
                //Set it unclickable so it wont remain without containing mixtape
                this.itemView.setClickable(false);
                mixtaperow_song_checkable_cb.setEnabled(false);
                //Set state and checkbox view
                this.isChecked = true;
                mixtaperow_song_checkable_cb.setChecked(true);
            }
            //Bind rest of the data
            String name = song.getName();
            String artist = song.getArtist();
            if (artist.isEmpty())
                mixtaperow_song_tv.setText(name);
            else
                mixtaperow_song_tv.setText(artist + " - " + name);
        }
    }

    //______________________ Recycler View Adapter Class _____________________________
    //List adapter holding also the row listeners
    class ListAdapter extends RecyclerView.Adapter<RowHolder> {

        OnItemClickListener listener;

        //Row listeners setters
        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        //Option to cancel on click listener
        public void setEnabled(boolean enabled) {
            if (!enabled)
                this.listener = null;
        }

        @NonNull
        @Override
        //Settings for when creating the row view holder
        //Creates the row view from the row view resource and creates a view holder for it
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_mixtape_song_row_checkable, parent, false);
            return new RowHolder(view, listener);
        }

        @Override
        //Settings for when binding the view items and view resources
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            Song song = viewModel.getUserSongs().get(position);
            holder.bind(song);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getUserSongs() == null)
                return 0;
            return viewModel.getUserSongs().size();
        }
    }
}