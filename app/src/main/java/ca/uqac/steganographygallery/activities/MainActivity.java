package ca.uqac.steganographygallery.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.uqac.steganographygallery.Config;
import ca.uqac.steganographygallery.PicturesAdapter;
import ca.uqac.steganographygallery.R;
import ca.uqac.steganographygallery.Steganography;

public class MainActivity extends AppCompatActivity implements PicturesAdapter.PicturesAdapterOnClickHandler {

    private PicturesAdapter mPicturesAdapter;

    @BindView(R.id.recyclerview_pic_list) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("stegano", "test");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupUI();
        loadPictures();
    }

    private void setupUI(){

        // define num of columns based on device orientation
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int numColumns = Config.HOME_LIST_NUM_COLUMNS;
        numColumns = display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180
                ? numColumns
                : (int) (numColumns * Config.HOME_LIST_COLUMNS_RATIO);

        // start layout and adapter
        GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mPicturesAdapter = new PicturesAdapter(this, this, numColumns);
        mRecyclerView.setAdapter(mPicturesAdapter);
    }

    private void loadPictures(){
        // @TODO load pictures on the device here

        ArrayList<String> picturesList = new ArrayList<>(8);
        picturesList.add(getURLForResource(R.drawable.test1));
        picturesList.add(getURLForResource(R.drawable.test2));
        picturesList.add(getURLForResource(R.drawable.test3));
        picturesList.add(getURLForResource(R.drawable.test4));
        picturesList.add(getURLForResource(R.drawable.test5));
        picturesList.add(getURLForResource(R.drawable.test6));
        picturesList.add(getURLForResource(R.drawable.test7));
        picturesList.add(getURLForResource(R.drawable.test8));
        mPicturesAdapter.swapData(picturesList);
    }

    public String getURLForResource (int resourceId) {
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceId).toString();
    }

    @Override
    public void onClick(String filePath, PicturesAdapter.PicturesAdapterViewHolder adapterViewHolder) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this,
                adapterViewHolder.getThumbView(),
                getString(R.string.transition_thumb)
        );
        //Steganography s = new Steganography(filePath, "test");
        Intent detailsActivityIntent = new Intent(MainActivity.this, DetailActivity.class);
        detailsActivityIntent.putExtra(DetailActivity.PARAM_PICTURE, filePath);
        startActivity(detailsActivityIntent, options.toBundle());
    }
}
