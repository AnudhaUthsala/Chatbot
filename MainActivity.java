//Main Activity.java
package com.example.chatbot;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
public static List<ChatModel> chatModelList = new ArrayList<>();
public static MassageAdapter massageAdapter;
private String username = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText messageInput = findViewById(R.id.send);
        ImageView sendButton = findViewById(R.id.sendbtn);
        RecyclerView recyclerView = findViewById(R.id.chatlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        // Retrieve the intent that started this activity
        Intent intent = getIntent();
        username = intent.getStringExtra("username"); // Use the same key
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = messageInput.getText().toString().trim();
                ChatModel chatModel = new ChatModel();
                chatModel.setUser(username);
                chatModel.setMassage(messageContent);
                chatModelList.add(chatModel);
                massageAdapter = new MassageAdapter(MainActivity.this,chatModelList);
                recyclerView.setAdapter(massageAdapter);
                massageAdapter.notifyDataSetChanged();
                if (!messageContent.isEmpty()) {
                    messageInput.setText("");
                    new OpenAIExample().execute(messageContent);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

class OpenAIExample extends AsyncTask<String, Void, String> {
    private static final String TAG = "OpenAIExample";
    private TextView responseText;
    OpenAIExample() {
    }
    @Override
    protected String doInBackground(String... params) {
        String response = null;
        try {
            URL url = new URL("https://api.llama-api.com/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer LL-N3I5SpXXuxMA6kjdh6ro3sBlQFnL3MPcGcCmauCI4nGCsnNH5W731VEC8nHZ91AY");
            connection.setDoOutput(true);
            JSONObject requestBody = new JSONObject();
            JSONArray messagesArray = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", params[0]);
            messagesArray.put(message);
            requestBody.put("messages", messagesArray);
            requestBody.put("functions", new JSONArray());
            requestBody.put("model", "codellama-7b-instruct");
            requestBody.put("stream", false);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line);
                }
                in.close();
                response = responseBuilder.toString();

            } else {
                Log.e(TAG, "HTTP error code: " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    String content = message.getString("content");
                    ChatModel chatModel = new ChatModel();
                    chatModel.setUser("Llama");
                    chatModel.setMassage(content);
                    MainActivity.chatModelList.add(chatModel);
                    MainActivity.massageAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "\"No answer received. ");
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception parsing JSON: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "\"No answer received. ");
        }
    }
}


//Login Activity.java

package com.example.chatbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private Button goButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        usernameEditText = findViewById(R.id.send);
        goButton = findViewById(R.id.button2);

        // Set the OnClickListener for the GO button
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                if (username.isEmpty()) {
                    // Show a Toast or error if the EditText is empty
                    Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                } else {
                    MainActivity.chatModelList.clear();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", username); // Optional: Pass the username to the next activity
                    startActivity(intent);
                }
            }
        });
    }
}

//chatmodel.java

package com.example.chatbot;

public class ChatModel {
    String user;
    String massage;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }
}


//MassageAdapter.java

package com.example.chatbot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MassageAdapter extends RecyclerView.Adapter<MassageAdapter.ItemViewHolder> {

    private Context context;
    private List<ChatModel> chatModelList;

    public MassageAdapter(Context context, List<ChatModel> postlist) {
        this.context = context;
        this.chatModelList = postlist;
    }

    @NonNull
    @Override
    public MassageAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MassageAdapter.ItemViewHolder holder, int position) {
        ChatModel chatModel = chatModelList.get(position);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.Icon.getLayoutParams();
        LinearLayout.LayoutParams paramsCard = (LinearLayout.LayoutParams) holder.card.getLayoutParams();


        if (chatModel.getUser().equals("Llama")) {
            holder.Icon.setImageResource(R.drawable.google);
            params.gravity = Gravity.START; // Align to the end (right in LTR layouts)
            holder.name.setGravity(Gravity.START);
            paramsCard.gravity = Gravity.START;
        } else if (!chatModel.getUser().equals("Llama")) {
            holder.Icon.setImageResource(R.drawable.account);
            params.gravity = Gravity.END; // Align to the start (left in LTR layouts)
            paramsCard.gravity = Gravity.END;
            holder.name.setGravity(Gravity.END);
        }
        holder.name.setText(chatModel.getUser());
        holder.message.setText(chatModel.getMassage());

    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView message,name;
        private ImageView Icon;
        private CardView card;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            name = itemView.findViewById(R.id.user2);
            Icon = itemView.findViewById(R.id.icon);
            card = itemView.findViewById(R.id.card);
        }
    }
}
