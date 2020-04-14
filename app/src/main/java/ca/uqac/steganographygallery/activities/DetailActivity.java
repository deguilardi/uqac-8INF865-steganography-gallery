package ca.uqac.steganographygallery.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.airbnb.lottie.LottieAnimationView;
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
    @BindView(R.id.thumb_view) AppCompatImageView mThumbView;
    @BindView(R.id.btn_save) AppCompatButton mBtnSave;
    @BindView(R.id.txt_edit) AppCompatEditText mTxtEdit;
    @BindView(R.id.content_holder) LinearLayoutCompat mContentHolder;
    @BindView(R.id.spinner) LottieAnimationView mSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // retrieve selected picture path from the main activity
        Bundle bundle = getIntent().getExtras();
        String picturePath = Objects.requireNonNull(bundle).getString(PARAM_PICTURE);
        mPictureFile = new File(Objects.requireNonNull(picturePath));

        // decode and show initial message
        Runnable runnable = () -> {
            Bitmap bitmap = BitmapFactory.decodeFile(mPictureFile.getAbsolutePath());
            Steganography s11y = new Steganography(bitmap);
            bitmap.recycle();
            String hiddenText = s11y.getHiddenMessage();
            s11y.free();
            if (!hiddenText.equals("")) {
                runOnUiThread(() -> mTxtEdit.setText(hiddenText));
            }
        };
        new Thread(runnable).start();

        mBtnSave.setOnClickListener(this);
        setupUI();
    }

    private void setupUI(){

        // define the image size based on device size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int size = Math.min(screenWidth, screenHeight);

        Picasso.get()
                .load(mPictureFile)
                .resize(size, size)
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
            hideKeyboard();
            mSpinner.setVisibility(View.VISIBLE);
            mContentHolder.setVisibility(View.GONE);

            final Context self = this;
            Runnable runnable = () -> {
                try {
                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap original = BitmapFactory.decodeFile(mPictureFile.getAbsolutePath(), op);
                    Steganography s11y = new Steganography(original);
                    original.recycle();
                    Bitmap bitmap = s11y.hideMessage(Objects.requireNonNull(mTxtEdit.getText()).toString());
                    FileOutputStream out = new FileOutputStream(mPictureFile.getAbsolutePath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                    bitmap.recycle();
                    s11y.free();
                    runOnUiThread(() -> Toast.makeText(self, R.string.saving_success_alert, Toast.LENGTH_SHORT).show());
                } catch (IOException ignore) {
                    runOnUiThread(() -> Toast.makeText(self, R.string.saving_error_alert, Toast.LENGTH_SHORT).show());
                } finally {
                    runOnUiThread(() -> {
                        mSpinner.setVisibility(View.GONE);
                        mContentHolder.setVisibility(View.VISIBLE);
                    });
                }
            };
            new Thread(runnable).start();
        }
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
