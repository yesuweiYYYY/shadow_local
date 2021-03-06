package com.example.shadow.Log_register;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shadow.MainActivity;
import com.example.shadow.R;
import com.example.shadow.ShadowApplication;
import com.example.shadow.sql.HTTPUtil;
import com.example.shadow.sql.SQLUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import data_struct.User;
import okhttp3.*;

//EM
import com.example.shadow.EM.*;
import com.hyphenate.exceptions.HyphenateException;

public class Register_activity extends AppCompatActivity implements View.OnClickListener{
    // ????????????
    private String sex="-1";
    private RadioGroup regsex;
    private RadioButton boy;
    private RadioButton girl;

    User myuser;

    private Button ok;
    boolean pass=false;
    private PopupWindow popupWindow;
    private Button choose_albums,take_photo,pop_cancel;

    //????????????????????????
    private int index,width=500,height=650;
    private ImageView head;
    private String image="";

    private static final int PERMISSION_REQUEST = 1001;

    String path = Environment.getExternalStorageDirectory()+"/Android/data/com.example.shadow/files/";
    File mCameraFile = new File(path, "IMAGE_FILE_NAME.jpg");//????????????File??????
    File mCropFile = new File(path, "PHOTO_FILE_NAME.jpg");//????????????File??????
    File mGalleryFile = new File(path, "IMAGE_GALLERY_NAME.jpg");//?????????File??????

    // ????????????
    private static final int IMAGE_REQUEST_CODE = 100;
    private static final int SELECT_PIC_NOUGAT = 101;
    private static final int RESULT_REQUEST_CODE = 102;
    private static final int CAMERA_REQUEST_CODE = 104;



