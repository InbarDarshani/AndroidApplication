package com.example.mixtape.app;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mixtape.MyApplication;
import com.example.mixtape.R;

import com.example.mixtape.model.Song;
import com.example.mixtape.viewmodels.MixtapeDetailsViewModel;
import com.example.mixtape.viewmodels.MixtapeDetailsViewModelFactory;
import com.squareup.picasso.Picasso;

public class MixtapeDetailsFragment extends Fragment {
    MixtapeDetailsViewModel viewModel;
    ImageView mixtape_details_user_iv;
    TextView mixtape_details_user_tv, mixtape_name_tv, mixtape_description_tv;
    RecyclerView songsList;
    ListAdapter adapter;

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

        //Setup mixtape's song list
        songsList.setHasFixedSize(true);
        songsList.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new ListAdapter();
        songsList.setAdapter(adapter);

        //Create the row items listeners actions
        adapter.setOnItemClickListener((v, position) -> {
            //get song item from view model
            String songId = viewModel.getSongs().get(position).getSongId();
            //navigate to song details page
            Navigation.findNavController(v).navigate(MixtapeDetailsFragmentDirections.actionMixtapeDetailsFragmentToSongDetailsFragment(songId));
        });

        //Bind Mixtape data
        mixtape_name_tv.setText(viewModel.getMixtape().getName());
        mixtape_description_tv.setText(viewModel.getMixtape().getDescription());

        //Bind User data of this mixtape
        if (!viewModel.getUser().getImage().isEmpty())
            Picasso.get().load(viewModel.getUser().getImage()).into(mixtape_details_user_iv);
        mixtape_details_user_tv.setText(viewModel.getUser().getDisplayName());

        //Set on click navigation
        mixtape_details_user_iv.setOnClickListener(v -> Navigation.findNavController(v).navigate(FeedFragmentDirections.actionGlobalProfileFragment(viewModel.getUser().getUserId())));

        return view;
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
