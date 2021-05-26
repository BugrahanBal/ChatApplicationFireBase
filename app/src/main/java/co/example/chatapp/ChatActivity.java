package co.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    //CHAT AKTİVİTEDE INIŞILAYZ ETTİK CUNKU BURADA İSTİYORUZ.
    //PUSH PUSH VE ON CREATE ALTINDA BASLATTIK MORUK
    private static final String ONESIGNAL_APP_ID = "4921ec18-5b6f-4d75-b78b-b0d25a5336e3";


    private FirebaseAuth mAuth;
    RecyclerView recyclerView;
    Adapter adapter;
    EditText messageText;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private ArrayList<String> chatMessages = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.options_menu_sign_out){

            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
            startActivity(intent);

        } else if (item.getItemId()==R.id.options_menu_profile){

            Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //--INITILAZE OF ONESIGNAL --//

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        //--INITILAZE OF ONESIGNAL --//




        messageText = findViewById(R.id.chat_activity_message_text);
        recyclerView = findViewById(R.id.chat_activity_recyclerView);

        RecyclerView.LayoutManager recyclerViewManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(recyclerViewManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new Adapter(chatMessages);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        //referanslar databaseimizin icerisinde kullanabilecegimiz kendi sınıflarımıza
        //kendi dökümantasyonlara ulasmamıza olanak saglıyor.


        //-- GET USER ID FROM ONE SIGNAL-- //
        OSDeviceState deviceState = OneSignal.getDeviceState();
        String userId = deviceState != null ? deviceState.getUserId() : null;
        //-- GET USER ID FROM ONE SIGNAL-- //

        //--SAVE ONESIGNALS USER ID ON FIREBASDATABASE--//

        // bu user Id ile birbirne mesaj atarken chatte kullanabiliriz.
        // userIdleri firebasee de kaydedebilriim.
        // her kullanıcıyı kaydedecegizz
        UUID uuid = UUID.randomUUID();
        String uuidString2 = uuid.toString();

        DatabaseReference newReference = firebaseDatabase.getReference("PlayerIDs");
        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> playerIDsFromServer = new ArrayList<>();

                for(DataSnapshot ds :snapshot.getChildren()){

                    HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();
                    String currentPlayerID = hashMap.get("playerId");

                    playerIDsFromServer.add(currentPlayerID);
                }
                if(!playerIDsFromServer.contains(userId)){

                    databaseReference.child("PlayerIDs").child(uuidString2).child("playerId").setValue(userId);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //--SAVE ONESIGNALS USER ID ON FIREBASDATABASE--//


        getData();

    }

    public void sendMessage(View view){
        //kullanıcı ne yazdıysa veri tabanına kaydetmek istiyorum
        String messageToSend = messageText.getText().toString();

        //databaseReference.child("Chat").child("Chat1").child("Test Chat").child("Test1").setValue(messageToSend);
        //uniq ıd bir defaya mahsus ıd eger her mesajın ıdsi olmasaydı biz o mesajları üst
        //üste cagırmıs olarak mesajı update edecek ve en son olanı kaydetmiş olacaktık amaa
        // bu şekilde aynı instagram cllone da oldugu gibi her dataya ıd verirriz ki
        // farklı kaydedilsin ve aynı zamanda örneğin mesaj sikayet edildiğinde
        // mesaj fava atıldıgında bu ıdler yardım etsin.// uydurma dizi numara.

        FirebaseUser user = mAuth.getCurrentUser();
        String userEmail=user.getEmail();

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        databaseReference.child("Chats").child(uuidString).child("User Message").setValue(messageToSend);
        databaseReference.child("Chats").child(uuidString).child("User E mail").setValue(userEmail);
        databaseReference.child("Chats").child(uuidString).child("time").setValue(ServerValue.TIMESTAMP);

        /*db.collection("data").document("one")
        .set(docData)
        .addOnSuccessListener(new OnSuccessListener<Void>() {*/

        messageText.setText(""); // kullanıcı mesaj attıktan sonra mesaj bölümü sıfırlandı ki yazsın tekrar

        getData(); // mesaj yollandıktan sonra da yeni bir mesaj eklendiğini görmek istiyoruz.
    }

    public void getData(){
        //reference aslında upload ve get yapılırken iki kere farklı farklı olusturulur.
        //instagram cloneda da her iki durum icin ayrı ayrı reference olusturduk.

        DatabaseReference newReference = firebaseDatabase.getReference("Chats");
        // bir daha neden referans olusturduk şundan dolayı burada
        // sadece Chats baslıgı adı altındakilerle ilgilen demek istedim.
        // addvalue listener da değerleri addchildda su olusturdugumuz childların değiştirilmesini arıyoruz.
        // addvalue en detaylısı bununla childlara da ulasırız..

        Query query = newReference.orderByChild("time");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //datasnapshot verilerin fotografı
                //verileri alırken ne veriyor deneyelim
                //System.out.println("dataSnapShot Children"+snapshot.getChildren());
                //System.out.println("dataSnapShot Values"+snapshot.getValue());
                //System.out.println("dataSnapShot Key"+snapshot.getKey());
                chatMessages.clear(); // devamlı ekliyor 2ser 3 er onun önüne geç

                for (DataSnapshot ds: snapshot.getChildren()){

                    HashMap <String, String> hashMap = (HashMap<String, String>) ds.getValue();

                    String useremail = hashMap.get("User E mail");
                    String usermessage = hashMap.get("User Message");
                    chatMessages.add(useremail + " : " +usermessage);

                    adapter.notifyDataSetChanged(); // yeni bir sey ekledim git orayı güncelle diyorum.

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
            }
        });



    }
}