    //?????? ????????????
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            reg();
            Looper.loop();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register);
        head=(ImageView) findViewById(R.id.iv_personal_icon);//??????
        head.setOnClickListener(this);
        ok= (Button) findViewById(R.id.regok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
                if(pass){
                    Log.d("register_ok","????????????");

                    Toast.makeText(Register_activity.this,"????????????",Toast.LENGTH_SHORT).show();

                    Intent intent=new Intent(Register_activity.this,MainActivity.class);
                    intent.putExtra("username",myuser.getUsername());
                    startActivity(intent);
                }else{
                    Log.d("register_fail","????????????");
                    Toast.makeText(Register_activity.this,"????????????",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /**
     * ????????????,
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_REQUEST:
                break;
            default:
                break;
        }
    }


    private void register(){
        // ????????????
        regsex= (RadioGroup) findViewById(R.id.regsex_radiogroup);
        regsex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.regboy:sex="???";
                    case R.id.regirl:sex="???";
                }
            }
        });
        Log.d("reg",sex);

        myuser = new User(
                ( (EditText) findViewById(R.id.regusername_text)      ).getText().toString(),
                ((EditText) findViewById(R.id.regpassword_text)       ).getText().toString(),

                sex,

                ( (EditText) findViewById(R.id.regage_text)           ).getText().toString(),
                (  (EditText) findViewById(R.id.regphone_text)        ).getText().toString(),
                ( (TextView)findViewById(R.id.description)            ).getText().toString(),

                image

        );

        String password0=( (EditText) findViewById(R.id.regpassword0_text) ).getText().toString();
        if(!password0.equals(myuser.getPassword())){
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password0.equals("")){
            Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
            return;
        }




        //????????????

        new Thread(runnable).start();


    }
    private void reg() {
        User _myuser=myuser;
        HTTPUtil _httplog = new HTTPUtil();
        int res = _httplog.register(_myuser);
        Log.d("resgister:",String.valueOf(res));
        if(res == 1){
            pass=true;
//reg_in_EM
            try {
                tool.addClient(myuser.username,myuser.password);
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
            Toast.makeText(Register_activity.this, "????????????",Toast.LENGTH_SHORT).show();

            //1.5??????????????????
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    finish();
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 1500);//1.5????????????TimeTask???run??????
        }else if(res == 2){
            Toast.makeText(Register_activity.this, "????????????,???????????????",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.iv_personal_icon:
                Log.d("register","clickpicture");
                displayPopupWindow();
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    initPopupWindowView(view);
                }
                break;
            case R.id.take_photo:
                Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0?????????
                    Uri uriForFile = FileProvider.getUriForFile(this, "com.example.shadow.fileprovider", mCameraFile);
                    intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                    intentFromCapture.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                    intentFromCapture.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraFile));
                }
                startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);

                break;
            case R.id.choose_albums:

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//??????????????????7.0??????FileProvider
                    Uri uriForFile = FileProvider.getUriForFile
                            (this, "com.example.shadow.fileprovider", mGalleryFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                    intent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, SELECT_PIC_NOUGAT);
                } else {
                    //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mGalleryFile));
                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                }
                break;
            case R.id.pop_cancel:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                break;
            default:
                break;
        }
    }
    //?????????????????????
    public void initPopupWindowView(View parent) {
        Log.d("register","popwindows");
        // // ???????????????????????????pop.xml?????????
        View customView = this.getLayoutInflater().inflate(R.layout.popupwindow_photo_style_layout, null, false);
        // ??????PopupWindow??????,200,150????????????????????????
        popupWindow = new PopupWindow(customView, width, height);
        // ?????????view?????????????????????????????????????????????????????????
        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow= null;
                }
                return false;
            }
        });
        //??????????????????
        take_photo = (Button)customView.findViewById(R.id.take_photo);
        take_photo.setOnClickListener(this);
        take_photo.setFocusable(true);
        choose_albums = (Button)customView.findViewById(R.id.choose_albums);
        choose_albums.setOnClickListener(this);
        pop_cancel = (Button)customView.findViewById(R.id.pop_cancel);
        pop_cancel.setOnClickListener(this);

        //??????popupWindow???????????????
        popupWindow.showAsDropDown(parent);
    }

    //????????????
    public void displayPopupWindow(){
        //???????????????,??????popupWindow???????????????
        if(this.getCurrentFocus()!=null)
        {
            ((InputMethodManager)this.getSystemService(this.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }


    }

    //??????????????????
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {//???????????????
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri inputUri = FileProvider.getUriForFile(this, "com.example.shadow.fileprovider", mCameraFile);//??????FileProvider????????????content?????????Uri

                    startPhotoZoom(inputUri);//??????????????????
                } else {
                    Uri inputUri = Uri.fromFile(mCameraFile);
                    startPhotoZoom(inputUri);
                }
                break;
            }
            case IMAGE_REQUEST_CODE: {//??????<7.0  ???????????????
                if (data != null) {
                    // ????????????????????????
                    Uri uri = data.getData();
                    //crop(uri);
                    startPhotoZoom(uri);
                }
                break;
            }
            case SELECT_PIC_NOUGAT://??????>= 7.0
                File imgUri = new File(GetImagePath.getPath(this, data.getData()));
                Uri dataUri = FileProvider.getUriForFile
                        (this, "com.example.shadow.fileprovider", imgUri);
                // Uri dataUri = getImageContentUri(data.getData());
                startPhotoZoom(dataUri);
                break;
            case RESULT_REQUEST_CODE:{
                Uri inputUri = FileProvider.getUriForFile(this, "com.example.shadow.fileprovider", mCropFile);//??????FileProvider????????????content?????????Uri
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(inputUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //Bitmap bitmap = data.getParcelableExtra("data");
                head.setImageBitmap(bitmap);
                image=imageToBase64(bitmap);

                break;
            }

        }
    }

    /**
     * ????????????????????????
     *
     * @param inputUri
     */
    public void startPhotoZoom(Uri inputUri) {
        if (inputUri == null) {
            Log.e("error","The uri is not exist.");
            return;
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        //sdk>=24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Uri outPutUri = Uri.fromFile(mCropFile);
            intent.setDataAndType(inputUri, "image/*");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);

        } else {
            Uri outPutUri = Uri.fromFile(mCropFile);
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                String url = GetImagePath.getPath(this, inputUri);//?????????????????????4.4?????????????????????Uri???????????????????????????
                intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
            } else {
                intent.setDataAndType(inputUri, "image/*");
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
        }


        // ????????????
        intent.putExtra("crop", "true");
        // aspectX aspectY ??????????????????
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY ?????????????????????
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", false);//??????????????????????????????????????????????????????
        intent.putExtra("outputFormat", "JPEG");
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// ????????????
        startActivityForResult(intent, RESULT_REQUEST_CODE);//?????????????????????????????????Uri?????????
    }
    private String imageToBase64(Bitmap bitmap) {

//????????????????????????bitmap???null

        if (bitmap == null)

            return "????????????";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

//???????????????bitmap????????????????????????????????????png?????????????????????????????????????????????????????????????????????100????????????????????????outputstream???????????????????????????

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        try {

            outputStream.flush();

            outputStream.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

//???????????????outputstream?????????byte??????

        byte[] bytes = outputStream.toByteArray();

//android.util?????????Base64???????????????????????????????????????Base64.DEFAULT??????

        String str = Base64.encodeToString(bytes, Base64.DEFAULT);

//??????????????????????????????

        Log.i("MyLog", "imageToBase64: " + str.length());

        return str;

    }



}
