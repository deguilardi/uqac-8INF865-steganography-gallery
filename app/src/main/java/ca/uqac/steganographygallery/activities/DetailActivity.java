package ca.uqac.steganographygallery.activities;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.uqac.steganographygallery.R;
import ca.uqac.steganographygallery.Steganography;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PARAM_PICTURE = "PARAM_PICTURE";

    File mPictureFile;
    @BindView(R.id.thumb_view) ImageView mThumbView;
    @BindView(R.id.btn_save) Button mBtnSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Bundle b = getIntent().getExtras();
        String picturePath = Objects.requireNonNull(b).getString(PARAM_PICTURE);
        mPictureFile = new File(picturePath);
        mBtnSave.setOnClickListener(this);
        setupUI();
    }

    private void setupUI(){

        // define the image size based on device size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        Picasso.get()
                .load(mPictureFile)
                .resize(screenWidth, screenWidth)
                .centerCrop()
                .error(R.drawable.placeholder)
                .into(mThumbView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_save){
            AssetManager assetManager = getAssets();
            try {
                InputStream imageBytes = assetManager.open("file://" + mPictureFile.getAbsolutePath());
            } catch (IOException ignore) {
                Toast.makeText(this, "Error loading image file", Toast.LENGTH_SHORT).show();
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("file://" + mPictureFile.getAbsolutePath()));
                Steganography s = new Steganography(bitmap, "test");
                bitmap = s.hideMessage();
                FileOutputStream out = new FileOutputStream(mPictureFile.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
