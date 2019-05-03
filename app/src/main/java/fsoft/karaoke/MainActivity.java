package fsoft.karaoke;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Pattern;

import fsoft.adapter.MusicAdapter;
import fsoft.adapter.SingAdapter;
import fsoft.model.Music;
import fsoft.model.Sing;

public class MainActivity extends AppCompatActivity {

    TabHost tabHost;

    ListView lvMusic, lvFovarite, lvSing;
    ArrayList<Music> arrMusic, arrFovarite;
    MusicAdapter adapterMusic, adapterFovarite;

    ArrayList<Sing> arrSing;
    SingAdapter adapterSing;

    LinearLayout it_search;
    ImageButton btnSearch;
    EditText edtSearch;

    /*ArrayList<Music> dsTimDuoc=new ArrayList<>();
    ArrayList<String> dsMaBH= new ArrayList<>();*/

    public static String DATABASE_NAME = "Arirang.sqlite";
    String DB_PATH_SUFFIX = "/databases/";
    public static SQLiteDatabase database = null;

    public static String API_KEY = "AIzaSyDKcHzqyQ8Id03LNFNo8HXufNj6epNSy3M";
    String ID_PLAYLIST = "PL4q4mZHkyI3tyfFwNTfDtyrhAziDWfW_F";
    String ulrGetJson = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + ID_PLAYLIST + "&key=" + API_KEY + "&maxResults=50";


    //https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=PL4q4mZHkyI3tyfFwNTfDtyrhAziDWfW_F&key=AIzaSyDKcHzqyQ8Id03LNFNo8HXufNj6epNSy3M&maxResults=50
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xuLySaoChepCSDLTuAssetsVaoHeThongMobile();

        addControls();
        addEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_search,menu);


        return super.onCreateOptionsMenu(menu);
    }

    boolean visible;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_search:
                visible =! visible;
                it_search.setVisibility(visible ? View.VISIBLE:View.GONE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_app, menu);
