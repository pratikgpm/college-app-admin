package com.example.mycollege;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class addTeacher extends AppCompatActivity {
    private ImageView addTeacherImage;
    private Bitmap bitmap ;
    private  String category;

    private  final  int REQ = 1;

    private Spinner addTeacherCategory;
     private String download_url ;
    private ProgressDialog pd;
    private  StorageReference storageReference;
    private DatabaseReference reference , dbRef;
    private EditText addTeacherName , addTeacherEmail , addTeacherPost;
    private Button addTeacherBtn;
    private String name , email , post , downloadUrl= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);
        //demo 1

        addTeacherImage = findViewById(R.id.addTeacherImage);
        addTeacherName = findViewById(R.id.addTeacherName);
        addTeacherEmail = findViewById(R.id.addTeacherEmail);
        addTeacherPost = findViewById(R.id.addTeacherPost);
        addTeacherCategory = findViewById(R.id.addTeacherCategory);
        addTeacherBtn =findViewById(R.id.addTeacherBtn);
        pd = new ProgressDialog(this);
        reference = FirebaseDatabase.getInstance().getReference().child("teacher");
        storageReference = FirebaseStorage.getInstance().getReference();
        String[] items = new String[]{"Select Category","Mechanical","Electronic","Computer"};
        addTeacherCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,items));

        addTeacherCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                category  =addTeacherCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addTeacherImage.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        addTeacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });







    }

    private void checkValidation() {

        name = addTeacherName.getText().toString();
        email = addTeacherEmail.getText().toString();
        post = addTeacherPost.getText().toString();

        if (name.isEmpty()){
            addTeacherName.setError("Empty");
            addTeacherName.requestFocus();
        } else if (email.isEmpty()) {
            addTeacherEmail.setError("Empty");
            addTeacherEmail.requestFocus();

        }
        else if (post.isEmpty()) {
            addTeacherPost.setError("Empty");
            addTeacherPost.requestFocus();

        }
        else if (category.equals("Select Category")){
            Toast.makeText(this, "Please provide Teacher Category", Toast.LENGTH_SHORT).show();
        } else if (bitmap == null) {
            pd.setMessage("Uploading.. Ins..");
            pd.show();
            insertData();

        }else {
            pd.setMessage("Uploading...upd.");
            pd.show();
            uploadImage();
        }

    }

    private void uploadImage() {
        pd.setTitle("Uploading.....");
        pd.show();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
        byte[] finalimg = baos.toByteArray();
        final StorageReference filepath ;
        filepath = storageReference.child("teacher").child(finalimg+"jpg");
        final UploadTask uploadTask = filepath.putBytes(finalimg);
        uploadTask.addOnCompleteListener(addTeacher.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    download_url = String.valueOf(uri);
                                    insertData();
                                    Intent intent = new Intent(addTeacher.this,UpdateFaculty.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);


                                }
                            });
                        }
                    });
                }else {
                    pd.dismiss();
                    Toast.makeText(addTeacher.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void insertData() {
        dbRef = reference.child(category);
        final String uniqueKey = dbRef.push().getKey();




        TeacherData teacherData  = new TeacherData(name, email,post,download_url,uniqueKey);
        dbRef.child(uniqueKey).setValue(teacherData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(addTeacher.this, "Teacher Added", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(addTeacher.this, "Something went wrong...", Toast.LENGTH_SHORT).show();

            }
        });


    }


    private void openGallery() {
        Intent pick_image = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pick_image,REQ);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            addTeacherImage.setImageBitmap(bitmap);
        }
    }
}