package ddwu.mobile.finalproject.ma02_20170971;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.security.MessageDigest;

import ddwu.mobile.finalproject.ma02_20170971.BreadReview.AddActivity;
import ddwu.mobile.finalproject.ma02_20170971.BreadReview.AllActivity;
import ddwu.mobile.finalproject.ma02_20170971.GoogleMapAPI.ViewMapActivity;
import ddwu.mobile.finalproject.ma02_20170971.NaverBlogAPI.NaverBlogActivity;
import ddwu.mobile.finalproject.ma02_20170971.NaverLocalAPI.NaverLocalActivity;

public class MainActivity extends AppCompatActivity {

    NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        networkManager = new NetworkManager(this);
    }

    public void onClick(View view) {
        Intent intent = null;

        switch (view.getId()) {
            case R.id.btn_all:     //전체 리스트를 보여준다.
                intent = new Intent(this, AllActivity.class);
                break;
            case R.id.btn_create:      //새로 추가한다.
                intent = new Intent(this, AddActivity.class);
                break;
            case R.id.btn_map:      // 지도를 보여준다.
                intent = new Intent(this, ViewMapActivity.class);
                break;
            case R.id.btn_blog:
                if (!networkManager.isOnline()) {
                    Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(this, NaverBlogActivity.class);
                break;
            case R.id.btn_local:
                if (!networkManager.isOnline()) {
                    Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(this, NaverLocalActivity.class);
                break;

        }
        if (intent != null) { startActivity(intent); }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    public void onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mIntent = new Intent(this, MainActivity.class);
                startActivity(mIntent);
                break;
            case R.id.navigation_map:
                Intent vIntent = new Intent(this, ViewMapActivity.class);
                startActivity(vIntent);
                break;

        }
    }

}