package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.user.User;

/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class EditUserProfileFragment extends DialogFragment {

    public static final int REQ_CODE_PICK_IMAGE = 1;
    public static final String TEMP_PHOTO_FILE = "calendula_user_profile_image";
    public static final String USER_PROFILE_IMG_PATH = "user_profile_image.png";


    OnProfileEditListener mProfileEditCallback;

    EditText mNameTextView;
    Button mConfirmButton;
    ImageView profileImage;
    Bitmap bitmap;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mNameTextView = (EditText) rootView.findViewById(R.id.profile_username);
        mConfirmButton = (Button) rootView.findViewById(R.id.profile_button_ok);
        profileImage = (ImageView) rootView.findViewById(R.id.profile_image);

        User u = Session.instance().getUser();
        mNameTextView.setText(u.getName());

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEdit();
            }
        });

        Bitmap userProfileImg = Session.instance().getUserProfileImage(getActivity());

        if (userProfileImg != null) {
            profileImage.setImageBitmap(userProfileImg);
        }
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findImage();
            }
        });

        if (getDialog() != null) {
            getDialog().setTitle("Edit profile");
        }

        return rootView;
    }


    private void onEdit() {

        String name = mNameTextView.getText().toString();
        boolean needSave = false;
        if (bitmap != null) {
            saveProfilePicture(getActivity(), bitmap);
            Session.instance().getUser().setProfileImagePath(USER_PROFILE_IMG_PATH);
            needSave = true;

        }
        if (name != null && name.length() > 0) {
            Session.instance().getUser().setName(name);
            needSave = true;

        } else {
            mNameTextView.setError("Please, type a valid name");
            mNameTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    mNameTextView.setError(null);
                    mNameTextView.removeTextChangedListener(this);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }

        if (needSave) {
            try {
                Session.instance().save(getActivity());
                mProfileEditCallback.onProfileEdited(Session.instance().getUser());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    void saveProfilePicture(Context ctx, Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = ctx.openFileOutput(USER_PROFILE_IMG_PATH, Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void findImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra("crop", "true");
        photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
        photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
    }


    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    private File getTempFile() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File file = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file;
        } else {

            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:

                if (imageReturnedIntent != null) {

                    File tempFile = getTempFile();
                    String filePath = Environment.getExternalStorageDirectory() + "/" + TEMP_PHOTO_FILE;
                    Log.d(getTag(), "path: " + filePath);
                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    bitmap = selectedImage;
                    profileImage.setImageBitmap(selectedImage);
                    if (tempFile.exists()) tempFile.delete();
                }
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnProfileEditListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnProfileEditListener) {
            Log.d(getTag(), "Set onProfileEditListener onAttach");
            mProfileEditCallback = (OnProfileEditListener) activity;
        }
    }

    // Container Activity must implement this interface
    public interface OnProfileEditListener {
        public void onProfileEdited(User u);
    }


    public void setOnProfileEditListener(OnProfileEditListener l) {
        this.mProfileEditCallback = l;
    }


}