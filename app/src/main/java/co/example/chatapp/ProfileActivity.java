package co.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    EditText ageText;
    ImageView userImageView;
    Uri selectedImage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference; // depolama işlemlerini yapabilmemiz için
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ageText= findViewById(R.id.profile_activity_ageText);
        userImageView= findViewById(R.id.profile_activity_imageView);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        //storage ı ayrı bir obje gibi yaratmadık ancak o sekilde de olur.
        mAuth = FirebaseAuth.getInstance();

        getData();


    }

    public void getData(){

        DatabaseReference newReference = firebaseDatabase.getReference("Profiles");
        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){

                    HashMap <String,String> hashMap = (HashMap<String,String>) ds.getValue();
                    // bu ds.getvalue bize json dosyası olarak valueları veriyordu.

                    String userName = hashMap.get("userEmail");

                    if(userName.matches(mAuth.getCurrentUser().getEmail().toString())){
                        //burada alternatif bol queryileri ögren ve alternatif yapıstır.
                        String userAge = hashMap.get("userAge");
                        String userImageURL= hashMap.get("userImageURL");

                        if (userAge !=null && userImageURL != null ) {

                            Picasso.get().load(userImageURL).into(userImageView);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void button(View view){

        // 1.adım resmi storage a uuıd ile bir yol belirleyip kaydetmek.
        // 2.adım bu kayıt edilen url alıp klasik chat activityde kullanıdıgmız realtime databesede
        // bir adet chat sınıfı değil de profil sınıfı olusturarak dosya açmak.
        // e tabi bu Profile dosyasında da uuıd olacak.

        final UUID uuidImage = UUID.randomUUID();
        String imageName = "images/"+uuidImage+".jpg";

        // veri tabanlarında nasıl ağaç gibi kuruyorduk.
        // ona benzer işlem yapıyorum
        //putfile da uri istedi
        StorageReference newReference = storageReference.child(imageName);
        newReference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener< UploadTask.TaskSnapshot >() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //chatactivity de datasnapshot vardı burada da tasksnapshot var.
                //tasksnapshot ta bu uygulamanın detayları mevcut
                //metadata gibi farklı ve önemli verileri barındırır.
                //upload ettikten sonra ben URL ararım.
                //firebasedeki o urlyi almaya calısırım ki ulasabileyim.
                //nereye upload ettiğimi ögrenmek icin bunu yapıyorum ve tekrar reference olusturacagım
                StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("images/"+uuidImage+".jpg");
                // profile image upload edilirse onu nereye kaydettiğimin stringini cevirecek
                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener< Uri >() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadURL = uri.toString();
                        //kaydedilen yeri url olarak alıyorum.
                        //bu urlyi veritabanına kaydedecegim ki istediğim zaman görebileyim.
                        UUID uuid = UUID.randomUUID();
                        String uuidString = uuid.toString();

                        String userAge = ageText.getText().toString();

                        FirebaseUser user = mAuth.getCurrentUser();
                        String userEmail = user.getEmail().toString();

                        databaseReference.child("Profiles").child(uuidString).child("userImageURL").setValue(downloadURL); // aldıgımız urlyi tek satırda kaydettik
                        databaseReference.child("Profiles").child(uuidString).child("userAge").setValue(userAge);
                        databaseReference.child("Profiles").child(uuidString).child("userEmail").setValue(userEmail);

                        Toast.makeText(getApplicationContext(),"Upload !! ",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                        startActivity(intent);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });

    }

    public void selectPhoto(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if ((grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode==RESULT_OK && data != null){ // bize uri verecek
            selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                userImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}