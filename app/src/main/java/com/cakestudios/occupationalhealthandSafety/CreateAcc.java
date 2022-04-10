package com.cakestudios.occupationalhealthandSafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CreateAcc extends AppCompatActivity {
    private Button btn_register;
    private EditText txt_name;
    private EditText txt_email;
    private EditText txtpassword, txtpassword2;
    private TextView txt_back;
    private TextView txt_name_control, txt_sifre_control;
    private String mail;
    private String password;
    private ProgressDialog dialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mfirestore;
    private String name;
    private StorageReference storageReference;
    private boolean nameCheck = true, passCheck = true;
    private ArrayList<String> nameControlList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_acc);
        txt_back = findViewById(R.id.txt_back);
        txt_name = findViewById(R.id.txt_Name);
        txt_email = findViewById(R.id.txt_email);
        txt_name_control = findViewById(R.id.txt_user_registaration_name_control);
        txt_sifre_control = findViewById(R.id.txt_sifre_uyusmazlıgı);
        txtpassword = findViewById(R.id.txt_password);
        txtpassword2 = findViewById(R.id.txt_password2);
        btn_register = findViewById(R.id.btn_register_user_register_page);
        mAuth = FirebaseAuth.getInstance();
        mfirestore = FirebaseFirestore.getInstance();
        dialog = new ProgressDialog(CreateAcc.this);//progress dialog
        txt_back.setOnClickListener(new View.OnClickListener() {//geri dön butonu
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
       mfirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        doc.getString("name");
                        nameControlList.add(doc.getString("name"));//bütün isimleri dbden çekiyor.
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        txtpassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //şifrenin kontrolü
                if (txtpassword.getText().toString().equals(txtpassword2.getText().toString())) {
                    txt_sifre_control.setVisibility(View.GONE);
                    btn_register.setEnabled(true);
                    btn_register.setClickable(true);
                    passCheck = true;
                } else {
                    txt_sifre_control.setVisibility(View.VISIBLE);
                    passCheck = false;
                    btn_register.setEnabled(false);
                    btn_register.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        txt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                name = txt_name.getText().toString();
                //isimleri kontrol ediyor
                if (nameControlList != null) {
                    for (String names : nameControlList) {
                        if (name.toLowerCase().equals(names.toLowerCase())) {
                            txt_name_control.setVisibility(View.VISIBLE);
                            nameCheck = false;
                            btn_register.setEnabled(false);
                            btn_register.setClickable(false);
                            break;
                        } else {
                            txt_name_control.setVisibility(View.GONE);
                            nameCheck = true;
                            btn_register.setEnabled(true);
                            btn_register.setClickable(true);
                        }

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passCheck) {
                    mail = txt_email.getText().toString();
                    password = txtpassword.getText().toString();
                    name = txt_name.getText().toString();
                    if (nameCheck) {
                        if (!TextUtils.isEmpty(mail) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(name)) {
                            dialog.setTitle("Kayıt İşlemi");
                            dialog.setMessage("Kaydınız yapılıyor...");
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                            Register(mail, password, name);
                        } else {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_NEGATIVE) {

                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(CreateAcc.this);
                            builder.setMessage("Bilgileri eksizsiz bir şekilde doldurun!").setPositiveButton("Tamam", dialogClickListener).show();

                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Kullanıcı adı kullanılmaktadır!", Toast.LENGTH_SHORT).show();
                    }
                }

            }


        });


    }

    public void Register(String mail, String password, final String name) {
        mAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(CreateAcc.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();//userID
                            //hashmap e kullanıcı bilgileri yazıldı
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("adres", "-");
                            userMap.put("telefon", "-");

                            mfirestore.collection("Users").document(userID).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //kayıt başarılı
                                        dialog.dismiss();
                                        txt_name.setText("");
                                        txt_email.setText("");
                                        txtpassword.setText("");
                                        Toast.makeText(getApplicationContext(), "Kayıt Yapıldı!", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(i);
                                        finish();

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Kayıt Yapılamadı!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });

                        } else {
                            dialog.dismiss();
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_NEGATIVE) {

                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(CreateAcc.this);
                            builder.setMessage("Kayıt Yapılamadı").setPositiveButton("Tamam", dialogClickListener).show();
                        }


                    }
                });
    }

}