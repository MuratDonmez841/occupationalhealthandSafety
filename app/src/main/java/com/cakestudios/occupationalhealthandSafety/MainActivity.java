package com.cakestudios.occupationalhealthandSafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Button btn_login;
    TextView btn_register;
    EditText txt_mail;
    EditText txt_password;
    String mail, password;
    ProgressDialog dialog;
    private FirebaseAuth mAuth;
    EpostaHatirlatmaDB db;
    ArrayList<HashMap<String, String>> hatirlatmalist;
    CheckBox hatirlatma;
    String chehck = "False";
    TextView txt_sifre_sifirla, txt_emegi_gecenler;
    ImageView img_close, img_info;
    EditText txt_mail_sifirla;
    Button btn_gonder;
    String mailSifirla;
    Dialog logoDialog;
    FirebaseUser user;
    String oturumDurum = "False";
    FirebaseFirestore frStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        btn_login = findViewById(R.id.btn_Login);
        btn_register = findViewById(R.id.btn_register);
        txt_mail = findViewById(R.id.txt_email);
        txt_password = findViewById(R.id.txt_password);
        hatirlatma = findViewById(R.id.checkbox_hatirla);
        txt_sifre_sifirla = findViewById(R.id.txt_sifre_sifirla);
        mAuth = FirebaseAuth.getInstance();
        frStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        dialog = new ProgressDialog(MainActivity.this);
        setMailPassword();

        if (chehck.equals("True")) {
            hatirlatma.setChecked(true);
        } else {
            hatirlatma.setChecked(false);
        }

        if (oturumDurum.equals("True")) {
            //logoyu getiriyor ve id şifre istemeden programa giriş yaptırıyor.
            logoDialog = new Dialog(MainActivity.this, R.style.full_screen_dialog);
            logoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            logoDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            logoDialog.setContentView(R.layout.animlayout);
            logoDialog.setCancelable(false);
            logoDialog.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAuth.getCurrentUser() != null) {
                        user = mAuth.getCurrentUser();
                        Intent i = new Intent(getApplicationContext(), MainPage.class);
                        //logoDialog.dismiss();
                        startActivity(i);
                        finish();
                    } else {
                        logoDialog.dismiss();
                    }
                }
            }, 3000);


        }

        hatirlatma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hatirlatma.isChecked()) {
                    //database i sıfırlıyor
                    db = new EpostaHatirlatmaDB(getApplicationContext());
                    db.resetHatirlatma();
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mail = txt_mail.getText().toString();
                password = txt_password.getText().toString();
                if (!TextUtils.isEmpty(mail) && !TextUtils.isEmpty(password)) {

                    dialog.setTitle("Oturum Açılıyor");
                    dialog.setMessage("Giriş yapılıyor lütfen bekleyin...");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    Login(mail, password);


                } else {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {

                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Mail veya şifre boş kalamaz!").setPositiveButton("Tamam", dialogClickListener).show();

                }


            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CreateAcc.class);
                startActivity(i);
                finish();
            }
        });
        txt_sifre_sifirla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.sifre_sifirla_layout);
                img_close = findViewById(R.id.img_close);
                txt_mail_sifirla = findViewById(R.id.txt_mail_sifirla);
                btn_gonder = findViewById(R.id.btn_sifre_sifirla_göder);
                img_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
                btn_gonder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mailSifirla = txt_mail_sifirla.getText().toString();
                        if (!mailSifirla.equals("")) {
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            auth.sendPasswordResetEmail(mailSifirla)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Mailinizi kontrol edin!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Mail gönderilemedi!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Mail kısmı boş kalamaz!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }
        });
    }

    public void Login(final String mail, final String password) {
        mAuth.signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener(MainActivity.this
                        , new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    dialog.dismiss();
                                    user = mAuth.getCurrentUser();
                                    if (hatirlatma.isChecked()) {
                                        db = new EpostaHatirlatmaDB(getApplicationContext());
                                        if (hatirlatmalist.size() != 0) {
                                            db.resetHatirlatma();
                                        }
                                        chehck = "True";
                                        db.hatirlatma(mail, password, chehck, "True");
                                        db.close();

                                    } else {
                                        db = new EpostaHatirlatmaDB(getApplicationContext());
                                        db.resetHatirlatma();
                                    }
                                    Toast.makeText(getApplicationContext(), "Giriş yapıldı", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(getApplicationContext(), MainPage.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    dialog.dismiss();
                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == DialogInterface.BUTTON_NEGATIVE) {

                                            }
                                        }
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setMessage("Giriş yapılamadı!\n" + task.getException().getMessage()).setPositiveButton("Tamam", dialogClickListener).show();

                                }


                            }
                        });

    }

    public void setMailPassword() {
        //mail ve passwordu yerleştiriyor
        db = new EpostaHatirlatmaDB(getApplicationContext());
        hatirlatmalist = db.hatirlatmaList();
        if (hatirlatmalist.size() != 0) {
            for (int i = 0; i < hatirlatmalist.size(); i++) {
                txt_mail.setText(hatirlatmalist.get(i).get("email"));
                txt_password.setText(hatirlatmalist.get(i).get("password"));
                chehck = hatirlatmalist.get(i).get("checkboxdurum");
                oturumDurum = hatirlatmalist.get(i).get("oturumdurum");
            }
        }
    }
}