package ddwu.mobile.finalproject.ma02_20170971.BreadReview;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import ddwu.mobile.finalproject.ma02_20170971.GoogleMapAPI.ViewMapActivity;
import ddwu.mobile.finalproject.ma02_20170971.R;

public class AddActivity extends AppCompatActivity {
    final static String TAG = "AddActivity";
    EditText et_name;
    EditText et_position;
    RadioButton rb_yes;
    RadioButton rb_no;
    EditText et_contents;
    ImageView mImageView;
    String mCurrentPhotoPath;

    PostDBHelper postDBHelper;
    private static final int REQUEST_TAKE_PHOTO = 200;  // 원본 이미지 사용 요청
    private static final int DETAIL_CODE = 500;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        et_name = findViewById(R.id.et_name);
        et_position = findViewById(R.id.et_position);
        rb_yes = findViewById(R.id.rb_yes);
        rb_no = findViewById(R.id.rb_no);
        et_contents = findViewById(R.id.et_contents);
        mImageView = findViewById(R.id.image_add);

        postDBHelper = new PostDBHelper(this);
    }

    protected void onPause() {
        super.onPause();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_add: // DB 데이터 삽입 작업
                SQLiteDatabase db = postDBHelper.getWritableDatabase();
                ContentValues row = new ContentValues();
                row.put(PostDBHelper.COL_STORE, et_name.getText().toString());
                row.put(PostDBHelper.COL_LOCATION, et_position.getText().toString());
                row.put(PostDBHelper.COL_STATUS,  getCheckedType());
                row.put(PostDBHelper.COL_CONTENTS, et_contents.getText().toString());
                row.put(PostDBHelper.COL_IMG, mCurrentPhotoPath); // 이미지 파일 경로를 저장

                long result = db.insert(PostDBHelper.TABLE_NAME, null, row);
                postDBHelper.close();
                String msg = result > 0 ? "게시물 추가 성공" : "게시물 추가 실패!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                finish();
                break;
            case R.id.btn_cancel1: // 디비 삽입 안함
                finish();
                break;
            case R.id.btn_pic:  // 사진 찍기
                dispatchTakePictureIntent();
                break;
            case R.id.btn_search_position: // 빵집 이름 입력하면 해당 주소들 리스트로 보여주기
                if (!et_name.getText().toString().equals("")) {
                    Intent intent = new Intent(this, ViewMapActivity.class);
                    intent.putExtra("store", et_name.getText().toString());
                    startActivityForResult(intent, DETAIL_CODE);

                }else{
                    Toast.makeText(this, "빵집 가게 이름을 입력하세요. ", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {  // 고화질 이미지 요청 결과 처리
            setPic();
        }
        switch (requestCode) {
            case DETAIL_CODE:
                if (resultCode == RESULT_OK) {
                    String location = data.getStringExtra("location");
                    et_position.setText(location);
                }
                break;
        }

    }

    /*이미지뷰에 지정할 수 있는 크기로 이미지를 변경하여 표시*/
    private void setPic() {
        // 이미지뷰의 가로/세로 크기 확인
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // 원본 사진의 이미지 가로/세로 크기 확인
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;    // 메모리에 로딩하진 않고 실제 크기만 확인 시 true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // 원본사진과 이미지뷰의 크기 비율 계산
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        // 지정한 옵션에 따라 비트맵을 메모리에 로딩
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    /*원본 사진 요청*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 요청을 처리할 수 있는 카메라 앱이 있을 경우
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 사진을 저장할 파일 생성
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // 파일을 정상 생성하였을 경우
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ddwu.mobile.finalproject.ma02_20170971.fileprovider",    // 다른 앱에서 내 앱의 파일을 접근하기 위한 권한명 지정
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /*현재 시간을 사용한 파일명으로 앱전용의 외부저장소에 파일 정보 생성*/
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private int getCheckedType() { // 사용자가 선택을 안하면 no가 디폴트
        if(rb_yes.isChecked()) {
            return 1;
        } else {
            return 0;
        }
    }


}
