package com.example.supreeth.finalprojectwithview;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.Nullable;

public class SecondActivity extends AppCompatActivity {

    Button selectFile,upload,fetch;
    TextView notification;
    Uri pdfUri;
    String fileName1;
    String fileName;

    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);

        Bundle bundle=getIntent().getExtras();
        final String branch=bundle.getString("KEY");


        storage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();

        selectFile=findViewById(R.id.selectFile);
        upload=findViewById(R.id.upload);
        notification=findViewById(R.id.notification);

        fetch=findViewById(R.id.fetchfiles);
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(SecondActivity.this,MyRecyclerViewActivity.class);
                intent.putExtra("BRANCH",branch);
                startActivity(intent);

            }
        });

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(SecondActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectPdf();
                }
                else
                {
                    ActivityCompat.requestPermissions(SecondActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
                }

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(pdfUri!=null)
                {
                    uploadFile(pdfUri,branch);
                }
                else
                {
                    Toast.makeText(SecondActivity.this,"Select a file",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void uploadFile(Uri pdfUri, final String branch) {

        if(fileName1.indexOf(".")>0)
        {
            fileName=fileName1.substring(0,fileName1.lastIndexOf("."));
        }
        else
        {
            fileName=fileName1;
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("uploading file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        StorageReference storageReference=storage.getReference();

        storageReference.child(branch).child(fileName1).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String url=taskSnapshot.getDownloadUrl().toString();
                        DatabaseReference reference=database.getReference();

                        reference.child(branch).child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    Toast.makeText(SecondActivity.this,"File Successfully uploaded",Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(SecondActivity.this,"File not Successfully uploaded",Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(SecondActivity.this,"File not Successfully uploaded",Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentProgress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);

            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectPdf();
        }
        else
        {
            Toast.makeText(SecondActivity.this,"please provide permission...",Toast.LENGTH_SHORT).show();
        }

    }

    private void selectPdf() {

        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,86);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==86 && resultCode==RESULT_OK && data!=null)
        {
            pdfUri=data.getData();
            notification.setText("A file is selected : "+data.getData().getLastPathSegment());
            fileName1=data.getData().getLastPathSegment();
        }
        else
        {
            Toast.makeText(SecondActivity.this,"Please select a file",Toast.LENGTH_SHORT).show();
        }

    }

}
