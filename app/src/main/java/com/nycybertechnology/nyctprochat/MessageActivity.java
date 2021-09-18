package com.nycybertechnology.nyctprochat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nycybertechnology.nyctprochat.Adapter.MessageAdapter;
import com.nycybertechnology.nyctprochat.Model.Chat;
import com.nycybertechnology.nyctprochat.Model.Users;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MessageActivity extends AppCompatActivity {

    TextView username;
    ImageView imageView;

    RecyclerView recyclerViewy;
    EditText msg_editText;
    ImageButton sendBtn;

    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;

    //var to encr added -----------------

    private byte encryptationKey[] = {-25, -118, -1, -120, 80, -111, -49, -75, 56, -12, 81, -65, -45, -69, -26, -39};// numbers generated by a random meth
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;
    private String encryptedstring2=null;


// mostrar
    MessageAdapter messageAdapter;
    List<Chat> mchat;


    // Display the messages

    RecyclerView recyclerView;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //Widgets
        imageView = findViewById(R.id.imageview_profile);
        username  = findViewById(R.id.usernamey);
        sendBtn =findViewById(R.id.btn_send);
        msg_editText =findViewById(R.id.text_send);



        //RecyclerView, muestra el chat en el
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        intent =getIntent();
        String userid =intent.getStringExtra("userid");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // encr part

                try {
                    cipher = Cipher.getInstance("AES");
                    decipher = Cipher.getInstance("AES");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }

                secretKeySpec = new SecretKeySpec(encryptationKey, "AES");



                //-----------------------------------------------------------------------

                Users user = dataSnapshot.getValue(Users.class);
                username.setText(user.getUsername());

                if(user.getImageURL().equals("default"))
                {
                    imageView.setImageResource(R.mipmap.ic_launcher);

                }else{
                    Glide.with( MessageActivity.this)
                            .load(user.getImageURL())
                            .into(imageView);
                }

                //Esto es lo que pasa al meth readMessages lo que hace falta ojojojojkoj primer parametro creo es el mess

                readMessages(fuser.getUid(),userid, user.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = msg_editText.getText().toString(); // string that contains the message plain text original


                if(!msg.equals("")){ //check here que es donde se envia el mensaje

                    // I'm going to check fist if the string is empty or not and them encrypt it

                    sendMessage(fuser.getUid(),userid, AESEncriptionMethod(msg));// send to the database
                    //encryptedstring2 = AESEncriptionMethod(msg);


                }
                else{
                    Toast.makeText( MessageActivity.this, "Please do not send an empty message", Toast.LENGTH_SHORT);
                }
            }
        });


    }
    private void sendMessage(String sender, String receiver, String message){  //Method the enviar este es el que tengo que ver paara editar con sec
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("chats").push().setValue(hashMap); // mete el map en la bd



    }

    private void readMessages(String myid, String userid, String imageurl){ // importante tambien para sec esto es lo que lee los mensajes
            mchat = new ArrayList<>();

            reference = FirebaseDatabase.getInstance().getReference("chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    mchat.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Chat chat = snapshot.getValue(Chat.class);

                        if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){

                            try {
                                chat.setMessage(AESDecriptionMethod(chat.getMessage()));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            mchat.add(chat);

                        }



                        messageAdapter = new MessageAdapter(MessageActivity.this,mchat,imageurl); // mchat es lo que original pasa
                        recyclerView.setAdapter(messageAdapter);


                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private String AESEncriptionMethod(String string ){  // este methodo encrypta

        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];


        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;
        try {
            returnString = new String(encryptedByte, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }


    private String AESDecriptionMethod(String string ) throws UnsupportedEncodingException {

        byte[] EncryptedByte = string.getBytes("ISO-8859-1");
        String decryptedString = string;

        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    }


}