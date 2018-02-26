package com.a4t8bfarm.a4t8b;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Interfaces.Users;
import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    /*public static final String PREFS = "prefFile";
    public static final String IS_LOGIN = "IsLogin";*/
    public static final int REQUEST_CODE=0;

    EditText UsernameEt, PasswordEt;
    SessionManagement session;
    protected DrawerLayout Drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_login, null, false);
        Drawer.addView(contentView, 0);*/
        onButtonClick();
        session = new SessionManagement(getApplicationContext());
        UsernameEt = (EditText) findViewById(R.id.edUsername);
        PasswordEt = (EditText) findViewById(R.id.edPassword);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }*/
    /*@Override
    public void onBackPressed() {
        //super.onBackPressed();
        // finish();

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }*/

    public void onLogin(View view) {
        final String username = UsernameEt.getText().toString();
        String password = PasswordEt.getText().toString();

        if (isEmptyField(UsernameEt)) return;
        if (isEmptyField(PasswordEt)) return;

        HashMap postData = new HashMap();
        postData.put("txtUsername", username);
        postData.put("txtPassword", password);

        PostResponseAsyncTask task = new PostResponseAsyncTask(LoginActivity.this, postData, new AsyncResponse() {
            @Override
            public void processFinish(String s) {

                try {
                    ArrayList<Users> userDetails = new JsonConverter<Users>().toArrayList(s, Users.class);
                    Users users = userDetails.get(0);

                    session.createLoginSession(users);

                    Intent i = getIntent();
                    String activity = i.getStringExtra("from");
                    if(activity.equals("MainActivity")){
                        Toast.makeText(LoginActivity.this, "Successful login", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                    else if(activity.equals("CartActivity")){
                        Toast.makeText(LoginActivity.this, "Successful login", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.putExtra("customer", username);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                } catch (Exception e) {
                    if (s.contains("Wrong username and password")){
                        AlertDialog.Builder dialogBox = new AlertDialog.Builder(LoginActivity.this);
                        dialogBox.setMessage("You have entered a wrong username and/or password.")
                                .setCancelable(false)
                                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();//closes the dialog box
                                    }
                                });

                        AlertDialog dialog = dialogBox.create();
                        dialog.setTitle("Login Failed");
                        dialog.show();
                    }
                    else {
                        //if the username and/or password is invalid, a dialog box appears informs them of possible errors
                        AlertDialog.Builder dialogBox = new AlertDialog.Builder(LoginActivity.this);
                        dialogBox.setMessage("An error occured while logging in. Please try again later.")
                                .setCancelable(false)
                                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();//closes the dialog box
                                    }
                                });

                        AlertDialog dialog = dialogBox.create();
                        dialog.setTitle("An Error was Detected!");
                        dialog.show();
                    }
                }
            }
        });
        task.execute("https://androidshopping.000webhostapp.com/loginv2.php/");
    }

    public void onButtonClick() {
        Button signUp = (Button) findViewById(R.id.btnSignUp);


        //By pressing the Sign-Up button, the Sign-up page appears
        signUp.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getIntent();
                String activity = i.getStringExtra("from");
                if(activity.equals("MainActivity")){
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/
                    intent.putExtra("from","MainActivity");
                    startActivity(intent);
                }
                else if(activity.equals("CartActivity")){
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/
                    intent.putExtra("from","CartActivity");
                    startActivityForResult(intent, REQUEST_CODE);
                }
                else{
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/
                    intent.putExtra("from","CartActivity");
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });

        //By pressing the forgot my password button, the forgot my password page opens
        /*FMP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ForgotPassActivity.class);
                startActivity(i);
            }
        });*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==REQUEST_CODE)
        {
            if(resultCode== Activity.RESULT_OK){
                String customer = data.getStringExtra("customer");
                Intent intent = new Intent(LoginActivity.this,CartActivity.class);
                intent.putExtra("customer", customer);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

        }
    }

    private boolean isEmptyField(EditText editText){
        boolean result = editText.getText().length() <= 0;
        if (result){
            AlertDialog.Builder dialogBox = new AlertDialog.Builder(LoginActivity.this);
            dialogBox.setMessage("Please fill-in all fields.")
                    .setCancelable(false)
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();//closes the dialog box
                        }
                    });
            AlertDialog dialog = dialogBox.create();
            dialog.setTitle("4T8B says:");
            dialog.show();

        }
        return result;
    }

}

