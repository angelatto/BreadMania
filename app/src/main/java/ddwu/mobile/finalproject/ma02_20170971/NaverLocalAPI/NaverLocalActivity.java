package ddwu.mobile.finalproject.ma02_20170971.NaverLocalAPI;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ddwu.mobile.finalproject.ma02_20170971.NetworkManager;
import ddwu.mobile.finalproject.ma02_20170971.R;

public class NaverLocalActivity extends AppCompatActivity {
    public static final String TAG = "NaverLocalActivity";

    EditText et_search;
    ListView lvList;
    String apiAddress;
    String query;
    LocalAdapter adapter;
    ArrayList<NaverLocalDto> resultList = null;
    NaverLocalXmlParser parser;
    NetworkManager networkManager;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locallist);
        networkManager = new NetworkManager(this);

        et_search = findViewById(R.id.etLsearch);
        lvList = findViewById(R.id.lvLocal);

        resultList = new ArrayList();
        adapter = new LocalAdapter(this, R.layout.listview_local, resultList);
        lvList.setAdapter(adapter);

        apiAddress = getResources().getString(R.string.api_url2);
        parser = new NaverLocalXmlParser();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLsearch:
                query = et_search.getText().toString(); //사용자가 입력한 음식점의 정보를 담은 문자열을 쿼리에

                try {
                    new NaverAsyncTask().execute(apiAddress + URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    class NaverAsyncTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(NaverLocalActivity.this, "Wait", "Downloading...");
        }


        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            String result = networkManager.downloadNaverContents(address);
            if (result == null) return "Error!";
            return result;
        }


        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, result);

            resultList = parser.parse(result);      // 파싱 수행

            adapter.setList(resultList);    // Adapter 에 파싱 결과를 담고 있는 ArrayList 를 설정
            adapter.notifyDataSetChanged();

            progressDlg.dismiss();
        }

    }
}