//
//        SearchView searchView = (SearchView) menu.findItem(R.id.menuSearch).getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String st) {
//                Toast.makeText(MainActivity.this, st, Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                Log.d("AAAA", s);
//                return false;
//            }
//        });
//
//        return super.onCreateOptionsMenu(menu);
//    }

    private void GetJsonYouTube(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ArrayList<String> ds = new ArrayList<>();

                try {
                    JSONArray jsonItems = response.getJSONArray("items");
                    String title = "";
                    String url = "";
                    String idVideo = "";

                    for (int i = 0; i < jsonItems.length(); i++) {
                        JSONObject jsonItem = jsonItems.getJSONObject(i);
                        JSONObject jsonSnippet = jsonItem.getJSONObject("snippet");
                        title = jsonSnippet.getString("title");
                        JSONObject jsonThumbnail = jsonSnippet.getJSONObject("thumbnails");
                        JSONObject jsonMedium = jsonThumbnail.getJSONObject("medium");
                        url = jsonMedium.getString("url");
                        JSONObject jsonResourceID = jsonSnippet.getJSONObject("resourceId");
                        idVideo = jsonResourceID.getString("videoId");

                        arrSing.add(new Sing(title, url, idVideo));
                    }

                    adapterSing.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Loi", Toast.LENGTH_LONG).show();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void xuLySaoChepCSDLTuAssetsVaoHeThongMobile() {
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                CopyDataBaseFromAsset();
                Toast.makeText(this, "Sao chep CSDL vao he thong thanh cong", Toast.LENGTH_LONG).show();


            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void CopyDataBaseFromAsset() {
        try {
            InputStream myInput = getAssets().open(DATABASE_NAME);
            String outFileName = layDuongDanLuuTru();
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!f.exists()) {
                f.mkdir();
            }

            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();


        } catch (Exception e) {
            Log.e("Loi_SaoChep", e.toString());

        }
    }

    private String layDuongDanLuuTru() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }


    private void addEvents() {
        xuLyBaiHatGoc();
        xuLySing();

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabID) {
                if (tabID.equalsIgnoreCase("t1")) {
                    xuLyBaiHatGoc();
                } else if (tabID.equalsIgnoreCase("t2")) {
                    //xuLyBaiHatKhongThich();
                    xuLyBaiHatYeuThich();
                } else if (tabID.equalsIgnoreCase("t3")) {
                    GetJsonYouTube(ulrGetJson);

                }
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xuLyTimBaiHatTheoTen(edtSearch.getText().toString());
            }
        });
    }

    /*private void xuLyTimRealTime(String text) {
        String textFortmat=xuLyDataNhapVao(text.toUpperCase());
        xuLyTimRealTime(textFortmat);
        for(String s : dsMaBH){
            timBaiHat(s);
        }
    }*/


    private void xuLySing() {
        lvSing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,PlayVideoActivity.class);
                intent.putExtra("idVideoSing", arrSing.get(i).getIdVideo());
                startActivity(intent);
            }
        });
    }

    private void xuLyBaiHatGoc() {
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query("ArirangSongList", null, null, null, null, null, null);
        arrMusic.clear();
        while (cursor.moveToNext()) {
            String mabh = cursor.getString(0);
            String tenbh = cursor.getString(1);
            String casi = cursor.getString(3);
            int yeuthich = cursor.getInt(5);

            Music music = new Music();
            music.setMa(mabh);
            music.setTen(tenbh);
            music.setCaSi(casi);
            music.setThich(yeuthich == 1);
            arrMusic.add(music);
        }

        cursor.close();
        adapterMusic.notifyDataSetChanged();
    }

    private void xuLyBaiHatYeuThich() {
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query("ArirangSongList", null, "YEUTHICH=?", new String[]{"1"}, null, null, null);
        arrFovarite.clear();
        while (cursor.moveToNext()) {
            String mabh = cursor.getString(0);
            String tenbh = cursor.getString(1);
            String casi = cursor.getString(3);
            int yeuthich = cursor.getInt(5);

            Music music = new Music();
            music.setMa(mabh);
            music.setTen(tenbh);
            music.setCaSi(casi);
            music.setThich(yeuthich == 1);
            arrFovarite.add(music);
        }
        cursor.close();
        adapterFovarite.notifyDataSetChanged();
    }
    public void xuLyTimBaiHatTheoTen(String ten){
        if(ten!=null){
            String input=xuLyDataNhapVao(ten.toUpperCase());
            if(input.endsWith(" "))
                input=input.trim();
            String mabh=timBaiHatTheoTen(input);
            if(mabh==null){
                mabh=timBaiHatTheoTen(xuLyDataNhapVao(ten.toString()));
                timBaiHat(mabh);
            }
            else {
                timBaiHat(mabh);
            }
        }
    }


    private void addControls() {
        setupControls();
        setupTasbHost();
    }


    private void setupControls() {

        lvMusic = (ListView) findViewById(R.id.lvMusic);
        arrMusic = new ArrayList<Music>();
        adapterMusic = new MusicAdapter(MainActivity.this, R.layout.item, arrMusic);
        lvMusic.setAdapter(adapterMusic);

        //lvMusic.setTextFilterEnabled(true);

        lvFovarite = (ListView) findViewById(R.id.lvFavorite);
        arrFovarite = new ArrayList<Music>();
        adapterFovarite = new MusicAdapter(MainActivity.this, R.layout.item, arrFovarite);
        lvFovarite.setAdapter(adapterFovarite);

        lvSing = (ListView) findViewById(R.id.lvSing);
        arrSing = new ArrayList<Sing>();
        adapterSing = new SingAdapter(this, R.layout.item_sing, arrSing);
        lvSing.setAdapter(adapterSing);

        it_search =(LinearLayout) findViewById(R.id.it_search);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        edtSearch = (EditText) findViewById(R.id.edtSearch);

    }

    private void setupTasbHost() {
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("t1");
        tab1.setIndicator("", getDrawable(R.drawable.ic_queue_music_black_24dp));
        tab1.setContent(R.id.tab1);
        tabHost.addTab(tab1);

        TabHost.TabSpec tab2 = tabHost.newTabSpec("t2");
        tab2.setIndicator("", getDrawable(R.drawable.ic_mood_black_24dp));
        tab2.setContent(R.id.tab2);
        tabHost.addTab(tab2);

        TabHost.TabSpec tab3 = tabHost.newTabSpec("t3");
        tab3.setIndicator("", getDrawable(R.drawable.icon_sing));
        tab3.setContent(R.id.tab3);
        tabHost.addTab(tab3);
    }



    //    private void setupData(List<Music> duLieuTuTrenMang) {
