package ca.uqac.steganographygallery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.uqac.steganographygallery.Config;
import ca.uqac.steganographygallery.PicturesAdapter;
import ca.uqac.steganographygallery.R;

public class MainActivity extends AppCompatActivity implements PicturesAdapter.PicturesAdapterOnClickHandler {


    private PicturesAdapter mPicturesAdapter;

    @BindView(R.id.recyclerview_pic_list) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //pour la permission de stockage
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1000);

        }
        else
            {
            // affiche l'image
                ButterKnife.bind(this);
                setupUI();
                loadPictures();
            }



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

        String path = Environment.getExternalStorageDirectory().toString()+"/Images-";
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++){
            picturesList.add(path);
        }

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
        Intent detailsActivityIntent = new Intent(MainActivity.this, DetailActivity.class);
        detailsActivityIntent.putExtra(DetailActivity.PARAM_PICTURE, filePath);
        startActivity(detailsActivityIntent, options.toBundle());
    }


   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
   {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1000)
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //show the image
                ButterKnife.bind(this);
                setupUI();
                loadPictures();

                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                finish();

            }
        }
    }
}
