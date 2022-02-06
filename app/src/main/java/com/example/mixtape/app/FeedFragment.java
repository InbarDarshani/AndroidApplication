package com.example.mixtape.app;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mixtape.R;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.Song;
import com.example.mixtape.viewmodels.FeedViewModel;

public class FeedFragment extends Fragment {
    FeedViewModel viewModel;
    RecyclerView list;
    ListAdapter adapter;
    SwipeRefreshLayout swipeRefresh;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        //Get refresh view and bind data refresh to its listener
        swipeRefresh = view.findViewById(R.id.feed_swiperefresh);
        swipeRefresh.setOnRefreshListener(() -> Model.instance.refreshFeed());

        //Set list and adapter
        list = view.findViewById(R.id.feed_rv);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListAdapter();
        list.setAdapter(adapter);

        //Create the row items listeners actions
        adapter.setOnItemClickListener((v, position) -> {
            //get song item from view model
            String sId = viewModel.getSongs().getValue().get(position).getSongId();
            //navigate to sod details page
            Navigation.findNavController(v).navigate(FeedFragmentDirections.actionFeedFragmentToSongDetailsFragment());
        });

        return view;
    }

    //______________________Listeners Interface Wrapper______________________________________
    //Interface wrapper for a list item listeners
    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //______________________Recycler View Holder Implementation_____________________________
    //Holds student's row view items and links them to the view resources
    class RowHolder extends RecyclerView.ViewHolder {
        ImageView feedrow_profile_iv;
        TextView feedrow_user_tv;
        ImageView feedrow_photo_iv;
        TextView feedrow_song_tv;
        TextView feedrow_artist_tv;
        TextView feedrow_mixtape_tv;
        TextView feedrow_caption_tv;

        public RowHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            //Set row view resources
            feedrow_profile_iv = itemView.findViewById(R.id.feedrow_profile_iv);
            feedrow_user_tv = itemView.findViewById(R.id.feedrow_user_tv);
            feedrow_photo_iv = itemView.findViewById(R.id.feedrow_photo_iv);
            feedrow_artist_tv = itemView.findViewById(R.id.feedrow_artist_tv);
            feedrow_song_tv = itemView.findViewById(R.id.feedrow_song_tv);
            feedrow_mixtape_tv = itemView.findViewById(R.id.feedrow_mixtape_tv);
            feedrow_caption_tv = itemView.findViewById(R.id.feedrow_caption_tv);

            //Sets row listeners
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos);
            });
        }
    }

    //______________________Recycler View Adapter Implementation_____________________________
    //Student list adapter holding also the row listeners
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
            View view = getLayoutInflater().inflate(R.layout.list_feed_row, parent, false);
            return new RowHolder(view, listener);
        }

        @Override
        //Settings for when binding the view items and view resources
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            Song song = viewModel.getSongs().getValue().get(position);
            //TODO: Set images
            //holder.feedrow_profile_iv.setImageResource(song.getUser().getImage);
            //holder.feedrow_photo_iv.setImageResource(song.getImage());
            holder.feedrow_song_tv.setText(song.getName());
            holder.feedrow_artist_tv.setText(song.getArtist());
            holder.feedrow_caption_tv.setText(song.getCaption());
            //TODO: Set relations
            //holder.feedrow_user_tv.setText(song.getUser().getFullName());
            //holder.feedrow_mixtape_tv.setText(Model.instance.getMixtapeOfSong(song).getName());
        }

        @Override
        public int getItemCount() {
            if(viewModel.getSongs().getValue() == null){
                return 0;
            }
            return viewModel.getSongs().getValue().size();
        }
    }
}