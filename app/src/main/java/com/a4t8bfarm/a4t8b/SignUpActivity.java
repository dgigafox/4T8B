package com.a4t8bfarm.a4t8b;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Interfaces.Users;
import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    public EditText firstname, lastname, uN, pW, pW2, emailAddress, phoneNum, deliveryAddress, zipCode;
    String LOG = "SignUpActivity";
    SessionManagement session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManagement(getApplicationContext());
        firstname = (EditText) findViewById(R.id.firstname);
        lastname = (EditText) findViewById(R.id.lastname);
        uN = (EditText) findViewById(R.id.username);
        pW = (EditText) findViewById(R.id.password);
        pW2 = (EditText) findViewById (R.id.confirmPassword);
        emailAddress = (EditText) findViewById(R.id.email);
        phoneNum = (EditText) findViewById(R.id.mobilePhone);
        deliveryAddress = (EditText) findViewById(R.id.deliveryAddress);
        zipCode = (EditText) findViewById(R.id.zipCode);
        OnButtonClick();

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    public void OnButtonClick() {
        Button Return = (Button) findViewById(R.id.loginReturn);
        Button createAccountBtn = (Button) findViewById(R.id.createAccountButton);

        //Upon being pressed, user is taken back to the login page
        Return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);*/
                finish();
            }
        });


        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = uN.getText().toString();

                if (isEmptyField(firstname)) return;
                if (isEmptyField(lastname)) return;
                if (isEmptyField(phoneNum)) return;
                if (isEmptyField(uN)) return;
                if (isEmptyField(pW)) return;
                if (isEmptyField(pW2)) return;
                if (isEmptyField(emailAddress)) return;
                if (isEmptyField(deliveryAddress)) return;
                if (isEmptyField(zipCode)) return;

                if (!usernameCheck(uN)) return;
                if (!passwordCheck(pW)) return;
                if (!passwordCompare(pW,pW2)) return;
                if (!phoneNumCheck(phoneNum)) return;
                if (!zipCodecheck(zipCode)) return;

                HashMap postData = new HashMap();
                postData.put("fName", firstname.getText().toString());
                postData.put("lName", lastname.getText().toString());
                postData.put("pNumber", phoneNum.getText().toString());
                postData.put("username", uN.getText().toString());
                postData.put("password", pW.getText().toString());
                postData.put("email", emailAddress.getText().toString());
                postData.put("address", deliveryAddress.getText().toString());
                postData.put("zipCode", zipCode.getText().toString());


                PostResponseAsyncTask taskInsert = new PostResponseAsyncTask(SignUpActivity.this, postData, new AsyncResponse() {
                    @Override
                    public void processFinish(String s) {

                        try {
                            ArrayList<Users> userDetails = new JsonConverter<Users>().toArrayList(s, Users.class);
                            Users users = userDetails.get(0);
                            Log.d(LOG, s);

                            session.createLoginSession(users);

                            Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                            Intent i = getIntent();
                            String activity = i.getStringExtra("from");
                            if(activity.equals("MainActivity")){
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                            }
                            else if(activity.equals("CartActivity")) {
                                Intent intent = new Intent();
                                intent.putExtra("customer", username);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        }

                        catch (Exception e){
                            //If the username (or password) has under 8 characters, or the phone number length is less than 11, then an error box presents itself
                            if (s.contains("failed")) {
                                AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
                                dialogBox.setMessage("Database connection error. Please try again later.")
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
                            //Username already exist
                            else if (s.contains("username already exists")) {
                                AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
                                dialogBox.setMessage("Username already exists.")
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
                            else if (s.contains("invalid email format")) {
                                AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
                                dialogBox.setMessage("Invalid email format.")
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
                        }
                    }
                });
                taskInsert.execute("https://androidshopping.000webhostapp.com/register_v2.php");
            }
        });
    }

    private boolean isEmptyField(EditText editText){
        boolean result = editText.getText().length() <= 0;
        if (result){
            AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
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

    private boolean usernameCheck(EditText username){
        boolean checkLength = username.getText().length() >= 6;
        boolean checkChar = username.getText().toString().matches("^[a-zA-Z0-9_]*$");
        if (!checkLength || !checkChar){
            AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
            dialogBox.setMessage("Username should be at least 6 characters and should not contain any special characters except underscore(_)")
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
        boolean check = checkLength && checkChar;
        return  check;
    }

    private boolean passwordCheck(EditText password){
        boolean checkLength = password.getText().length() >= 8;
        boolean checkChar = password.getText().toString().matches("^[a-zA-Z0-9]*$");
        if (!checkLength || !checkChar){
            AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
            dialogBox.setMessage("Password should be at least 8 characters containing letters or numbers or both.")
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
        boolean check = checkLength && checkChar;
        return  check;
    }

    private boolean passwordCompare(EditText passwordOne, EditText passwordTwo){
        String password1 = passwordOne.getText().toString();
        String password2 = passwordTwo.getText().toString();
        boolean comparePw = password1.equals(password2);
        if(!comparePw){
            AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
            dialogBox.setMessage("Passwords do not match.")
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
        return comparePw;
    }

    private boolean phoneNumCheck(EditText phoneNumber){
        int length = phoneNumber.getText().length();
        boolean checkLength = length == 11;
        boolean checkChar = phoneNumber.getText().toString().matches("[0-9]+");
        if(!checkLength || !checkChar){
            AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
            dialogBox.setMessage("Invalid phone number format. Phone number should contain 11 digits. Example: 09123456789")
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
        boolean check = checkLength && checkChar;
        return check;
    }

    private boolean zipCodecheck(EditText zipCode){
        int length = zipCode.getText().length();
        boolean checkLength = length == 4;
        boolean checkChar = zipCode.getText().toString().matches("[0-9]+");
        if(!checkLength || !checkChar){
            AlertDialog.Builder dialogBox = new AlertDialog.Builder(SignUpActivity.this);
            dialogBox.setMessage("Invalid zip code format. Zip code should contain 4 digits. Example: 1234")
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
        boolean check = checkLength && checkChar;
        return check;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent h = NavUtils.getParentActivityIntent(this);
                h.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, h);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
