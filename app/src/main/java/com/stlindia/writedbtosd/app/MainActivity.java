package com.stlindia.writedbtosd.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stlindia.writedbtosd.R;
import com.stlindia.writedbtosd.helper.DatabaseHandler;
import com.stlindia.writedbtosd.helper.URIHelper;
import com.stlindia.writedbtosd.helper.UserModel;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName() + " ";
    Button btnNew, btnBkpSD, btnRestoreSD, btnShow, btnExportPath;
    TextView tvDesc;
    Intent intent;
    String strDBAbsolutePath = "";
    File externalFileDir = null ;
    DatabaseHandler db ;
    static final int CREATE_REQUEST_CODE = 100 ;
    static final int BACKUP_REQUEST_CODE = 101 ;
    static final int RESTORE_REQUEST_CODE = 102 ;
    Uri currentUri = null;
    URIHelper mURIHelper = null ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        testDeviceMemory();
        testExternalStorage();
        if (Build.VERSION.SDK_INT >= 19) {
                testAllExternalStorage();
        }

    }//end onCreate




    /**
     * This method initializes the UI elements
     */
    private void initUI() {
        System.out.println(TAG + "Entry---->initUI()");
        btnNew = (Button) findViewById(R.id.btnNew);
        btnBkpSD = (Button) findViewById(R.id.btnBkpSD);
        btnRestoreSD = (Button) findViewById(R.id.btnRestoreSD);
        btnShow = (Button) findViewById(R.id.btnShow);
        btnExportPath = (Button) findViewById(R.id.btnExportPath);
        tvDesc = (TextView) findViewById(R.id.tvDesc);

        btnNew.setOnClickListener(this);
        btnBkpSD.setOnClickListener(this);
        btnRestoreSD.setOnClickListener(this);
        btnShow.setOnClickListener(this);
        btnExportPath.setOnClickListener(this);
        db = new DatabaseHandler(this);//Instantiate Database handler
        mURIHelper = new URIHelper(this);//Instantiate Shared Preference
        try {
            currentUri = currentUri.parse(mURIHelper.getURI());//get URI from SP
            System.out.println(TAG+"currentUri---->"+currentUri);
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(TAG + "Exit---->initUI()");
    }//end initUI

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNew:

                System.out.println(TAG + " clicked::New Record");

                intent = new Intent(MainActivity.this, NewRecordActivity.class);
                startActivity(intent);

                break;

            case R.id.btnBkpSD:

                System.out.println(TAG + " clicked::btnBkpSD");
                //copy the database to sd card
                //copyDBToSDCard();
                if (Build.VERSION.SDK_INT >= 19) {
                    /*If either uri not exist then do it or if uri exist but permission not available then also do it*/
                    if(null == currentUri || "".equals(currentUri.toString()) || (PackageManager.PERMISSION_DENIED == checkPathPermission(currentUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)) ) {
                        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/*");
                        intent.putExtra(Intent.EXTRA_TITLE, "database_copy.db");

                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivityForResult(intent, BACKUP_REQUEST_CODE);
                    }else{//if uri exist also permission available
                        backupFileContent(currentUri);
                    }
                }else{
                    copyDBToSDCard();
                }
                tvDesc.setText("DB backup to SD card successful...");
                break;
            case R.id.btnRestoreSD:

                System.out.println(TAG + " clicked::btnRestoreSD");
                if (Build.VERSION.SDK_INT >= 19) {
                    /*If either uri not exist then do it or if uri exist but permission not available then also do it*/
                    if(null == currentUri || "".equals(currentUri.toString()) || (PackageManager.PERMISSION_DENIED == checkPathPermission(currentUri,Intent.FLAG_GRANT_READ_URI_PERMISSION)) ) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/*");

                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivityForResult(intent, RESTORE_REQUEST_CODE);
                    }else{//if uri exist also permission available
                        restoreFileContent(currentUri);
                    }
                }else {
                    restoreDB();
                }
                tvDesc.setText("DB restore from SD card sucessful...");
                break;
            case R.id.btnShow:

                System.out.println(TAG + " clicked::Show Records");

                readFromDatabase();

                break;
            case R.id.btnExportPath:

                System.out.println(TAG + " clicked::btnExportPath");
                if (Build.VERSION.SDK_INT >= 19) {
                    //here get the export path and save it
                    intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/*");
                    intent.putExtra(Intent.EXTRA_TITLE, "database_copy.db");

                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivityForResult(intent, CREATE_REQUEST_CODE);
                }else{
                    Toast.makeText(getApplicationContext(),"This function for >=19 api level !!!",Toast.LENGTH_SHORT).show();
                }

                break;
            default:
        }
    }//end of onClick

    private int checkPathPermission(Uri currentUri,final int PERMISSION) {
        // check uri permissions but does not work
        int perm = checkUriPermission(currentUri , null, null,
                Binder.getCallingPid(), Binder.getCallingUid(),
                PERMISSION);
        System.out.println(TAG+"perm---->"+perm);
        if (perm == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(),"Permission denied for the file...", Toast.LENGTH_LONG).show();
        }
        return perm ;
    }//end checkPathPermission

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {



        if (resultCode == Activity.RESULT_OK)
        {
            if(null != resultData ) {
                currentUri = resultData.getData();
                System.out.println(TAG+"currentUri----->"+currentUri);
                System.out.println(TAG+"currentUri Path---->"+currentUri.getPath());

                if (Build.VERSION.SDK_INT >= 19) {
                    this.grantUriPermission(this.getPackageName(), currentUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                    final int takeFlags = intent.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // Check for the freshest data.
                    getContentResolver().takePersistableUriPermission(currentUri, takeFlags);

                }
                //save the uri in shared preference
                mURIHelper.setURI(currentUri.toString());

                if (requestCode == CREATE_REQUEST_CODE) {

                }else if (requestCode == BACKUP_REQUEST_CODE) {
                    backupFileContent(currentUri);
                }else if (requestCode == RESTORE_REQUEST_CODE)
                {
                    restoreFileContent(currentUri);
                }
            }

        }
    }//END OF onActivityResult

    /**
     * This method restores the database backup from the given uri
     * @param uri
     */
    private void restoreFileContent(Uri uri){
        strDBAbsolutePath = this.getDatabasePath("UserManager.db").getAbsolutePath();
        if (null != strDBAbsolutePath && "" != strDBAbsolutePath) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(strDBAbsolutePath));
                InputStream inputStream = getContentResolver().openInputStream(uri);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > 0) {
                    fos.write(buf, 0, bytesRead);
                }

                inputStream.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ;
    }//end of restoreFileContent

    /**
     * This method writes the backup of the database to the given uri
     * @param uri
     */
    private void backupFileContent(Uri uri)
    {
        strDBAbsolutePath = this.getDatabasePath("UserManager.db").getAbsolutePath();
        if (null != strDBAbsolutePath && "" != strDBAbsolutePath) {
            try {
                FileInputStream fin = new FileInputStream(new File(strDBAbsolutePath));
                ParcelFileDescriptor pfd =
                        this.getContentResolver().
                                openFileDescriptor(uri, "w");

                FileOutputStream fileOutputStream =
                        new FileOutputStream(pfd.getFileDescriptor());
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = fin.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, bytesRead);
                }

                fileOutputStream.close();
                pfd.close();
                fin.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//end of writeFileContent
    /**
     * Restores the database from External Storage
     */
    private void restoreDB() {
        System.out.println(TAG + "Entry---->restoreDB()");

        strDBAbsolutePath = this.getDatabasePath("UserManager.db").getAbsolutePath();
        System.out.println(TAG+"strDBAbsolutePath---->"+strDBAbsolutePath);
        String strBkpPath = "" ;

        strBkpPath = Environment.getExternalStorageDirectory()+ File.separator + this.getPackageName() + File.separator + "database" + File.separator +"database_copy.db";
        System.out.println(TAG+"strBkpPath---->"+strBkpPath);

        try {
            FileUtils.copyFile(new File(strBkpPath), new File(strDBAbsolutePath));
        }catch (IOException ie){

            ie.printStackTrace();
        }
        System.out.println(TAG + "Exit---->restoreDB()");
    }//end of restoreDB

    /**
     * This method reads the data from database and displays in the TextView
     */
    private void readFromDatabase() {
        System.out.println(TAG + "Entry---->readFromDatabase()");

        strDBAbsolutePath = this.getDatabasePath("UserManager.db").getAbsolutePath();
        System.out.println(TAG+"strDBAbsolutePath---->"+strDBAbsolutePath);
        if(null != strDBAbsolutePath && ""!=strDBAbsolutePath){
            //read the data from database and display in textview
            List<UserModel> alUsers = db.getAllUsers();
            String data = "" ;
            for (UserModel um:alUsers) {
                data += um.getUname()+" "+um.getPass()+"\n" ;
            }//end for
            tvDesc.setText(data);
        }//end if
        System.out.println(TAG + "Exit---->readFromDatabase()");
    }//end of readFromDatabase

    /**
     * THis metod copies the database file from device memory loc to SD card
     */
    private void copyDBToSDCard() {
        //check if sd card is mounted or not

        //if mounted then copy
        if (isExternalStorageWritable() && isExternalStorageReadable()) {
            System.out.println(TAG + "External storage has both RW rights!!!");
            strDBAbsolutePath = this.getDatabasePath("UserManager.db").getAbsolutePath();
            if (null != strDBAbsolutePath && "" != strDBAbsolutePath) {
                try {
                    System.out.println(TAG + "Database path------>" + strDBAbsolutePath);// /data/data/com.stlindia.writedbtosd/databases/userManager
                    //final String inFileName = "/data/data/<your.app.package>/databases/foo.db";
                    File dbFile = new File(strDBAbsolutePath);
                    String outFileName = "" ;
                    //outFileName = Environment.getExternalStorageDirectory() + "/database_copy.db"; // < 4.4

                    //outFileName = System.getenv("SECONDARY_STORAGE") + "/database_copy.db";// >= 4.4 not working access denined

                    String sdpath = System.getenv("SECONDARY_STORAGE");
                    StringTokenizer st = new StringTokenizer(sdpath,":");
                    if(st.hasMoreTokens()){
                        sdpath = st.nextToken() ;
                        System.out.println(TAG+"sdpath--->"+sdpath);
                    }
                    outFileName = sdpath + File.separator + this.getPackageName() + File.separator + "database" + File.separator + "database_copy.db";




                    System.out.println(TAG + "outFileName----->" + outFileName);
                    try {
                        FileUtils.copyFile(dbFile,new File(outFileName));
                    }catch (IOException ie){
                        ie.printStackTrace();
                        outFileName = Environment.getExternalStorageDirectory() + File.separator + this.getPackageName() + File.separator + "database" + File.separator + "database_copy.db";
                        System.out.println(TAG + "outFileName----->" + outFileName);
                        FileUtils.copyFile(dbFile,new File(outFileName));
                    }
                    //check existence of new file
                    if(new File(outFileName).exists()){
                        System.out.println(TAG+"New backup file created successfully...");
                    }else{
                        System.out.println(TAG+"New backup file can't be created...");
                    }
                } catch (FileNotFoundException fnf) {
                    fnf.printStackTrace();
                }catch ( IOException e){
                    e.printStackTrace();
                }//end try catch
            }//end if
        } else {
            System.out.println("External storage not found");
            Toast.makeText(getApplicationContext(),"External storage not found!!!",Toast.LENGTH_LONG).show();
        }

        //else give message no sd card mounted
    }//end of copyDBToSDCard

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    /******************************************************************************************************************************/
                        // TESTING CODES
    /******************************************************************************************************************************/

    private void testExternalStorage() {
        System.out.println(TAG + "Entry---->testExternalStorage()");
        //String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath() ;		// Getting available external storage device's (priority mounted sd card) path
        //String sdpath = System.getenv("EXTERNAL_STORAGE");    // Getting available removable storage device's path
        String sdpath = System.getenv("SECONDARY_STORAGE");
        System.out.println(TAG + "sdpath------>" + sdpath);

        StringTokenizer st = new StringTokenizer(sdpath,":");
        if(st.hasMoreTokens()){
            sdpath = st.nextToken() ;
            System.out.println(TAG+"sdpath--->"+sdpath);
        }

        File sdcard = new File(sdpath);

        StatFs stat = new StatFs(sdcard.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        //System.out.println(TAG+"External storage blockSize----->"+blockSize);
        //System.out.println(TAG+"External storage availableBlocks----->"+availableBlocks);
        System.out.println(TAG + "External storage details-------->" + Formatter.formatFileSize(this, availableBlocks * blockSize));

        if (isExternalStorageWritable() && isExternalStorageReadable()) {
            System.out.println(TAG + "External storage has both RW rights!!!");

        } else {
            System.out.println("External storage not found");
        }

        System.out.println(TAG + "Exit---->testExternalStorage()");
    }//end of testExternalStorage

    private void testDeviceMemory() {
        System.out.println(TAG + "Entry---->testDeviceMemory()");

        File path1 = Environment.getDataDirectory();//Device memory
        String outFileName = path1.getAbsolutePath();
        System.out.println(TAG + "Internal storage is available with path ===> " + outFileName);
        StatFs stat = new StatFs(path1.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        //System.out.println(TAG + "Device storage blockSize----->" + blockSize);
        //System.out.println(TAG + "Device storage availableBlocks----->" + availableBlocks);
        System.out.println(TAG + "Device storage details-------->" + Formatter.formatFileSize(this, availableBlocks * blockSize));

        System.out.println(TAG + "Exit---->testDeviceMemory()");
    }//end of testDeviceMemory
    private void testAllExternalStorage() {
        System.out.println(TAG + "Entry---->testAllExternalStorage()");
        String strInfo = "";
        //API Level 19
        File[] externalFilesDirs = getExternalFilesDirs(null);
        strInfo += "\ngetExternalFilesDirs(null):\n";
        for (File f : externalFilesDirs) {
            if( null != f && null != f.getAbsolutePath()) {
                strInfo += f.getAbsolutePath() + "\n";
                externalFileDir = f ;//assign extSD
            }else{//assign the internal memory
                externalFileDir = Environment.getExternalStorageDirectory();
                break;
            }
        }

        strInfo += "Assigned path----->"+externalFileDir.getAbsolutePath();
        //API Level 19
        /*File[] externalCacheDirs = getExternalCacheDirs();
        strInfo += "\ngetExternalCacheDirs():\n";
        for(File f : externalCacheDirs){
            strInfo += f.getAbsolutePath() + "\n";
        }*/
        System.out.println(TAG+"strInfo---->"+strInfo);


        //API Level 21
        /*if (Build.VERSION.SDK_INT == 21) {
            File[] externalMediaDirs = getExternalMediaDirs();
            strInfo += "\ngetExternalMediaDirs():\n";
            for (File f : externalMediaDirs) {
                strInfo += f.getAbsolutePath() + "\n";
            }
        }*/

        tvDesc.setText(strInfo);
        System.out.println(TAG + "Exit---->testAllExternalStorage()");
    }//end of testAllExternalStorage



}//end MainActivity












/*

    private void copyDBToSDCard {
        //check if sd card is mounted or not

        //if mounted then copy
        if (isExternalStorageWritable() && isExternalStorageReadable()) {
            System.out.println(TAG + "External storage has both RW rights!!!");
            strDBAbsolutePath = this.getDatabasePath("userManager").getAbsolutePath();
            if (null != strDBAbsolutePath && "" != strDBAbsolutePath) {
                try {
                    System.out.println(TAG + "Database path------>" + strDBAbsolutePath);// /data/data/com.stlindia.writedbtosd/databases/userManager
                    //final String inFileName = "/data/data/<your.app.package>/databases/foo.db";
                    File dbFile = new File(strDBAbsolutePath);
                    FileInputStream fis = new FileInputStream(dbFile);

                    //String outFileName = Environment.getExternalStorageDirectory() + "/database_copy.db"; // < 4.4

                    String outFileName = System.getenv("SECONDARY_STORAGE") + "/database_copy.db";// >= 4.4

                    if(!new File(outFileName).exists()){
                        System.out.println(TAG+"File not exists...");
                        System.out.println(TAG+"Checking write permission to device...");
                        if(new File(System.getenv("SECONDARY_STORAGE")).canWrite()){
                            System.out.println(TAG+"Can write...");
                            new File(outFileName).createNewFile();

                            // Open the empty db as the output stream
                            OutputStream output = new FileOutputStream(outFileName);

                            // Transfer bytes from the inputfile to the outputfile
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }

                            // Close the streams
                            output.flush();
                            output.close();
                        }else{
                            System.out.println(TAG+"Write access denied...");
                            System.out.println(TAG+"Checking read permission for sake...");
                            if(new File(System.getenv("SECONDARY_STORAGE")).canRead()){
                                System.out.println(TAG + "Can read at least...");
                            }else{
                                System.out.println(TAG+"Read access also denied...");
                            }
                        }
                    }

                    fis.close();
                } catch (FileNotFoundException fnf) {
                    fnf.printStackTrace();
                }catch ( IOException e){
                    e.printStackTrace();
                }//end try catch
            }//end if
        } else {
            System.out.println("External storage not found");
            Toast.makeText(getApplicationContext(),"External storage not found!!!",Toast.LENGTH_LONG).show();
        }

        //else give message no sd card mounted
    }//end of copyDBToSDCard

     /**
     * Here, you might use System.getenv("EXTERNAL_STORAGE") to retrieve the primary External Storage directory (e.g. "/storage/sdcard0")
     * and System.getenv("SECONDARY_STORAGE") to retieve the list of all the secondary directories (e.g. "/storage/extSdCard:/storage/UsbDriveA:/storage/UsbDriveB").
     * Remember that, also in this case, you might want to filter the list of secondary directories in order to exclude the USB drives.
     */
/*public void testOtherExternalStorage() {
    System.out.println(TAG + "Entry---->testOtherExternalStorage()");
//        File sdcard = Environment.getExternalStorageDirectory() ;		// Getting available external storage device's (priority mounted sd card) path
    String sdpath = System.getenv("SECONDARY_STORAGE");    // Getting available removable storage device's path
    System.out.println(TAG + "sdpath------>" + sdpath);
    File sdcard = new File(sdpath);

    StatFs stat = new StatFs(sdcard.getPath());
    long blockSize = stat.getBlockSize();
    long availableBlocks = stat.getAvailableBlocks();
    //System.out.println(TAG+"External storage blockSize----->"+blockSize);
    //System.out.println(TAG+"External storage availableBlocks----->"+availableBlocks);
    System.out.println(TAG + "External storage details-------->" + Formatter.formatFileSize(this, availableBlocks * blockSize));

    System.out.println(TAG + "Exit---->testOtherExternalStorage()");

}//end of testOtherExternalStorage
    */
