package ca.uqac.steganographygallery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.uqac.steganographygallery.Config;
import ca.uqac.steganographygallery.PicturesAdapter;
import ca.uqac.steganographygallery.R;

public class MainActivity extends AppCompatActivity implements PicturesAdapter.PicturesAdapterOnClickHandler {

    private static final int PERMISSIONS_REQUEST_CODE = 1000;
    private PicturesAdapter mPicturesAdapter;

    @BindView(R.id.recyclerview_pic_list) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("steganoTag", "test");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //pour la permission de stockage
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_CODE);
        }
        else{
            // affiche l'image
            ButterKnife.bind(this);
            setupUI();
            loadPictures();
        }
    }

    private void setupUI(){

        // define num of columns based on device orientation
        Display display = ((WindowManager) Objects.requireNonNull(getSystemService(WINDOW_SERVICE))).getDefaultDisplay();
        int numColumns = Config.HOME_LIST_NUM_COLUMNS;
        numColumns = display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180
                ? numColumns
                : numColumns * Config.HOME_LIST_COLUMNS_RATIO;

        // start layout and adapter
        GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mPicturesAdapter = new PicturesAdapter(this, this, numColumns);
        mRecyclerView.setAdapter(mPicturesAdapter);
    }

    private void loadPictures(){
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,null,null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);

        if(cursor != null && cursor.moveToFirst()) {
            ArrayList<String> picturesList = new ArrayList<>(cursor.getCount());
            while (!cursor.isAfterLast()) {
                picturesList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                cursor.moveToNext();
            }
            cursor.close();
            mPicturesAdapter.swapData(picturesList);
        }
        else{
            Toast.makeText(this, R.string.loading_error_alert, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSelectItem(String filePath, PicturesAdapter.PicturesAdapterViewHolder adapterViewHolder) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this,
                adapterViewHolder.getThumbView(),
                getString(R.string.transition_thumb)
        );
        Intent detailsActivityIntent = new Intent(MainActivity.this, DetailActivity.class);
        detailsActivityIntent.putExtra(DetailActivity.PARAM_PICTURE, filePath);
        startActivity(detailsActivityIntent, options.toBundle());
    }


   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
   {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSIONS_REQUEST_CODE)
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //show the image
                ButterKnife.bind(this);
                setupUI();
                loadPictures();
            }
            else{
                Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
