package com.cakestudios.occupationalhealthandSafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MainPage extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private FirebaseAuth mAuth;
    FirebaseFirestore frStore;
    StorageReference storageReference;
    MenuItem addNew;
    ArrayList<HashMap<String, String>> hatirlatmalist;
    EpostaHatirlatmaDB db;
    String mail, password, chehck = "False", oturumDurum;
    Dialog newPostDialog;
    Uri imgUri;
    SwipeRefreshLayout swipeRefreshLayout;
    EditText editGenelProfil;
    EditText editRiskler;
    EditText editOnlemler;
    RecyclerView recyclerView;
    EditText editMalzemeler;
    EditText editIsAdi;
    ImageView imgIs;
    boolean isCheck = false;
    String isAdi, genelProfil, riskler, onlemler, malzemeler, imgUrlDownload;
    ProgressDialog progressDialog;
    TextView txtBaslik;
    TextView txtAciklama;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imgUri = result.getUri();
                imgIs.setImageURI(imgUri);
                isCheck = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        addNew = menu.findItem(R.id.menü_item_add_new);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menü_item_oturum_kapat) {
            if (chehck.equals("True")) {
                db = new EpostaHatirlatmaDB(getApplicationContext());
                db.hatirlatma(mail, password, chehck, "False");
                db.close();
            }
            mAuth.signOut();
            Toast.makeText(getApplicationContext(), "Oturum Kapatıldı", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
        if (item.getItemId() == R.id.menü_item_add_new) {
            newPostDialog = new Dialog(MainPage.this, R.style.full_screen_dialog);
            newPostDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            newPostDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            newPostDialog.setContentView(R.layout.new_post_dialog);
            editGenelProfil = newPostDialog.findViewById(R.id.edit_text_genel_profil);
            editRiskler = newPostDialog.findViewById(R.id.edit_text_kazalar_hastalıklar);
            editOnlemler = newPostDialog.findViewById(R.id.edit_text_saglik_guvenlik);
            editMalzemeler = newPostDialog.findViewById(R.id.edit_text_malzemeler);
            editIsAdi = newPostDialog.findViewById(R.id.edit_text_is_adi);
            imgIs = newPostDialog.findViewById(R.id.img_is);
            Button btnSave = newPostDialog.findViewById(R.id.btn_save);
            ImageView imgBack = newPostDialog.findViewById(R.id.img_close_dialog_new_post);
            newPostDialog.show();
            frStore = FirebaseFirestore.getInstance();
            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newPostDialog.dismiss();
                }
            });
            imgIs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //resmi kırpıyor ve imagein içine yerleştiriyor
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            Intent i = CropImage.activity()
                                    .setAspectRatio(16, 16)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .getIntent(newPostDialog.getContext());
                            startActivityForResult(i, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    }
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isAdi = editIsAdi.getText().toString();
                    genelProfil = editGenelProfil.getText().toString();
                    riskler = editRiskler.getText().toString();
                    onlemler = editOnlemler.getText().toString();
                    malzemeler = editMalzemeler.getText().toString();
                    if (!TextUtils.isEmpty(isAdi) && !TextUtils.isEmpty(genelProfil) && !TextUtils.isEmpty(riskler) && !TextUtils.isEmpty(onlemler) && !TextUtils.isEmpty(malzemeler)) {
                        if (isCheck) {
                            progressDialog = new ProgressDialog(MainPage.this);
                            progressDialog.setTitle("İşlem Yapılıyor...");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();
                            storageReference = FirebaseStorage.getInstance().getReference();
                            String randomID = UUID.randomUUID().toString();
                            final StorageReference bildirimIMG = storageReference.child("Resimler/").child(randomID + " .jpg");
                            Bitmap bmp = null;
                            try {
                                bmp = MediaStore.Images.Media.getBitmap(newPostDialog.getContext().getContentResolver(), imgUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //resmin boyutunu düşüyor 1/4
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                            byte[] data = baos.toByteArray();
                            bildirimIMG.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    bildirimIMG.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //resmi kaydedip download urslsini alıyoruz
                                            imgUrlDownload = uri.toString();
                                            share();
                                        }
                                    });

                                }
                            });
                        }
                    }
                    else{
                        Toast.makeText(MainPage.this, "Bütün alanları doldurunuz!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_page);
        recyclerView=findViewById(R.id.rc_view);
        swipeRefreshLayout =findViewById(R.id.reflesh);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mAuth = FirebaseAuth.getInstance();
        getState();
        getSupportActionBar().setTitle("İş Kolları ve İş Güvenliği");
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                onStart();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        swipeRefreshLayout.setRefreshing(true);
        final Query query= FirebaseFirestore.getInstance().collection("Iskollari");//Iskolları nı okuyor
        FirestoreRecyclerOptions<Iskollari> firestoreRecyclerOptions= new FirestoreRecyclerOptions
                .Builder<Iskollari>()
                .setQuery(query,Iskollari.class)
                .build();
        FirestoreRecyclerAdapter<Iskollari,IskollariHolder> adapter= new FirestoreRecyclerAdapter<Iskollari, IskollariHolder>(firestoreRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull IskollariHolder holder, final int position, @NonNull final Iskollari model) {
                swipeRefreshLayout.setRefreshing(false);
                holder.btnIskollari.setText(model.getIsAdi());
                holder.btnIskollari.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_NEGATIVE) {
                                    final ProgressDialog progressDialog = new ProgressDialog(MainPage.this);
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setTitle("Dosya Siliniyor...");
                                    progressDialog.show();
                                    String key = getItem(position).getKey();
                                    frStore.collection("Iskollari").document(key).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                progressDialog.dismiss();
                                                Toast.makeText(MainPage.this, "İş kolu Silindi!", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                progressDialog.dismiss();
                                                Toast.makeText(MainPage.this, "Dosya Silinemedi!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
                        builder.setMessage("Seçilen İş verisi silinecektir!").setPositiveButton("İptal", dialogClickListener).setNegativeButton("Sil",dialogClickListener).show();
                        return false;
                    }
                });
                holder.btnIskollari.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String isAdi=getItem(position).getIsAdi();
                        final String genelProfilDialog=getItem(position).getGenelProfil();
                        final String risklerDialog=getItem(position).getRiskler();
                        final String onlemlerDialog=getItem(position).getOnlemler();
                        final String malzemelerDialog=getItem(position).getMalzemeler();
                        String imgUrlDownload=getItem(position).getImgDownloadURL();
                        final Dialog dialog = new Dialog(MainPage.this,R.style.full_screen_dialog);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        dialog.setContentView(R.layout.is_kolu_model_view);
                        ImageView imgBack=dialog.findViewById(R.id.img_close_dialog_detaylar);
                        TextView txtIsAdi=dialog.findViewById(R.id.txt_is_kolu_adi);
                        final ImageView imgIs=dialog.findViewById(R.id.img_is_kolu);
                        Button btnGenelProfil=dialog.findViewById(R.id.btn_view_genel_profil);
                        Button btnRiskler=dialog.findViewById(R.id.btn_view_kazalar_hastalıklar);
                        Button btnOnlemler=dialog.findViewById(R.id.btn_onlemler);
                        Button btnMalzemeler=dialog.findViewById(R.id.btn_malzemeler);
                        final TextView txtYukleniyor=dialog.findViewById(R.id.txt_resim_yükleniyor);
                        dialog.show();
                        imgBack.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        Picasso.get().load(imgUrlDownload).resize(400,400).into(imgIs, new Callback() {
                            @Override
                            public void onSuccess() {
                                txtYukleniyor.setVisibility(View.GONE);
                                imgIs.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                        txtIsAdi.setText(isAdi);
                        btnGenelProfil.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                              textDialog();
                                txtAciklama.setText(genelProfilDialog);
                                txtBaslik.setText("Genel Profil");
                            }
                        });
                        btnMalzemeler.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                textDialog();
                                txtAciklama.setText(malzemelerDialog);
                                txtBaslik.setText("Kullanılan Makinalar ve Tezgahlar");
                            }
                        });
                        btnOnlemler.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                textDialog();
                                txtAciklama.setText(onlemlerDialog);
                                txtBaslik.setText("Sağlık ve Güvenlik Önlemleri");
                            }
                        });
                        btnRiskler.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                textDialog();
                                txtAciklama.setText(risklerDialog);
                                txtBaslik.setText("Kazalar ve Hastalıklar");
                            }
                        });
                    }
                });
            }
            @NonNull
            @Override
            public IskollariHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_page_display, parent, false);
                return new IskollariHolder(view);
            }
            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onRefresh() {
        onStart();
    }

    public static class IskollariHolder extends RecyclerView.ViewHolder {
        Button btnIskollari;
        public IskollariHolder(@NonNull View itemView) {
            super(itemView);
            btnIskollari=itemView.findViewById(R.id.btn_main_page_is_kollari);
        }
    }
    public void getState() {
        db = new EpostaHatirlatmaDB(getApplicationContext());
        hatirlatmalist = db.hatirlatmaList();
        if (hatirlatmalist.size() != 0) {
            for (int i = 0; i < hatirlatmalist.size(); i++) {
                mail = hatirlatmalist.get(i).get("email");
                password = hatirlatmalist.get(i).get("password");
                chehck = hatirlatmalist.get(i).get("checkboxdurum");
                oturumDurum = hatirlatmalist.get(i).get("oturumdurum");
            }
        }
    }
    private void textDialog(){
        final Dialog textDialog= new Dialog(MainPage.this,R.style.full_screen_dialog);
        textDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        textDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        textDialog.setContentView(R.layout.dialog_text_page);
        ImageView imgBack=textDialog.findViewById(R.id.img_close_dialog_text_page);
        txtBaslik=textDialog.findViewById(R.id.txt_baslik_text_page);
        txtAciklama=textDialog.findViewById(R.id.txt_text_page);
        textDialog.show();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textDialog.dismiss();
            }
        });
    }
    private void share() {
        HashMap<String, String> map = new HashMap<>();
        String key= UUID.randomUUID().toString();
        map.put("isAdi", isAdi);
        map.put("genelProfil", genelProfil);
        map.put("onlemler", onlemler);
        map.put("malzemeler", malzemeler);
        map.put("riskler", riskler);
        map.put("imgDownloadURL", imgUrlDownload);
        map.put("key",key);
        frStore.collection("Iskollari").document(key).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Yeni iş kolu eklendi!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    editGenelProfil.setText("");
                    editIsAdi.setText("");
                    editMalzemeler.setText("");
                    editOnlemler.setText("");
                    editRiskler.setText("");
                    imgIs.setImageResource(R.drawable.logo);
                    isCheck=false;
                } else {
                    Toast.makeText(getApplicationContext(), "Yeni iş kolu eklenemedi!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        });

    }


}