//        //-------- Music -----------
//        // Ham xu ly data de hien thi
//        // 1. Lam sao de truyen  response vao arrMusic
//        // 2. Lam sau reset cai adapterMusic sau khi add
//
////        for (int i=0; i<duLieuTuTrenMang.size(); i++) {
////            arrMusic.add(duLieuTuTrenMang.get(i));
////        }
//        // Lay toan bo du lieu o trong phan List<Music>ra ngoai man hinh.
//
//        arrMusic.addAll(duLieuTuTrenMang);
//        adapterMusic.notifyDataSetChanged();
//    }
    //xử lý data nhập vào
    public static String xuLyDataNhapVao(String s){
        String sKhongDau=removeAccent(s);
        return sKhongDau;
    }
    //xóa dấu tiếng việt
    private static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }
    //truy xuất databe tìm bằng mã
    public void timBaiHat(String ma){
        if(ma!=null){
            database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            Cursor cursorTim=database.query("ArirangSongList",null,"MABH=?",new String[]{ma},null,null,null);
            adapterMusic.clear();
            while (cursorTim.moveToNext()){
                String mabh = cursorTim.getString(0);
                String tenbh = cursorTim.getString(1);
                String casi = cursorTim.getString(3);
                int yeuthich = cursorTim.getInt(5);
                Music music = new Music();
                music.setMa(mabh);
                music.setTen(tenbh);
                music.setCaSi(casi);
                music.setThich(yeuthich == 1);

                arrMusic.add(music);
            }
            cursorTim.close();
            adapterMusic.notifyDataSetChanged();
        }
        else
            Toast.makeText(MainActivity.this,"Không tìm thấy bài hát",Toast.LENGTH_LONG).show();
    }
    public String timBaiHatTheoTen(String ten){
        String mabh=null;
        if(ten.equals("")==false){
            database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            Cursor cursorTimTen=database.query("ArirangSongListKhongDau",null,"TENBH=?",new String[]{ten},null,null,null);
            while (cursorTimTen.moveToNext()){
                mabh = cursorTimTen.getString(0);
                return mabh;
            }
            cursorTimTen.close();
        }

        if(mabh==null){
            mabh=timBaiHatTheoCaSi(ten);
            return mabh;
        }
        else {
            return mabh;
        }
    }
    public String timBaiHatTheoCaSi(String casi){
        String mabh=null;
        if(casi!=null){
            database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            Cursor cursorTimTen=database.query("ArirangSongListKhongDau",null,"CASI=?",new String[]{casi},null,null,null);
            while (cursorTimTen.moveToNext()){
                mabh = cursorTimTen.getString(0);
                return mabh;
            }
            cursorTimTen.close();
        }
        else
            Toast.makeText(MainActivity.this,"Không tìm thấy bài hát",Toast.LENGTH_LONG).show();
        return mabh;
    }
    /*public ArrayList<String> timBaiHatTheoTenRealTime(String ten){
        String mabh=null;
        if(ten.equals("")==false){
            database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            //Cursor cursorTimTen=database.query("ArirangSongListKhongDau",null,"TENBH LIKE ?",new String[]{"'"+ten+"%'"},null,null,null);
            Cursor cursorTimTen=database.rawQuery("SELECT * FROM ArirangSongListKhongDau WHERE TENBH LIKE 'A%'",null);
            while (cursorTimTen.moveToNext()){
                mabh = cursorTimTen.getString(0);
                dsMaBH.add(mabh);
                return dsMaBH;
            }
            cursorTimTen.close();
        }

        if(mabh==null){
            mabh=timBaiHatTheoCaSi(ten);
            return dsMaBH;
        }
        else {
            return dsMaBH;
        }
    }*/
}
