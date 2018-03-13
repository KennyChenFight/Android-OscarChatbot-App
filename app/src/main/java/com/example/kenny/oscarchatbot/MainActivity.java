package com.example.kenny.oscarchatbot;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private Button btnSend;
    private EditText editText;
    private TextView textView;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ArrayAdapter<ChatMessage> adapter;
    private ArrayList<String> oscarMovieLinks = new ArrayList<>();
    private String oscarMovieName;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideBar();
        findViews();
        setListView();
        setSendButtonEvent();
        checkNewMovie();
    }
    @Override
    protected  void onResume() {
        super.onResume();
        // 獲取當前時間，並設置在聊天室最上方
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        Date curDate = new Date(System.currentTimeMillis()) ;
        String dateStr = formatter.format(curDate);
        textView.setText(dateStr);
        Logger.d("date:" + dateStr);
    }
    // 隱藏狀態列，並將title改名
    public void hideBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.bar_bg));
        this.setTitle("");
        Logger.d();
    }
    // 找到元件
    public void findViews() {
        listView = findViewById(R.id.list_msg);
        btnSend = findViewById(R.id.btn_chat_send);
        editText = findViewById(R.id.msg_type);
        textView = findViewById(R.id.date);
        Logger.d();
    }
    // 初始化ListView內容
    public void setListView() {
        adapter = new MessageAdapter(this, R.layout.chat_right, chatMessages);
        listView.setAdapter(adapter);
        Logger.d();
    }
    // 處理每一次發送內容事件
    public void setSendButtonEvent() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, "請輸入你想詢問的事情唷~", Toast.LENGTH_SHORT).show();
                    Logger.d("have no input");
                } else {
                    String content = editText.getText().toString();
                    //取得現在時間
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date curDate = new Date(System.currentTimeMillis()) ;
                    String dateStr = formatter.format(curDate);

                    ChatMessage chatMessage = new ChatMessage(content, ChatMessage.Type.PERSON, dateStr);
                    chatMessages.add(chatMessage);
                    adapter.notifyDataSetChanged();
                    editText.setText("");
                    new ChatBotMesTask().execute(content);
                    Logger.d("have input");
                }
            }
        });
    }
    // 檢查目前時間是資料庫爬蟲資料時間的兩個月後，如果是，馬上爬蟲新的資料
    public void checkNewMovie() {
        FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
        DatabaseReference myRef = fireDB.getReference("電影");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isParse = true;
                // 取得今日日期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
                Calendar today = Calendar.getInstance();
                Date curDate = new Date(System.currentTimeMillis());
                today.setTime(curDate);
                Logger.d("check parser date:" + today.getTime().toString());
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ParserMovie parserMovie = ds.getValue(ParserMovie.class);
                    try {
                        Date datebaseDate = sdf.parse(parserMovie.getDate());
                        Calendar searchDatebaseDate = Calendar.getInstance();
                        searchDatebaseDate.setTime(datebaseDate);
                        Logger.d("firebase every parser date:" + searchDatebaseDate.getTime().toString());
                        // 如果找到資料庫的資料日期為當前日期之後，判定不爬蟲
                        if(today.before(searchDatebaseDate)) {
                            isParse = false;
                            Logger.d("decide not to parse");
                            break;
                        }
                    } catch (Exception ex) {
                        Logger.d("datebase date error:" + ex.getMessage());
                    }
                }
                // 執行爬蟲
                if(isParse) {
                    new ParseMovieTask().execute();
                    Logger.d("start parse new movie");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    // 連接dialogflow的接口，並得到機器人回應
    class ChatBotMesTask extends AsyncTask<String, Void, String> {
        // 得到dialogflow傳來的參數值
        private Map<String, String> paraMap;
        // 存取firebase的資料
        private String firebaseResponse = "";
        // 接收 response
        @Override
        protected String doInBackground(String... voids) {
            String response = null;
            try {
                paraMap = new HashMap<>();
                response = GetText(voids[0]);
                Logger.d("successful to get response from dialogflow");
            } catch (UnsupportedEncodingException ex) {
                Logger.d("not successful to get response from dialogflow:" + ex.getMessage());
            }
            return response;
        }
        // 收到response，並根據response跟firebase拿資料
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            // 取得現在時間
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date curDate = new Date(System.currentTimeMillis()) ;
            String dateStr = formatter.format(curDate);
            // 送出response回應
            ChatMessage chatMessageFromAPI = new ChatMessage(response, ChatMessage.Type.ROBOT, dateStr);
            chatMessages.add(chatMessageFromAPI);
            adapter.notifyDataSetChanged();
            // 送出firebase資料並回應
            getFirebaseResponse();
            Logger.d();
        }

        public String GetText(String query) throws UnsupportedEncodingException {
            String text = "";
            BufferedReader reader = null;
            // 送出data
            try {
                URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");
                // 送出 POST Request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestProperty("Authorization", "Bearer "+"6d2f82865180489d86ca2bd411a76bb6");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                // 宣告 JSONObject
                JSONObject jsonParam = new JSONObject();
                JSONArray queryArray = new JSONArray();
                queryArray.put(query);
                jsonParam.put("query", queryArray);
                jsonParam.put("lang", "en");
                jsonParam.put("sessionId", "1234567890");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Logger.d(jsonParam.toString());
                wr.write(jsonParam.toString());
                wr.flush();
                Logger.d(jsonParam.toString());
                // 讀取dialogflow回傳的json格式
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                text = sb.toString();
                // 解析json格式的回應
                JSONObject object1 = new JSONObject(text);
                JSONObject object = object1.getJSONObject("result");
                JSONObject fulfillment;
                JSONObject parameters;
                String speech = null;
                // 取得參數
                parameters = object.getJSONObject("parameters");
                // 將參數的key及value存入map
                Iterator<?> keys = parameters.keys();
                while(keys.hasNext() ) {
                    String key = (String)keys.next();
                    String value = parameters.optString(key);
                    if(!value.isEmpty()) {
                        paraMap.put(key, value);
                    }
                    Logger.d("參數值:\n" + key + ":" + value);
                }
                fulfillment = object.getJSONObject("fulfillment");
                // 取得dialogflow回應使用者的話語
                speech = fulfillment.optString("speech");
                Logger.d("response is " + text);
                return speech;

            } catch (Exception ex) {
                Logger.d("not successful to get dialogflow response" + ex.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (Exception ex) {
                    Logger.d(ex.getMessage());
                }
            }
            return null;
        }
        // 根據參數值拿取firebase相關資料
        public void getFirebaseResponse() {
            if(paraMap.containsKey("Number") && paraMap.containsKey("Oscar") && paraMap.containsKey("OscarModel")) {
                getOscarModelResponse();
                Logger.d("getOscarModelResponse");
            } else if(paraMap.containsKey("Movie_Type") && !paraMap.containsKey("number")) {
                getRecommendMovie();
                Logger.d("getRecommendMovie");
            } else if(paraMap.containsKey("trailer_word") && !oscarMovieLinks.isEmpty()) {
                getRecommendMovieTrailer();
                Logger.d("getRecommendMovieTrailer");
            } else if(paraMap.containsKey("movie_name")) {
                oscarMovieName = paraMap.get("movie_name");
                Logger.d("get the oscar movie name:" + oscarMovieName);
            } else if(paraMap.containsKey("movieinfo")) {
                getOscarMovieInfo();
                Logger.d("getOscarMovieInfo");
            } else if(paraMap.containsKey("movie_word") && paraMap.containsKey("date")) {
                date = paraMap.get("date");
                date = date.replace("[", "");
                date = date.replace("]", "");
                date = date.replace("\"", "");
                Logger.d("get the date:" + date);
            }else if((paraMap.containsKey("Movie_Type") || paraMap.containsKey("Movie_Type1")) && paraMap.containsKey("number")) {
                getNewMovieInfo();
                Logger.d("getNewMovieInfo");
            }
        }
        // 取得詢問奧斯卡獎項的資訊
        public void getOscarModelResponse() {
            String number = paraMap.get("Number");
            number = number.replace("第", "");
            number = number.replace("屆", "");
            FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
            DatabaseReference myRef = fireDB.getReference("奧斯卡獎項");
            Query query = myRef.orderByChild("grade").equalTo(Integer.parseInt(number));
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        OscarModel oscar = ds.getValue(OscarModel.class);
                        switch (paraMap.get("OscarModel")) {
                            case "最佳男主角獎":
                                firebaseResponse = oscar.getActor();
                                break;
                            case "最佳女主角獎":
                                firebaseResponse = oscar.getActress();
                                break;
                            case "最佳改編劇本獎":
                                firebaseResponse = oscar.getAdapted_screenplay();
                                break;
                            case "最佳導演獎":
                                firebaseResponse = oscar.getDirector();
                                break;
                            case "最佳外語片獎":
                                firebaseResponse = oscar.getFoeign_language_film();
                                break;
                            case "最佳原著劇本獎":
                                firebaseResponse = oscar.getOriginal_script();
                                break;
                            case "最佳原創歌曲獎":
                                firebaseResponse = oscar.getOriginal_song() + "\n" + oscar.getSong_link();
                                break;
                            case "最佳男配角獎":
                                firebaseResponse = oscar.getSupporting_actor();
                                break;
                            case "最佳女配角獎":
                                firebaseResponse = oscar.getSupprting_actress();
                                break;
                            case "最佳影片獎":
                                firebaseResponse = oscar.getVideo();
                                break;
                        }
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                    String dateStr = formatter.format(curDate);
                    ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                    chatMessages.add(chatMessageFromFirebase);
                    adapter.notifyDataSetChanged();
                    Logger.d("getOscarModelResponse:" + firebaseResponse);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        // 取得推薦奧斯卡電影的資訊
        public void getRecommendMovie() {
            FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
            DatabaseReference myRef = fireDB.getReference("奧斯卡電影");
            Query query = myRef.orderByChild("score");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<OscarMovie> oscarMovies = new ArrayList<>();
                    ArrayList<String> movieType = new ArrayList<>();
                    // 取得電影類型參數
                    for(Object key : paraMap.keySet()) {
                        if(paraMap.get(key).contains("片")) {
                            String type = paraMap.get(key).replace("片", "");
                            movieType.add(type);
                        }
                    }
                    // 根據使用者想要的電影類型與firebase裡面的電影類型做比對
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        int count = 0;
                        OscarMovie oscarMovie = ds.getValue(OscarMovie.class);
                        for(int i = 0; i < movieType.size(); i++) {
                            if (oscarMovie.getType().contains(movieType.get(i))) {
                                Logger.d(oscarMovie.getType());
                                count++;
                            }
                            if(count == movieType.size()) {
                                oscarMovies.add(oscarMovie);
                            }
                        }
                    }
                    oscarMovieLinks.clear();
                    // 只取出IMDb前三高的電影
                    if(oscarMovies.size() >= 3) {
                        for(int i = oscarMovies.size() - 3; i < oscarMovies.size(); i++) {
                            firebaseResponse += oscarMovies.get(i).getName() + "\n";
                            oscarMovieLinks.add(oscarMovies.get(i).getLink());
                            Logger.d("getRecommendMovieLink:" + oscarMovies.get(i).getLink());
                        }
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                        String dateStr = formatter.format(curDate);
                        ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                        chatMessages.add(chatMessageFromFirebase);
                        adapter.notifyDataSetChanged();
                        Logger.d("getRecommendMovie:" + firebaseResponse);
                    }
                    // 比對結果小於三筆
                    else if(oscarMovies.size() < 3 && oscarMovies.size() > 0) {
                        for(int i = 0; i < oscarMovies.size(); i++) {
                            firebaseResponse += oscarMovies.get(i).getName() + "\n";
                            oscarMovieLinks.add(oscarMovies.get(i).getLink());
                            Logger.d("getRecommendMovieLink:" + oscarMovies.get(i).getLink());
                        }
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                        String dateStr = formatter.format(curDate);
                        ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                        chatMessages.add(chatMessageFromFirebase);
                        adapter.notifyDataSetChanged();
                        Logger.d("getRecommendMovie:" + firebaseResponse);
                    }
                    // 沒有找出符合筆數
                    else if(oscarMovies.size() == 0) {
                        firebaseResponse = "哎呀～看來沒有你想要的這些類型的電影耶！";
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                        String dateStr = formatter.format(curDate);
                        ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                        chatMessages.add(chatMessageFromFirebase);
                        adapter.notifyDataSetChanged();
                        Logger.d("getRecommendMovie:" + firebaseResponse);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        // 取得推薦奧斯卡電影的預告片資訊
        public void getRecommendMovieTrailer() {
            for(int i = 0; i < oscarMovieLinks.size(); i++) {
                firebaseResponse += (i + 1) + "\n" + oscarMovieLinks.get(i) + "\n";
            }
            oscarMovieLinks.clear();
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
            String dateStr = formatter.format(curDate);
            ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
            chatMessages.add(chatMessageFromFirebase);
            adapter.notifyDataSetChanged();
            Logger.d(firebaseResponse);
        }
        // 取得奧斯卡電影的相關資訊
        public void getOscarMovieInfo() {
            FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
            DatabaseReference myRef = fireDB.getReference("奧斯卡電影");
            Query query = myRef.orderByChild("name").equalTo(oscarMovieName);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        OscarMovie oscarMovie = ds.getValue(OscarMovie.class);
                        switch (paraMap.get("movieinfo")) {
                            case "男主角":
                                firebaseResponse = oscarMovie.getActor();
                                break;
                            case "女主角":
                                firebaseResponse = oscarMovie.getActress();
                                break;
                            case "內容":
                                firebaseResponse = oscarMovie.getContent();
                                break;
                            case "導演":
                                firebaseResponse = oscarMovie.getDirector();
                                break;
                            case "第幾屆奧斯卡":
                                firebaseResponse = String.valueOf(oscarMovie.getGrade());
                                break;
                            case "片長":
                                firebaseResponse = oscarMovie.getLength();
                                break;
                            case "預告片網址":
                                firebaseResponse = oscarMovie.getLink();
                                break;
                            case "得獎項目":
                                firebaseResponse = oscarMovie.getPrize();
                                break;
                            case "評分":
                                firebaseResponse = String.valueOf(oscarMovie.getScore());
                                break;
                            case "影片分類":
                                firebaseResponse = oscarMovie.getType();
                                break;
                        }
                    }
                    if(firebaseResponse.equals("X")) {
                        firebaseResponse = "不好意思，這部電影查無此資料唷！";
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                    String dateStr = formatter.format(curDate);
                    ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                    chatMessages.add(chatMessageFromFirebase);
                    adapter.notifyDataSetChanged();
                    Logger.d("getOscarMovieInfo:" + firebaseResponse);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        // 取得最近上映電影的資訊
        public void getNewMovieInfo() {
            FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
            DatabaseReference myRef = fireDB.getReference("電影");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // 取得使用者想要的電影類型參數
                    ArrayList<String> movieType = new ArrayList<>();
                    for(Object key : paraMap.keySet()) {
                        if(paraMap.get(key).contains("片")) {
                            String type = paraMap.get(key).replace("片", "");
                            movieType.add(type);
                        }
                    }
                    ArrayList<ParserMovie> parseMovies = new ArrayList<>();
                    ArrayList<ParserMovie> responseMovies = new ArrayList<>();
                    // 比對使用者想要的日期
                    for (DataSnapshot ds : dataSnapshot.getChildren())  {
                        ParserMovie parserMovie = ds.getValue(ParserMovie.class);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
                        try {
                            Date originalDate = sdf.parse(date);
                            Calendar searchDate = Calendar.getInstance();
                            searchDate.setTime(originalDate);

                            Date datebaseDate = sdf.parse(parserMovie.getDate());
                            Calendar seatchDatebaseDate = Calendar.getInstance();
                            seatchDatebaseDate.setTime(datebaseDate);

                            if(seatchDatebaseDate.after(searchDate)) {
                                parseMovies.add(parserMovie);
                            }
                        }
                        catch (Exception ex) {
                            Logger.d("not get the date:" + ex.getMessage());
                        }
                    }
                    // 比對使用者想要的電影類型
                    for(int i = 0; i < parseMovies.size(); i++) {
                        if (parseMovies.get(i).getType().containsAll(movieType)) {
                            responseMovies.add(parseMovies.get(i));
                        }
                    }
                    // 符合筆數及電影類型
                    if(responseMovies.size() >= Integer.parseInt(paraMap.get("number"))) {
                        for (int i = 0; i < Integer.parseInt(paraMap.get("number")); i++) {
                            firebaseResponse = responseMovies.get(i).getName() +"(" + responseMovies.get(i).getEnName() + ")" + "\n"
                                    + "上映日期:" + responseMovies.get(i).getDate() + "\n" + "內容大綱:" + responseMovies.get(i).getContent() + ":"
                                    + responseMovies.get(i).getLink();
                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                            Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                            String dateStr = formatter.format(curDate);
                            ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                            chatMessages.add(chatMessageFromFirebase);

                        }
                        adapter.notifyDataSetChanged();
                    }
                    // 不符合筆數或電影類型不符合
                    else if(responseMovies.size() < Integer.parseInt(paraMap.get("number"))) {
                        firebaseResponse = "哎呀！目前即將上映的電影找不到剛好符合你的筆數唷！";
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                        Date curDate = new Date(System.currentTimeMillis());
                        String dateStr = formatter.format(curDate);
                        ChatMessage chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                        chatMessages.add(chatMessageFromFirebase);
                        adapter.notifyDataSetChanged();

                        for(int i = 0; i < responseMovies.size(); i++)  {
                            firebaseResponse = responseMovies.get(i).getName() +"(" + responseMovies.get(i).getEnName() + ")" + "\n"
                                    + "上映日期:" + responseMovies.get(i).getDate() + "\n" + "內容大綱:" + responseMovies.get(i).getContent() + ":"
                                    + responseMovies.get(i).getLink();

                            curDate = new Date(System.currentTimeMillis());
                            dateStr = formatter.format(curDate);
                            chatMessageFromFirebase = new ChatMessage(firebaseResponse, ChatMessage.Type.ROBOT, dateStr);
                            chatMessages.add(chatMessageFromFirebase);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    Logger.d("getNewMovieInfo" + firebaseResponse);
                    date = "";
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
