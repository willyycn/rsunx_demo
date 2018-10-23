package com.rsun.andemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.rsun.andemo.Operation.NetOpHelper;
import com.rsun.kit.RSunKit;
import java.util.Map;

import static com.rsun.andemo.Operation.NetOpHelper.ServerParam;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button verifyBTN;
    private Button generateBTN;
    private EditText passwdTF;
    private EditText tokenTF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }
    private String challenge;
    @Override
    protected void onResume() {
        super.onResume();
        challenge = RSunKit.getChallenge();
    }

    private void initUI(){
        verifyBTN = findViewById(R.id._verify);
        generateBTN = findViewById(R.id._generate);
        verifyBTN.setOnClickListener(mClickListener);
        generateBTN.setOnClickListener(mClickListener);
        passwdTF = findViewById(R.id._mainPasswd);
        tokenTF = findViewById(R.id._mainToken);
    }
    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id._verify:
                    try {
                        verify();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id._generate:
                    try {
                        generate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private String tk_sum;
    private String prikey;
    private void generate(){
        makeProgressDialog(MainActivity.this,"获取...");
        String hashpass = RSunKit.hash_passwd(passwdTF.getText().toString(),"my_salt");
        NetOpHelper.getHelper(MainActivity.this).generate(tokenTF.getText().toString(), hashpass, challenge, new NetOpHelper.NetOpCallback() {
            @Override
            public void onResponse(int status, Map<String, String> response) {
                dismissProgressDialog();
                tk_sum = response.get("tk_sum");
                prikey = response.get("prikey");
                challenge = response.get("challenge");
                generateBTN.setClickable(false);
                generateBTN.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            @Override
            public void onFailure(Error error) {

            }
        });
    }
    private void verify(){
        if (!generateBTN.isClickable()){
            String hashpass = RSunKit.hash_passwd(passwdTF.getText().toString(),"my_salt");
            Map<String,String>map = RSunKit.rsunxSignature(tokenTF.getText().toString(),hashpass,challenge,tk_sum,prikey,ServerParam);
            NetOpHelper.getHelper(MainActivity.this).verifySign(map.get("signature"), tokenTF.getText().toString(), hashpass, tk_sum, new NetOpHelper.NetOpCallback() {
                @Override
                public void onResponse(int status, Map<String, String> response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("结果");
                    builder.setTitle(response.get("verify"));
                    builder.setCancelable(true);
                    builder.create().show();
                }

                @Override
                public void onFailure(Error error) {

                }
            });
        }
    }
    private Dialog mDialog;
    private void makeProgressDialog(Context context, String title){
        mDialog = new ProgressDialog(context);
        mDialog.setCancelable(false);
        mDialog.setTitle(title);
        mDialog.show();
    }

    private void dismissProgressDialog(){
        if (mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }
}
