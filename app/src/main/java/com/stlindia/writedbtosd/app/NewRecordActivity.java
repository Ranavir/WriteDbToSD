package com.stlindia.writedbtosd.app;

import android.content.Intent;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.stlindia.writedbtosd.R;
import com.stlindia.writedbtosd.helper.DatabaseHandler;
import com.stlindia.writedbtosd.helper.UserModel;

public class NewRecordActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = NewRecordActivity.class.getSimpleName()+" ";
    Button btnSave;
    EditText etUser,etPass;
    Intent intent ;
    DatabaseHandler db ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);


        initUI();
    }//end of onCreate.
    /**
     * This method initializes the UI elements
     */
    private void initUI() {
        System.out.println(TAG+"Entry---->initUI()");
        etUser = (EditText)findViewById(R.id.etUser);
        etPass = (EditText)findViewById(R.id.etPass);
        btnSave = (Button)findViewById(R.id.btnSave);

        btnSave.setOnClickListener(this);
        db = new DatabaseHandler(this);

        System.out.println(TAG + "Exit---->initUI()");
    }//end initUI

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                System.out.println(TAG + " clicked::Save Record");
                //save record to sqlite database here
                String userName = etUser.getText().toString();
                String password = etPass.getText().toString();
                if (userName.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(), "Fill all details...", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    UserModel model = new UserModel(userName,password);
                    // Inserting Contacts
                    System.out.println(TAG+"Inserting ..");
                    db.addUser(model);

                    etUser.setText("");
                    etPass.setText("");
                    Toast.makeText(getApplicationContext(), "User saved to Database...", Toast.LENGTH_LONG).show(); ;
                    /*intent = new Intent(NewRecordActivity.this, MainActivity.class);
                    startActivity(intent);*/
                }



                break;
            default:
        }
    }//end of onClick
}


/*
Open cmd
Type 'adb shell'
        su
        Press 'Allow' on device
        chmod 777 /data /data/data /data/data/com.application.pacakage /data/data/com.application.pacakage*/
/*
Go to the DDMS view in Eclipse*/
