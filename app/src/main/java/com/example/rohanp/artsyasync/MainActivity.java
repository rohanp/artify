package com.example.rohanp.artsyasync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.logging.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.ViewGroup;

import com.hs.image.ImageUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
    ProgressDialog prgDialog;
    String encodedString;
    String artString;
    RequestParams params = new RequestParams();
    String imgPath, fileName;
    Bitmap bitmap;
    private static int RESULT_LOAD_IMG = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static int REQUEST_CAMERA = 0;
    private boolean CAMERA = false;
    private Uri mImageUri;

    private String photoFilePath;

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }

    public boolean openCamera(View view){

        try {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File f = ImageUtils.createImageFile(ImageUtils.getPackageName(this));
            photoFilePath =  f.toString();

            if ((f != null) && f.exists()) {
                mImageUri = Uri.fromFile(f);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                startActivityForResult(intent, CAMERA_REQUEST);
                CAMERA = true;
            }
        } catch (Exception e) {
            Log.e("ey", Log.getStackTraceString(e));
        }

        return true;
    }

    Integer[] imageIDs = {
            R.drawable.monalisa,
            R.drawable.starrynight,
            R.drawable.matisse,
            R.drawable.scream,
            R.drawable.seatednude,
            R.drawable.matisse2,
            R.drawable.memory
    };

    public void chooseFilter(View view) {
        setContentView(R.layout.activity_filter);
        Gallery gallery = (Gallery) findViewById(R.id.gallery1);
        gallery.setAdapter(new ImageAdapter(this));
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // display the images selected
                ImageView imageView = (ImageView) findViewById(R.id.image1);
                imageView.setImageResource(imageIDs[position]);
            }
        });
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private int itemBackground;
        public ImageAdapter(Context c)
        {
            context = c;
            // sets a grey background; wraps around the images
            TypedArray a =obtainStyledAttributes(R.styleable.MyGallery);
            itemBackground = a.getResourceId(R.styleable.MyGallery_android_galleryItemBackground, 0);
            a.recycle();
        }
        // returns the number of images
        public int getCount() {
            return imageIDs.length;
        }
        // returns the ID of an item
        public Object getItem(int position) {
            return position;
        }

        // returns the ID of an item
        public long getItemId(int position) {
            return position;
        }
        // returns an ImageView view
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(imageIDs[position]);
            imageView.setLayoutParams(new Gallery.LayoutParams(300, 300));
            imageView.setBackgroundResource(itemBackground);
            return imageView;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imgView = (ImageView) findViewById(R.id.imgView);

        Log.d("camera: ", Boolean.toString(CAMERA));

        try {

            if(CAMERA){
                Log.d("here", "yup");

                Bitmap photo = BitmapFactory.decodeFile(photoFilePath);
                photo = Bitmap.createScaledBitmap(photo,(int)(photo.getWidth()*0.6), (int)(photo.getHeight()*0.6), true);
                imgView.setImageBitmap(photo);

                String fileNameSegments[] = photoFilePath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                params.put("filename", fileName);
                Log.d("stillhere", "yup");
            }
            else if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();

                // Set the Image in ImageView
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgPath));
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                // Put file name in Async Http Post Param which will used in php web app
                params.put("filename", fileName);

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }
    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery
        if (imgPath != null && !imgPath.isEmpty() || photoFilePath != null) {
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            // Convert image to String using Base64
            encodeImagetoString();
            // When Image is not selected from Gallery
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }
    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;

                Log.d("hi", photoFilePath);

                if(CAMERA)
                    bitmap = BitmapFactory.decodeFile(photoFilePath,
                            options);
                else
                    bitmap = BitmapFactory.decodeFile(imgPath,
                            options);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.2), (int)(bitmap.getHeight()*0.2), true);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byte_arr = stream.toByteArray();
                encodedString = Base64.encodeToString(byte_arr, 0);

                Drawable d = ((ImageView) findViewById(R.id.image1)).getDrawable();
                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.3), (int)(bitmap.getHeight()*0.3), true);
                stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byte_arr_2 = stream.toByteArray();
                artString = Base64.encodeToString(byte_arr_2, 0);

                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
                params.put("image", encodedString);
                params.put("art", artString);
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to PHP server
    public void makeHTTPCall() {
        prgDialog.setMessage("Invoking PHP");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post("http://rpandit2-80.terminal.com/upload_image.php",
                params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(String response) {
                        // Hide Progress Dialog
                        prgDialog.hide();

                        Log.e("link", response);

                        try {
                            displayImage(response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    public void displayImage(String response) throws IOException, MalformedURLException {
        final String url = response;
        setContentView(R.layout.activity_output);
        ImageView outputView = (ImageView) findViewById(R.id.output);
        outputView.setImageBitmap(BitmapFactory.decodeFile(photoFilePath));

        final Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(getApplicationContext(), (String) message.obj, Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable() {
            ImageView outputView = (ImageView) findViewById(R.id.output);

            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                for(int i=50; i<1000; i+=50) {
                    try {
                        Thread.sleep(10000);                 //1000 milliseconds is one second.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    try {
                        while(getResponseCode(url.substring(0, url.lastIndexOf('.')) + "_" + i + ".png") == 404){
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    new DownloadImageTask(outputView).execute(url.substring(0, url.lastIndexOf('.')) + "_" + i + ".png");
                    Message message = mHandler.obtainMessage(1, i + "th iteration");
                    message.sendToTarget();

                }
                new DownloadImageTask(outputView).execute(url);
                Message message = mHandler.obtainMessage(1, "Final image!");
                message.sendToTarget();
            }
        }).start();

    }

    public static int getResponseCode(String urlString) throws MalformedURLException, IOException {
        URL u = new URL(urlString);
        HttpURLConnection huc =  (HttpURLConnection)  u.openConnection();
        huc.setRequestMethod("GET");
        huc.connect();
        return huc.getResponseCode();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

}