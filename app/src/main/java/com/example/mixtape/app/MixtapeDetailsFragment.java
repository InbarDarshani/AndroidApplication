package com.example.mixtape.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mixtape.MyApplication;
import com.example.mixtape.NavGraphDirections;
import com.example.mixtape.R;

import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.viewmodels.MixtapeDetailsViewModel;
import com.example.mixtape.viewmodels.MixtapeDetailsViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

public class MixtapeDetailsFragment extends Fragment {
    MixtapeDetailsViewModel viewModel;
    ImageView mixtape_details_user_iv;
    TextView mixtape_details_user_tv, mixtape_name_tv, mixtape_description_tv, songs_empty_tv;
    RecyclerView songsList;
    ListAdapter adapter;
    ImageButton mixtap_edit, mixtape_delete;
    ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String mixtapeId = MixtapeDetailsFragmentArgs.fromBundle(getArguments()).getMixtapeId();
        viewModel = new ViewModelProvider(this, new MixtapeDetailsViewModelFactory(mixtapeId)).get(MixtapeDetailsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mixtape_details, container, false);

        //Get views
        mixtape_details_user_iv = view.findViewById(R.id.mixtape_details_user_iv);
        mixtape_details_user_tv = view.findViewById(R.id.mixtape_details_user_tv);
        mixtape_name_tv = view.findViewById(R.id.mixtape_name_tv);
        mixtape_description_tv = view.findViewById(R.id.mixtape_description_tv);
        songsList = view.findViewById(R.id.mixtape_songs_rv);
        mixtap_edit = view.findViewById(R.id.mixtap_edit);
        mixtape_delete = view.findViewById(R.id.mixtape_delete);
        progressBar = view.findViewById(R.id.songs_progressbar);
        songs_empty_tv = view.findViewById(R.id.songs_empty_tv);

        //Setup mixtape's song list
        songsList.setHasFixedSize(true);
        songsList.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new ListAdapter();
        songsList.setAdapter(adapter);

        //Set on click navigation to user's profile
        mixtape_details_user_iv.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        MixtapeDetailsFragmentDirections.actionMixtapeDetailsFragmentToUserFragment(viewModel.getUser().getUserId())));

        //Observe view model's main data
        viewModel.getMixtapeItem().observe(getViewLifecycleOwner(), mixtapeItem -> {
            bind();
            setup();
        });

        //Observe view model's songs state
        viewModel.songsLoadingState.observe(getViewLifecycleOwner(), loadingState -> {
            if (loadingState == MixtapeDetailsViewModel.SongsState.loading)
                progressBar.setVisibility(View.VISIBLE);

            if (loadingState == MixtapeDetailsViewModel.SongsState.loaded){
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                songs_empty_tv.setVisibility(View.GONE);
            }

            if (loadingState == MixtapeDetailsViewModel.SongsState.empty){
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                songs_empty_tv.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void bind() {
        //Bind Mixtape data
        mixtape_name_tv.setText(viewModel.getMixtape().getName());
        mixtape_description_tv.setText(viewModel.getMixtape().getDescription());

        //Bind User data of this mixtape
        if (!viewModel.getUser().getImage().isEmpty())
            Picasso.get().load(viewModel.getUser().getImage()).into(mixtape_details_user_iv);
        mixtape_details_user_tv.setText(viewModel.getUser().getDisplayName());
    }

    private void setup() {
        //Create the row items listener actions
        adapter.setOnItemClickListener((v, position) -> {
            //get song item from view model
            String songId = viewModel.getSongs().get(position).getSongId();
            //navigate to song details page
            Navigation.findNavController(v).navigate(NavGraphDirections.actionGlobalSongDetailsFragment(songId));
        });

        //Setup buttons for current user
        String currentUserId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("userId", "");
        if (viewModel.getUser().getUserId().equals(currentUserId)) {
            mixtap_edit.setVisibility(View.VISIBLE);
            mixtape_delete.setVisibility(View.VISIBLE);

            mixtap_edit.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(MixtapeDetailsFragmentDirections.actionMixtapeDetailsFragmentToEditMixtapeFragment(viewModel.getMixtape().getMixtapeId()));
            });

            mixtape_delete.setOnClickListener(v -> {
                MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this.getContext());
                alert.setTitle("Cant Delete Mixtape");

                if (adapter.getItemCount() != 0)
                    alert.setMessage("You cant delete a mixtape with songs").show();
                else
                    Model.instance.deleteMixtape(viewModel.getMixtape(), () -> {
                        Navigation.findNavController(v).navigateUp();
                    });
            });
        }
    }

    //______________________ List Listeners Interface ______________________________________
    //Interface wrapper for a list item listeners
    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //______________________ Recycler View Holder Class _____________________________
    //Holds song's row view items and links them to the view resources
    class RowHolder extends RecyclerView.ViewHolder {
        TextView mixtaperow_song_tv;

        public RowHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            //Set row view resources
            mixtaperow_song_tv = itemView.findViewById(R.id.mixtaperow_song_tv);

            //Sets row listeners
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos);
            });
        }

        void bind(Song song) {
            //Bind Song data of this mixtape
            mixtaperow_song_tv.setText(song.getName());
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

        @NonNull
        @Override
        //Settings for when creating the row view holder
        //Creates the row view from the row view resource and creates a view holder for it
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_mixtape_song_row, parent, false);
            return new RowHolder(view, listener);
        }

        @Override
        //Settings for when binding the view items and view resources
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            Song song = viewModel.getSongs().get(position);
            holder.bind(song);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getSongs() == null)
                return 0;
            return viewModel.getSongs().size();
        }
    }
}
