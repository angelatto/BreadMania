package ddwu.mobile.finalproject.ma02_20170971.NaverBlogAPI;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ddwu.mobile.finalproject.ma02_20170971.NetworkManager;
import ddwu.mobile.finalproject.ma02_20170971.R;

public class NaverBlogActivity extends AppCompatActivity {
    public static final String TAG = "NaverBlogActivity";

    EditText storeName; // 빵집 가게 이름 입력한 결과
    ListView listView;
    String apiAddress;
    String query;

    BlogAdapter adapter;
    NetworkManager networkManager;
    NaverBlogXmlParser parser;
    ArrayList<NaverBlogDto> resultList = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloglist);
        networkManager = new NetworkManager(this);

        resultList = new ArrayList();
        storeName = findViewById(R.id.etBsearch);
        apiAddress = getResources().getString(R.string.api_url);
        listView = findViewById(R.id.lvBlog);
        parser = new NaverBlogXmlParser();

        adapter = new BlogAdapter(this, R.layout.listview_blog, resultList);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_store_name:
                if (!networkManager.isOnline()) {
                    Toast.makeText(NaverBlogActivity.this, "네트워크를 사용 설정해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                query = storeName.getText().toString();

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
            progressDlg = ProgressDialog.show(NaverBlogActivity.this, "Wait", "Downloading...");
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
            resultList = parser.parse(result);      // 파싱 수행

            if (resultList.size() == 0) {
                Toast.makeText(NaverBlogActivity.this, "No data!", Toast.LENGTH_SHORT).show();
            } else {

                adapter.setList(resultList);    // Adapter 에 파싱 결과를 담고 있는 ArrayList 를 설정
                adapter.notifyDataSetChanged();
                progressDlg.dismiss();

            }
        }

    }
}