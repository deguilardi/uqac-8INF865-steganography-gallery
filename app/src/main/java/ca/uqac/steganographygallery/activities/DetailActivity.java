package ca.uqac.steganographygallery.activities;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    @BindView(R.id.txt_edit) EditText mTxtEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Bundle b = getIntent().getExtras();
        String picturePath = Objects.requireNonNull(b).getString(PARAM_PICTURE);
        mPictureFile = new File(picturePath);
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeFile(mPictureFile.getAbsolutePath());
        Steganography s = new Steganography(bitmap, "");
        String hiddenText = s.getHiddenMessage();
        if(!hiddenText.equals("")) {
            mTxtEdit.setText(hiddenText);
        }
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

                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(mPictureFile.getAbsolutePath(), op);
                Steganography s1 = new Steganography(bitmap, mTxtEdit.getText().toString());
                bitmap = s1.hideMessage();
                Steganography s2 = new Steganography(bitmap, "");
                Toast.makeText(this, s2.getHiddenMessage(), Toast.LENGTH_SHORT).show();

                FileOutputStream out = new FileOutputStream(mPictureFile.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
