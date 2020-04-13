package ca.uqac.steganographygallery;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.PicturesAdapterViewHolder>{

    Activity mContext;
    private ArrayList<String> mData;
    final private PicturesAdapterOnClickHandler mClickHandler;
    private int mNumColumns;
    private int mSize;

    public interface PicturesAdapterOnClickHandler {
        void onClick(String filePath, PicturesAdapter.PicturesAdapterViewHolder adapterViewHolder);
    }

    public PicturesAdapter(@NonNull Activity context, PicturesAdapterOnClickHandler clickHandler, int numColumns) {
        mContext = context;
        mClickHandler = clickHandler;
        mNumColumns = numColumns;

        // define the image size based on device size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        mSize = screenWidth / mNumColumns;
    }

    @NonNull
    @Override
    public PicturesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pictures_list_item, viewGroup, false);
        view.setFocusable(true);
        return new PicturesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PicturesAdapterViewHolder adapterViewHolder, int position) {

        // set the thumb height to fix the elements on the screen and avoid "dancing"
        adapterViewHolder.thumbView.setMinimumHeight(mSize);

        // parse te result
        String picturePath = mData.get(position);
        Picasso.with(mContext).load(picturePath)
                .resize(mSize, mSize)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(adapterViewHolder.thumbView);
    }

    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    public void swapData(ArrayList<String> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public class PicturesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.thumb_view) ImageView thumbView;

        PicturesAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);

            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = mSize;
        }

        public ImageView getThumbView(){
            return thumbView;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mClickHandler.onClick(mData.get(position), this);
        }
    }
}