package com.example.mixtape.app;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mixtape.MyApplication;
import com.example.mixtape.NavGraphDirections;
import com.example.mixtape.R;
import com.example.mixtape.model.MixtapeItem;
import com.example.mixtape.model.Model;
import com.example.mixtape.viewmodels.UserViewModel;
import com.example.mixtape.viewmodels.UserViewModelFactory;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class UserFragment extends Fragment {
    UserViewModel viewModel;
    RecyclerView list;
    ListAdapter adapter;
    SwipeRefreshLayout swipeRefresh;
    ImageView user_image_iv;
    TextView user_empty_list_tv;
    ImageButton profile_gallery_btn, profile_cam_btn, profile_edit_name_btn, profile_save_name_btn, profile_cancel_name_btn, profile_add_mixtape_btn;
    EditText user_name_et;
    ProgressBar profile_save_progressbar;
    Bitmap inputImage;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String userId = UserFragmentArgs.fromBundle(getArguments()).getUserId();
        viewModel = new ViewModelProvider(this, new UserViewModelFactory(userId)).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        //Get views
        user_image_iv = view.findViewById(R.id.user_image_iv);
        user_name_et = view.findViewById(R.id.user_name_et);
        user_empty_list_tv = view.findViewById(R.id.user_empty_list_tv);
        profile_gallery_btn = view.findViewById(R.id.profile_gallery_btn);
        profile_cam_btn = view.findViewById(R.id.profile_cam_btn);
        profile_edit_name_btn = view.findViewById(R.id.profile_edit_name_btn);
        profile_save_name_btn = view.findViewById(R.id.profile_save_name_btn);
        profile_cancel_name_btn = view.findViewById(R.id.profile_cancel_name_btn);
        profile_save_progressbar = view.findViewById(R.id.profile_save_progressbar);
        profile_add_mixtape_btn = view.findViewById(R.id.profile_add_mixtape_btn);

        //Set user's data
        if (!viewModel.getUser().getImage().isEmpty())
            Picasso.get().load(viewModel.getUser().getImage()).into(user_image_iv);
        user_name_et.setText(viewModel.getUser().getDisplayName());

        //Setup refresh view, attach OnRefresh function, set refreshing state according to Model's loading state
        swipeRefresh = view.findViewById(R.id.profile_swiperefresh);
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
        swipeRefresh.setRefreshing(Model.instance.getProfileLoadingState().getValue() == Model.ProfileState.loading);

        //Set list and adapter
        list = view.findViewById(R.id.user_rv);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        adapter = new ListAdapter();
        list.setAdapter(adapter);

        //Create the row items listener actions
        adapter.setOnItemClickListener((v, position) -> {
            //Get mixtape item from view model
            String mixtapeId = viewModel.getMixtapeItems().getValue().get(position).mixtape.getMixtapeId();
            //Navigate to mixtape details page
            Navigation.findNavController(v).navigate(UserFragmentDirections.actionGlobalMixtapeDetailsFragment(mixtapeId));
        });

        //Setup observer for ViewModel's livedata
        viewModel.getMixtapeItems().observe(getViewLifecycleOwner(), mixtapeItems -> adapter.notifyDataSetChanged());

        //Setup observer for Model's profile loading state
        Model.instance.getProfileLoadingState().observe(getViewLifecycleOwner(), profileLoadingState -> {
            adapter.notifyDataSetChanged();

            //Change SwipeRefresh according to loading state
            swipeRefresh.setRefreshing(profileLoadingState == Model.ProfileState.loading);

            //Treat an empty list state
            if (profileLoadingState == Model.ProfileState.empty) {
                list.setVisibility(View.GONE);
                user_empty_list_tv.setVisibility(View.VISIBLE);
            } else {
                list.setVisibility(View.VISIBLE);
                user_empty_list_tv.setVisibility(View.GONE);
            }
        });

        //Setup profile if its current user's
        currentUserSetup();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void currentUserSetup() {
        String currentUserId = MyApplication.getContext().getSharedPreferences("USER", Context.MODE_PRIVATE).getString("userId", "");

        //Check if its the profile of current user
        if (!viewModel.getUser().getUserId().equals(currentUserId))
            return;

        //Expose Buttons
        profile_cam_btn.setVisibility(View.VISIBLE);
        profile_gallery_btn.setVisibility(View.VISIBLE);
        profile_edit_name_btn.setVisibility(View.VISIBLE);
        profile_add_mixtape_btn.setVisibility(View.VISIBLE);

        //Setup buttons listeners
        profile_cam_btn.setOnClickListener(v -> openCam());
        profile_gallery_btn.setOnClickListener(v -> openGallery());
        profile_add_mixtape_btn.setOnClickListener(v -> Navigation.findNavController(v).navigate(NavGraphDirections.actionGlobalAddMixtapeFragment()));

        //Edit username button
        profile_edit_name_btn.setOnClickListener(v -> {
            user_name_et.setEnabled(true);
            profile_edit_name_btn.setVisibility(View.GONE);
            profile_cancel_name_btn.setVisibility(View.VISIBLE);
            profile_save_name_btn.setVisibility(View.VISIBLE);
        });

        //Save username button
        profile_save_name_btn.setOnClickListener(v -> {
            profile_save_progressbar.setVisibility(View.VISIBLE);
            String inputUserName = user_name_et.getText().toString();
            if (inputUserName.isEmpty()) {
                Toast.makeText(MyApplication.getContext(), "Cant save empty name", Toast.LENGTH_LONG).show();
                return;
            }
            viewModel.getUser().setDisplayName(inputUserName);
            Model.instance.updateUser(viewModel.getUser(), () -> {
                profile_save_progressbar.setVisibility(View.GONE);
            });
            //Trigger cancel button to fix views
            profile_cancel_name_btn.callOnClick();
        });

        //Cancel username button
        profile_cancel_name_btn.setOnClickListener(v -> {
            user_name_et.setText(viewModel.getUser().getDisplayName());
            user_name_et.setEnabled(false);
            profile_edit_name_btn.setVisibility(View.VISIBLE);
            profile_cancel_name_btn.setVisibility(View.GONE);
            profile_save_name_btn.setVisibility(View.GONE);
        });
    }

    //______________________ Camera and Gallery Setup _____________________________
    //Activity launcher for result
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        //Create initial bitmap
                        Bitmap bitmap = null;

                        //If its a Camera activity get bitmap data directly from extras (Not Full Size Image)
                        if (result.getData().hasExtra("data")) {
                            bitmap = (Bitmap) result.getData().getExtras().get("data");
                        }
                        //If its a Gallery activity create bitmap data from image uri
                        else {
                            try {
                                Uri imageUri = result.getData().getData();
                                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //Convert bitmap image to thumbnail and set in view
                        if (bitmap != null) {
                            int wScale = (bitmap.getWidth() > user_image_iv.getWidth()) ? bitmap.getWidth() / user_image_iv.getWidth() : 1;
                            int hScale = (bitmap.getHeight() > user_image_iv.getHeight()) ? bitmap.getHeight() / user_image_iv.getHeight() : 1;
                            int w = bitmap.getWidth() / wScale;
                            int h = bitmap.getHeight() / hScale;
                            inputImage = Bitmap.createScaledBitmap(bitmap, w, h, false);
                            user_image_iv.setImageBitmap(inputImage);

                            //Save image to user
                            Model.instance.uploadUserImage(inputImage, viewModel.getUser(), user -> {
                            });
                        }
                    }
                }
            });

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        activityResultLauncher.launch(galleryIntent);
    }

    private void openCam() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher.launch(cameraIntent);
    }

    //______________________ Recycler View Adapter Setup _____________________________
    //List Listeners Interface
    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //Recycler View Holder Class
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
                listener.onItemClick(v, getAdapterPosition());
            });
        }

        void bind(@NonNull MixtapeItem mixtapeItem) {
            //Bind Mixtape data of this song post
            profilerow_mixtape_name_tv.setText(mixtapeItem.mixtape.getName());
            profilerow_mixtape_description_tv.setText(mixtapeItem.mixtape.getDescription());
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