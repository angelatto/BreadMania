package ddwu.mobile.finalproject.ma02_20170971.GoogleMapAPI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ddwu.mobile.finalproject.ma02_20170971.BreadReview.AddActivity;
import ddwu.mobile.finalproject.ma02_20170971.R;

public class DetailActivity extends AppCompatActivity {

    EditText etName;
    EditText etPhone;
    EditText etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        etName.setText(intent.getStringExtra("name"));
        etPhone.setText(intent.getStringExtra("phone"));
        etAddress.setText(intent.getStringExtra("address"));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                /*  여기서 save 버튼 누르면 AddActivity로 넘어가서
                *    해당하는 가게의 "이름"과 "주소"를 가지고 바로 애드 할 수 있도록 연결하기 !!!
                * */
                Intent intent = new Intent(this, AddActivity.class);
                if (!etAddress.getText().toString().equals("") || etAddress.getText() == null){
                    intent.putExtra("location", "주소 정보가 없음");
                    intent.putExtra("name", etName.getText().toString());
                }else{
                    intent.putExtra("location", etAddress.getText().toString());
                    intent.putExtra("name", etName.getText().toString());
                }
                startActivity(intent);

                break;
            case R.id.btnClose:
                finish();
                break;
        }
    }

}
