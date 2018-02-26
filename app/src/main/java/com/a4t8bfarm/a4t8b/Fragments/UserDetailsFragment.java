package com.a4t8bfarm.a4t8b.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.R;
import com.a4t8bfarm.a4t8b.SessionManagement;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /*private OnFragmentInteractionListener mListener;*/

    TextView FirstName, LastName, EmailAddress, MobileNumber, DeliveryAddress, ZipCode, IsEmailVerified;
    ImageButton EditEmailAddress, EditMobileNumber, EditDeliveryAddress, EditZipCode, ResendEmail;
    View view;
    SessionManagement sessionManagement;

    public UserDetailsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
/*    public static UserDetailsFragment newInstance(String param1, String param2) {
        UserDetailsFragment fragment = new UserDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

/*    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_details, container, false);


        FirstName = (TextView)view.findViewById(R.id.tvFirstName);
        LastName = (TextView)view.findViewById(R.id.tvLastName);
        EmailAddress = (TextView)view.findViewById(R.id.tvEmailAddress);
        MobileNumber = (TextView)view.findViewById(R.id.tvMobileNumber);
        DeliveryAddress = (TextView)view.findViewById(R.id.tvDeliveryAddress);
        ZipCode = (TextView)view.findViewById(R.id.tvZipCode);
        IsEmailVerified = (TextView)view.findViewById(R.id.isEmailVerified);

        sessionManagement = new SessionManagement(getContext());

        HashMap<String, String> user = sessionManagement.getUserDetails();
        final String customer = user.get(SessionManagement.KEY_NAME);
        String firstName = user.get(SessionManagement.KEY_FNAME);
        String lastName = user.get(SessionManagement.KEY_LNAME);
        final String emailAddress = user.get(SessionManagement.KEY_EMAIL);
        final String mobileNumber = user.get(SessionManagement.KEY_PHONENUMBER);
        final String deliveryAddress = user.get(SessionManagement.KEY_ADDRESS);
        final String zipCode = user.get(SessionManagement.KEY_ZIPCODE);
        String emailStatus = user.get(SessionManagement.KEY_VERIFIED);



        /*Toast.makeText(UserDetailsFragment.this.getActivity(), emailStatus, Toast.LENGTH_SHORT).show();*/

        FirstName.setText(firstName);
        LastName.setText(lastName);
        EmailAddress.setText(emailAddress);
        MobileNumber.setText(mobileNumber);
        DeliveryAddress.setText(deliveryAddress);
        ZipCode.setText(zipCode);

        ResendEmail = (ImageButton)view.findViewById(R.id.resendEmail);
        if (emailStatus.equals("0")){
            checkEmailStatus();
            final AlertDialog.Builder reminder = new AlertDialog.Builder(UserDetailsFragment.this.getActivity());
            reminder.setTitle("A gentle reminder from 4T8B");
            reminder.setMessage("Please verify your email to receive and gain access to our promos. If you have not received" +
                    " any email confirmation from us please click the mail button beside your email. If you have confirmed your email recently," +
                    " kindly click again on Account Settings in the main menu to refresh.");
            reminder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            reminder.show();
        }
        else if (emailStatus.equals("1")){
            IsEmailVerified.setText("Email Verified");
            IsEmailVerified.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBright));
            ResendEmail.setVisibility(View.INVISIBLE);
        }
        ResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(UserDetailsFragment.this.getActivity());
                dialog.setTitle("Resend Email Confirmation");
                dialog.setMessage("We will be sending an email confirmation again to you. Please click OK if you want to proceed.");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap postData = new HashMap();
                        postData.put("username", customer);
                        PostResponseAsyncTask resendEmail = new PostResponseAsyncTask(UserDetailsFragment.this.getActivity(), postData, new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                if(s.contains("Message has been sent")){
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Email sent successfully", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Email sending failed. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        resendEmail.execute("https://androidshopping.000webhostapp.com/resend_email.php");
                    }
                });
                dialog.show();
            }
        });

        EditEmailAddress = (ImageButton)view.findViewById(R.id.editEmailAddress);
        EditEmailAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = "https://androidshopping.000webhostapp.com/update_email.php";
                final String userDetailName = "Email Address";
                OpenEditDialogBox(userDetailName, emailAddress, new NewDetail() {
                    @Override
                    public void returnNewDetail(final String newDetail) {
                        HashMap postData = new HashMap();
                        postData.put("txtCustomer", customer);
                        postData.put("txtEmail", newDetail);
                        PostResponseAsyncTask taskUpdate = new PostResponseAsyncTask(UserDetailsFragment.this.getActivity(), postData, new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                if(s.contains("success")){
                                    sessionManagement.editUserDetail(userDetailName, newDetail);
                                    IsEmailVerified.setText("Email not yet verified");
                                    IsEmailVerified.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                                    sessionManagement.editUserDetail("Email Verification", "0");
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.detach(UserDetailsFragment.this).attach(UserDetailsFragment.this).commit();
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Email update successful", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Email update fail. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        taskUpdate.execute(url);
                    }
                });
            }
        });

        EditMobileNumber = (ImageButton)view.findViewById(R.id.editMobileNumber);
        EditMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = "https://androidshopping.000webhostapp.com/update_phone_number.php";
                final String userDetailName = "Mobile Number";
                OpenEditDialogBox(userDetailName, mobileNumber, new NewDetail() {
                    @Override
                    public void returnNewDetail(final String newDetail) {
                        HashMap postData = new HashMap();
                        postData.put("txtCustomer", customer);
                        postData.put("txtPhoneNumber", newDetail);
                        PostResponseAsyncTask taskUpdate = new PostResponseAsyncTask(UserDetailsFragment.this.getActivity(), postData, new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                if(s.contains("success")){
                                    sessionManagement.editUserDetail(userDetailName, newDetail);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.detach(UserDetailsFragment.this).attach(UserDetailsFragment.this).commit();
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Mobile number update successful", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Mobile number update fail. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        taskUpdate.execute(url);
                    }
                });
            }
        });

        EditDeliveryAddress = (ImageButton)view.findViewById(R.id.editDeliveryAddress);
        EditDeliveryAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = "https://androidshopping.000webhostapp.com/update_delivery_address.php";
                final String userDetailName = "Delivery Address";
                OpenEditDialogBox(userDetailName, deliveryAddress, new NewDetail() {
                    @Override
                    public void returnNewDetail(final String newDetail) {
                        HashMap postData = new HashMap();
                        postData.put("txtCustomer", customer);
                        postData.put("txtDeliveryAddress", newDetail);
                        PostResponseAsyncTask taskUpdate = new PostResponseAsyncTask(UserDetailsFragment.this.getActivity(), postData, new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                if(s.contains("success")){
                                    sessionManagement.editUserDetail(userDetailName, newDetail);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.detach(UserDetailsFragment.this).attach(UserDetailsFragment.this).commit();
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Delivery address update successful", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Delivery address update fail. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        taskUpdate.execute(url);
                    }
                });
            }
        });

        EditZipCode = (ImageButton)view.findViewById(R.id.editZipCode);
        EditZipCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = "https://androidshopping.000webhostapp.com/update_zip_code.php";
                final String userDetailName = "Zip Code";
                OpenEditDialogBox(userDetailName, zipCode, new NewDetail() {
                    @Override
                    public void returnNewDetail(final String newDetail) {
                        HashMap postData = new HashMap();
                        postData.put("txtCustomer", customer);
                        postData.put("txtZipCode", newDetail);
                        PostResponseAsyncTask taskUpdate = new PostResponseAsyncTask(UserDetailsFragment.this.getActivity(), postData, new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                if(s.contains("success")){
                                    sessionManagement.editUserDetail(userDetailName, newDetail);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.detach(UserDetailsFragment.this).attach(UserDetailsFragment.this).commit();
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Postal/Zip code update successful", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "Postal/Zip code update fail. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        taskUpdate.execute(url);
                    }
                });
            }
        });


        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden){
        super.onHiddenChanged(hidden);
        HashMap <String,String> user = sessionManagement.getUserDetails();
        String isVerified = user.get(SessionManagement.KEY_VERIFIED);
        if(!hidden){
            if (isVerified.equals("0")){
                checkEmailStatus();}
            else {
                ResendEmail.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        HashMap <String,String> user = sessionManagement.getUserDetails();
        String isVerified = user.get(SessionManagement.KEY_VERIFIED);
        if(this.isVisible()){
            if (isVerified.equals("0")){
                checkEmailStatus();}
            else {
                ResendEmail.setVisibility(View.INVISIBLE);
            }
        }
    }

    interface  NewDetail{
        void returnNewDetail (String newDetail);
    }

    private void OpenEditDialogBox(final String userDetailName, final String userCurrentDetail, final NewDetail newDetail){
        LinearLayout linearLayout = new LinearLayout(UserDetailsFragment.this.getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setPadding(50,0,50,0);
        final EditText input = new EditText(UserDetailsFragment.this.getActivity());

        switch(userDetailName){
            case ("Email Address"):
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                input.setHint("e.g. test@email.com");
                break;
            case ("Mobile Number"):
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setHint("e.g. 09123456789");
                break;
            case ("Delivery Address"):
                input.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                input.setHint("e.g. Pasong Kalabaw, Pulo ni Sarah Pantihan IV Maragondon Cavite");
                break;
            case ("Zip Code"):
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setHint("e.g. 4112");
                break;
        }

        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setCursorVisible(true);
        input.setSingleLine(true);

        linearLayout.addView(input);

        final AlertDialog.Builder EditDialog = new AlertDialog.Builder(UserDetailsFragment.this.getActivity());
        EditDialog.setTitle("Edit " + userDetailName);
        EditDialog.setMessage("Your current " + userDetailName + " is " + userCurrentDetail);
        EditDialog.setView(linearLayout);
        EditDialog.setCancelable(true);
        EditDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog dialog = EditDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUserDetail = input.getText().toString();
                if (!newUserDetail.equals("")){
                    switch (userDetailName) {
                        case "Email Address":
                            if (isEmailValid(newUserDetail)) {
                                if(!newUserDetail.equals(userCurrentDetail)){
                                    newDetail.returnNewDetail(newUserDetail);
                                    dialog.dismiss();
                                }
                                else{
                                    Animation shake = AnimationUtils.loadAnimation(UserDetailsFragment.this.getActivity(), R.anim.shake);
                                    input.startAnimation(shake);
                                    Toast.makeText(UserDetailsFragment.this.getActivity(), "You have entered the same email as your current one.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Animation shake = AnimationUtils.loadAnimation(UserDetailsFragment.this.getActivity(), R.anim.shake);
                                input.startAnimation(shake);
                                Toast.makeText(UserDetailsFragment.this.getActivity(), "You entered an invalid email address", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "Mobile Number":
                            if (isPhoneValid(newUserDetail)) {
                                newDetail.returnNewDetail(newUserDetail);
                                dialog.dismiss();
                            } else {
                                Animation shake = AnimationUtils.loadAnimation(UserDetailsFragment.this.getActivity(), R.anim.shake);
                                input.startAnimation(shake);
                                Toast.makeText(UserDetailsFragment.this.getActivity(), "You entered an invalid mobile number", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "Delivery Address":
                            newDetail.returnNewDetail(newUserDetail);
                            dialog.dismiss();
                            break;
                        case "Zip Code":
                            if (isZipCodeValid(newUserDetail)) {
                                newDetail.returnNewDetail(newUserDetail);
                                dialog.dismiss();
                            } else {
                                Animation shake = AnimationUtils.loadAnimation(UserDetailsFragment.this.getActivity(), R.anim.shake);
                                input.startAnimation(shake);
                                Toast.makeText(UserDetailsFragment.this.getActivity(), "You entered an invalid postal/zip code.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
                else {
                    dialog.dismiss();
                }
            }
        });
    }

    boolean isEmailValid(CharSequence email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    boolean isPhoneValid(CharSequence phoneNumber){
        int length = phoneNumber.length();
        boolean checkLength = length == 11;
        boolean checkChar = phoneNumber.toString().matches("[0-9]+");
        if (checkLength && checkChar){
            return  true;
        }
        else {
            return false;
        }
    }

    boolean isZipCodeValid(CharSequence zipCode){
        int length = zipCode.length();
        boolean checkLength = length == 4;
        boolean checkChar = zipCode.toString().matches("[0-9]+");
        if (checkLength && checkChar){
            return  true;
        }
        else {
            return false;
        }
    }

    public void checkEmailStatus(){
        HashMap<String, String> user = sessionManagement.getUserDetails();
        final String customer = user.get(SessionManagement.KEY_NAME);
        IsEmailVerified = (TextView)view.findViewById(R.id.isEmailVerified);
        HashMap postData = new HashMap();
        postData.put("txtCustomer",customer);
        PostResponseAsyncTask isEmailVerified = new PostResponseAsyncTask(UserDetailsFragment.this.getActivity(), postData, new AsyncResponse() {
            String userDetailName = "Email Verification";
            @Override
            public void processFinish(String s) {
                if(s.equals("1")){
                    IsEmailVerified.setText("Email verified");
                    IsEmailVerified.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBright));
                    sessionManagement.editUserDetail(userDetailName, s);
                    ResendEmail.setVisibility(View.INVISIBLE);
                }
                else if (s.equals("0")){
                    IsEmailVerified.setText("Email not yet verified");
                    IsEmailVerified.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    sessionManagement.editUserDetail(userDetailName, s);
                }
                else {
                    IsEmailVerified.setText("Error");
                }
            }
        });
        isEmailVerified.execute("https://androidshopping.000webhostapp.com/is_active.php");
    }



/*    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    *//**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *//*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
