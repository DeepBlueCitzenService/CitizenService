package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ExpImageRVAdapter extends RecyclerView.Adapter<ExpImageRVAdapter.ViewHolder> {

    private Context context;
    private List<String> imageUrlList;
    private ImageView expandedImage;

    public ExpImageRVAdapter(Context context, List<String> bitmapList, ImageView expandedImage){
        this.context = context;
        this.imageUrlList = bitmapList;
        this.expandedImage = expandedImage;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int size = parent.getHeight();
        ImageView imageView = new ImageView(context);
        ViewGroup.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
        imageView.setLayoutParams(lp);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        new AsyncTask<String, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... strings) {
                try {
                    return bitmapFromUrl(strings[0]);
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result){
                if(result == null)
                    holder.imageView.setVisibility(View.GONE);
                else {
                    holder.imageView.setImageBitmap(result);
                    setImageClickListener(holder.imageView);
                }
            }

        }.execute(imageUrlList.get(position));
    }

    private Bitmap bitmapFromUrl(String url) throws IOException {
        Bitmap bitmap;

        HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        bitmap = BitmapFactory.decodeStream(input);
        return bitmap;
    }

    private void setImageClickListener(final ImageView imageView){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                expandedImage.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
      }
    }
}
