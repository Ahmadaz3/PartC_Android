package com.example.partc_client1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private EditText UserNameEditText, PasswordEditText ,Message;
    private Button LoginButton , SendButton;

    private TextView LTV,RTV;
    private Socket clientSocket;

    private DataOutputStream dataOutputStream;
    public static String receivedMessage;

    private Boolean isConnected= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the fields
        UserNameEditText = findViewById(R.id.LoginUser);
        PasswordEditText = findViewById(R.id.LoginPass);
        Message = findViewById(R.id.MessageContant);
        SendButton = findViewById(R.id.SendButton);
        LoginButton = findViewById(R.id.LoginButton);
        LTV = findViewById(R.id.textView4);
        RTV = findViewById(R.id.textView);
        Thread connectClient = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket("192.168.27.176", 1234);
                    //isConnected = true;
                    //Toast.makeText(MainActivity.this, "Connected successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        connectClient.start();
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread verifyUser = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                            String messageToSend = UserNameEditText.getText().toString()+" "+PasswordEditText.getText().toString();
                            dataOutputStream.writeUTF(messageToSend);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                verifyUser.start();
            }
        });

        Thread receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (clientSocket != null && clientSocket.isConnected()) {
                            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                             receivedMessage = dataInputStream.readUTF();
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     Toast.makeText(MainActivity.this, receivedMessage, Toast.LENGTH_SHORT).show();
                                         UserNameEditText.setVisibility(View.GONE);
                                         PasswordEditText.setVisibility(View.GONE);
                                         LoginButton.setVisibility(View.GONE);
                                         LTV.setVisibility(View.GONE);
                                         SendButton.setVisibility(View.VISIBLE);
                                         Message.setVisibility(View.VISIBLE);
                                 }
                             });

                        }

                    }
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        receiveThread.start();
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread sendMessage = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(receivedMessage.equals("1")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                  //  RTV.setVisibility(View.GONE);
                                   // Toast.makeText(MainActivity.this, "YOU reciveed Message from client 2", Toast.LENGTH_SHORT).show();
                                }
                            });
                            try {
                                DataOutputStream dataOutputStream1 = new DataOutputStream(clientSocket.getOutputStream());
                                String messageToSend = Message.getText().toString();
                                dataOutputStream.writeUTF(messageToSend);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }else if(receivedMessage.equals("2")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   // RTV.setVisibility(View.GONE);
                                   // Toast.makeText(MainActivity.this, "You recicved message from client 1", Toast.LENGTH_SHORT).show();
                                }
                            });
                            try {
                                DataOutputStream dataOutputStream1 = new DataOutputStream(clientSocket.getOutputStream());
                                String messageToSend = Message.getText().toString();
                                dataOutputStream.writeUTF(messageToSend);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }

                });
                Thread setMessage = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream st = new DataInputStream(clientSocket.getInputStream());
                            String forwardMessage = st.readUTF();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(receivedMessage.equals("1")){
                                        RTV.setVisibility(View.VISIBLE);
                                        RTV.setText("Client  1 send: "+forwardMessage+"\n");
                                    }else{
                                        RTV.setVisibility(View.VISIBLE);
                                        RTV.setText("Client  2 send: "+forwardMessage+"\n");
                                    }

                                }
                            });
                        }catch (Exception ex){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });
                sendMessage.start();
                setMessage.start();
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}