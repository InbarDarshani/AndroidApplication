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
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.viewmodels.ProfileViewModel;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {
    ProfileViewModel viewModel;
    RecyclerView list;
    ListAdapter adapter;
    SwipeRefreshLayout swipeRefresh;
    ImageView profile_user_iv;
    TextView profile_user_tv, profile_empty_tv;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Get views
        profile_user_iv = view.findViewById(R.id.profile_user_iv);
        profile_user_tv = view.findViewById(R.id.profile_user_tv);
        profile_empty_tv = view.findViewById(R.id.profile_empty_tv);

        //Set current user's data
        if (!viewModel.getUser().getImage().isEmpty())
            Picasso.get().load(viewModel.getUser().getImage()).into(profile_user_iv);
        profile_user_tv.setText(viewModel.getUser().getDisplayName());

        //Setup refresh view, attach OnRefresh function, set refreshing state according to Model's loading state
        swipeRefresh = view.findViewById(R.id.profile_swiperefresh);
        swipeRefresh.setOnRefreshListener(Model.instance::refreshProfile);
        swipeRefresh.setRefreshing(Model.instance.getProfileLoadingState().getValue() == Model.ProfileState.loading);

        //Set list and adapter
        list = view.findViewById(R.id.profile_rv);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new ListAdapter();
        list.setAdapter(adapter);

        //Create the row items listeners actions
        adapter.setOnItemClickListener((v, position) -> {
            //get mixtape item from view model
            String mixtapeId = viewModel.getMixtapeItems().getValue().get(position).mixtape.getMixtapeId();
            //navigate to sod details page
            Navigation.findNavController(v).navigate(ProfileFragmentDirections.actionProfileFragmentToMixtapeDetailsFragment(mixtapeId));
        });

        //Setup observer for ViewModel's livedata, set OnChange action
        viewModel.getMixtapeItems().observe(getViewLifecycleOwner(), mixtapeItems -> ProfileFragment.this.refresh());

        //Setup observer for Model's profile loading state
        Model.instance.getProfileLoadingState().observe(getViewLifecycleOwner(), profileLoadingState -> {
            //Change SwipeRefreshLayout according to loading state
            swipeRefresh.setRefreshing(profileLoadingState != Model.ProfileState.loaded);

            //Treat an empty list state
            if (profileLoadingState == Model.ProfileState.empty) {
                list.setVisibility(View.GONE);
                profile_empty_tv.setVisibility(View.VISIBLE);
            } else {
                list.setVisibility(View.VISIBLE);
                profile_empty_tv.setVisibility(View.GONE);
            }
        });

        return view;
    }

    //Actions to perform on data refresh in this fragment
    private void refresh() {
        adapter.notifyDataSetChanged();
    }

    //______________________ List Listeners Interface ______________________________________
    //Interface wrapper for a list item listeners
    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //______________________ Recycler View Holder Class _____________________________
    //Holds song's row view items and links them to the view resources
    class RowHolder extends RecyclerView.ViewHolder {
        TextView profilerow_mixtape_name_tv;
        TextView profilerow_mixtape_description_tv;

        public RowHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            //Set row view resources
            profilerow_mixtape_name_tv = itemView.findViewById(R.id.profilerow_mixtape_name_tv);
            profilerow_mixtape_description_tv = itemView.findViewById(R.id.profilerow_mixtape_description_tv);

            //Sets row listeners
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos);
            });
        }

        void bind(MixtapeItem mixtapeItem) {
            //Bind Mixtape data of this song post
            profilerow_mixtape_name_tv.setText(mixtapeItem.mixtape.getName());
            profilerow_mixtape_description_tv.setText(mixtapeItem.mixtape.getDescription());
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
            View view = getLayoutInflater().inflate(R.layout.list_profile_row, parent, false);
            return new RowHolder(view, listener);
        }

        @Override
        //Settings for when binding the view items and view resources
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            MixtapeItem mixtapeItem = viewModel.getMixtapeItems().getValue().get(position);
            holder.bind(mixtapeItem);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getMixtapeItems().getValue() == null)
                return 0;
            return viewModel.getMixtapeItems().getValue().size();
        }
    }
}