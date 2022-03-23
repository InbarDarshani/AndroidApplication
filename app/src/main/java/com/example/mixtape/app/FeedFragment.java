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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mixtape.MyApplication;
import com.example.mixtape.R;
import com.example.mixtape.model.Model;
import com.example.mixtape.model.SongItem;
import com.example.mixtape.viewmodels.FeedViewModel;
import com.squareup.picasso.Picasso;

public class FeedFragment extends Fragment {
    FeedViewModel viewModel;
    RecyclerView list;
    ListAdapter adapter;
    SwipeRefreshLayout swipeRefresh;
    TextView feed_empty_tv;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        //Get views
        feed_empty_tv = view.findViewById(R.id.feed_empty_tv);

        //Setup refresh view, attach OnRefresh function, set refreshing state according to loading state
        swipeRefresh = view.findViewById(R.id.feed_swiperefresh);
        swipeRefresh.setOnRefreshListener(Model.instance::refreshFeed);
        swipeRefresh.setRefreshing(Model.instance.getFeedLoadingState().getValue() == Model.FeedState.loading);

        //Set list and adapter
        list = view.findViewById(R.id.feed_rv);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new ListAdapter();
        list.setAdapter(adapter);

        //Create the row items on click listener
        adapter.setOnItemClickListener((v, position) -> {
            //get song item from view model
            String songId = viewModel.getSongItems().getValue().get(position).song.getSongId();
            //navigate to song details page
            Navigation.findNavController(v).navigate(FeedFragmentDirections.actionGlobalSongDetailsFragment(songId));
        });

        //Setup observer for ViewModel's livedata, set OnChange action
        viewModel.getSongItems().observe(getViewLifecycleOwner(), songItems -> adapter.notifyDataSetChanged());

        //Setup observer for Model's feed loading state
        Model.instance.getFeedLoadingState().observe(getViewLifecycleOwner(), feedLoadingState -> {
            adapter.notifyDataSetChanged();

            //Change SwipeRefreshLayout according to loading state
            swipeRefresh.setRefreshing(feedLoadingState == Model.FeedState.loading);

            //Treat an empty list state
            if (feedLoadingState == Model.FeedState.empty) {
                list.setVisibility(View.GONE);
                feed_empty_tv.setVisibility(View.VISIBLE);
            } else {
                list.setVisibility(View.VISIBLE);
                feed_empty_tv.setVisibility(View.GONE);
            }
        });

        return view;
    }

    //______________________ Recycler View Adapter Setup _____________________________
    //List Listeners Interface
    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //Recycler View Holder Class
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
                listener.onItemClick(v, getAdapterPosition());
            });
        }

        void bind(SongItem songItem) {
            //Bind Mixtape data of this song post
            feedrow_mixtape_tv.setText(songItem.mixtape.getName());

            //Bind User data of this song post
            feedrow_profile_iv.setImageResource(R.drawable.empty_user_image_colored);
            if (!songItem.user.getImage().isEmpty())
                Picasso.get().load(songItem.user.getImage()).into(feedrow_profile_iv);
            feedrow_user_tv.setText(songItem.user.getDisplayName());

            //Bind song's data
            feedrow_photo_iv.setImageResource(R.drawable.empty_song_image);
            if (!songItem.song.getImage().isEmpty())
                Picasso.get().load(songItem.song.getImage()).into(feedrow_photo_iv);
            feedrow_song_tv.setText(songItem.song.getName());
            feedrow_artist_tv.setText(songItem.song.getArtist());
            feedrow_caption_tv.setText(songItem.song.getCaption());
        }
    }

    //Recycler View Adapter Class
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
            View view = getLayoutInflater().inflate(R.layout.list_feed_row, parent, false);
            return new RowHolder(view, listener);
        }

        @Override
        //Settings for when binding the view items and view resources
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            SongItem songItem = viewModel.getSongItems().getValue().get(position);
            holder.bind(songItem);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getSongItems().getValue() == null)
                return 0;
            return viewModel.getSongItems().getValue().size();
        }
    }
}