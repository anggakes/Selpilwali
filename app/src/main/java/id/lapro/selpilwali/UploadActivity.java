package id.lapro.selpilwali;
/**
 * Created by I on 05/11/2015.
 */
import id.lapro.selpilwali.AndroidMultiPartEntity.ProgressListener;
import java.io.File;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class UploadActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;
    //private Button btnUpload;
    private Profile profile;
    long totalSize = 0;
    //private TextView status;
    private  String nama = "angga";
    private String url = "angga@asdasd.com";
    ShareDialog shareDialog;
    CallbackManager callbackManager;

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
       // status = (TextView) findViewById(R.id.status);
       // btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);

        FacebookSdk.sdkInitialize(getApplicationContext());

        Profile profile = Profile.getCurrentProfile();
        nama = profile.getName();
        url = profile.getLinkUri().toString();
        shareDialog = new ShareDialog(this);
        callbackManager = CallbackManager.Factory.create();

        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result sharerResult) {
                // App code
                    /*
                    info.setText(
                            "User ID: "
                                    + loginResult.getAccessToken().getUserId()
                                    + "\n" +
                                    "Auth Token: "
                                    + loginResult.getAccessToken().getToken()
                    );
                    */
                // Profile profile = Profile.getCurrentProfile();
                // info.setText(profile.getName());
                showMain();
            }

            @Override
            public void onCancel() {
                // App code
                showAlert("Login attempt canceled.");
                showMain();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                showAlert(exception.toString());
                showMain();

            }

        });

        //status.setText(profile.getName());
        // Changing action bar background color
        //getActionBar().setBackgroundDrawable(
        // new ColorDrawable(Color.parseColor(getResources().getString(
        //       R.color.action_bar))));
        // Receiving the data from previous activity
        Intent i = getIntent();
        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");
        // boolean flag to identify the media type, image or video
        boolean isImage = i.getBooleanExtra("isImage", true);
        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }
        /*
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "haahha!", Toast.LENGTH_LONG).show();
                // uploading the file to server
            }
        });
        */
    }
    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            imgPreview.setImageBitmap(bitmap);

            UploadFileToServer kirim = new UploadFileToServer(this);
            kirim.profilett = profile;
            //kirim.shareDialog = shareDialog;
            kirim.execute();

        } else {
            imgPreview.setVisibility(View.GONE);
        }
    }
    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        private Profile profilett;
        //private ShareDialog shareDialog;
        private Context context;

        public UploadFileToServer(Context context){
            this.context = context;
            FacebookSdk.sdkInitialize(getApplicationContext());

        }

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);
            // updating progress bar value
            progressBar.setProgress(progress[0]);
            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }
        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }
        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                File sourceFile = new File(filePath);
                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));
                entity.addPart("nama", new StringBody(String.valueOf(nama)));
                entity.addPart("email", new StringBody(String.valueOf(url) ));
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);
                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            return responseString;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);
            // showing the server response in an alert dialog
            showAlert("Harap Tunggu ...");
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("SELPILWALI - Saya akan Memilih pada pilwali 2015")
                        .setContentDescription(
                                "Saya "+String.valueOf(nama)+", saya berjanju untuk ikut memilih pada pilwali Surabaya 2015 nanti. ayo bergabung dengan saya !")
                        .setContentUrl(Uri.parse("http://selpilwali.lapro.id"))
                        .build();

                shareDialog.show(linkContent);
            }

            // We need an Editor object to make preference changes.
            // All objects are from android.context.Context
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("selesai", true);
            editor.putString("pilihan", "memilih");
            // Commit the edits!
            editor.commit();

            super.onPostExecute(result);
        }
    }
    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void showMain(){
        // Send logged in users to Welcome.class
        Intent intent = new Intent(this, SelesaiActivity.class);
        startActivity(intent);
        finish();
    }
}