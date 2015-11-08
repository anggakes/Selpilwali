package id.lapro.selpilwali;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GolputActivity extends AppCompatActivity {

    public EditText golputText;
    public Button btnGolput;
    public Profile profil;

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_golput);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        golputText = (EditText) findViewById(R.id.golputText);
        btnGolput = (Button) findViewById(R.id.btnGolput);


        FacebookSdk.sdkInitialize(getApplicationContext());

        final Profile profil = Profile.getCurrentProfile();
        //status.setText(profile.getName());

        btnGolput.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // record video
                kirimGolput(golputText.getText().toString(), profil);

                bukaMain();
            }
        });

    }

    public void bukaMain(){
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish();
    }

    public void kirimGolput(final String alasan, final Profile profil){
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
               return  kirim();
            }

            private String kirim(){
                // String paramAddress = params[1];
                /*
                // String name = editTextName.getText().toString();
                String alasan = golputText.getText().toString();
                String nama = profil.getName();
                String email = profil.getLinkUri().toString();
                //String alasan = golputText.getText().toString();

                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("nama", nama));
                nameValuePairs.add(new BasicNameValuePair("email",email));
                nameValuePairs.add(new BasicNameValuePair("alasan", alasan));
                */

                //String nama = "sasas";
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("nama", profil.getName()));
                nameValuePairs.add(new BasicNameValuePair("email", profil.getLinkUri().toString()));
                nameValuePairs.add(new BasicNameValuePair("alasan", alasan));

                String responseString = null;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://selpilwali.lapro.id/index.php/golput");

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

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

                } catch (IOException e) {

                }

                return responseString;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);


                Toast.makeText(getApplicationContext(), "Terima Kasih sob", Toast.LENGTH_LONG).show();
                // We need an Editor object to make preference changes.

                // All objects are from android.context.Context
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("selesai", true);
                editor.putString("pilihan", "golput");
                // Commit the edits!
                editor.commit();

                //TextView textViewResult = (TextView) findViewById(R.id.textViewResult);
                //textViewResult.setText("Inserted");
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();
    }

    public void showMain(){
        // Send logged in users to Welcome.class
        Intent intent = new Intent(this, SelesaiActivity.class);
        startActivity(intent);
        finish();
    }




}
