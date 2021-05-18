package ddwu.mobile.finalproject.ma02_20170971.BreadReview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ddwu.mobile.finalproject.ma02_20170971.R;


public class UpdateActivity extends AppCompatActivity {

    static final String TAG = "UpdateActivity";

    EditText et_name;
    EditText et_position;
    EditText et_contents;
    EditText et_pic;
    RadioButton rb_yes;
    RadioButton rb_no;
    ImageView mImageView;
    Bitmap img;
    String mCurrentPhotoPath;
    PostDBHelper postDBHelper;
    PostDto postDto;

    private static final int REQUEST_TAKE_PHOTO = 200;  // 원본 이미지 사용 요청
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        et_name = findViewById(R.id.et_up_name);
        et_position = findViewById(R.id.et_up_position);
        rb_yes = findViewById(R.id.rb_up_yes);
        rb_no = findViewById(R.id.rb_up_no);
        et_contents = findViewById(R.id.et_up_contents);
        et_pic = findViewById(R.id.et_up_pic); // 이미지 파일 경로
        mImageView = findViewById(R.id.image_update);

        postDBHelper = new PostDBHelper(this);

        // 인텐트로 얻어온 dto
        postDto = (PostDto)getIntent().getSerializableExtra("postDto");
        setOriginData(postDto); // 원래 저장된 데이터를 보여준다.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_sns, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_share:
                if (et_name.getText().toString().equals("") || et_position.getText().toString().equals("")
                        || et_contents.getText().toString().equals("")) {
                    Toast.makeText(this, "입력이 안 된 항목이 있습니다!", Toast.LENGTH_SHORT).show();
                    break;
                }
                shareKakao();
                return true;
        }
        return false;
    }

    protected  void onPause() {
        super.onPause();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_up_add:
//   DB 데이터 업데이트 작업 수행  -> 여기서 바로 디비를 수정해주지 말고 데이터 수정 여부를 체크한 후에 result로 넘겨주기
                if(isDataChanged()) {
                    Log.d(TAG, "isDataChanged() : true");

                    Intent intent = new Intent();
                    intent.putExtra("et_name", et_name.getText().toString());
                    intent.putExtra("et_position", et_position.getText().toString());
                    intent.putExtra("et_contents", et_contents.getText().toString());
                    intent.putExtra("et_status", getCheckedType());
                    intent.putExtra("et_img", mCurrentPhotoPath);
                    intent.putExtra("id",  postDto.get_id());
                    setResult(RESULT_OK, intent);
                }else{
                    Log.d(TAG, "isDataChanged() : false");
                    setResult(RESULT_CANCELED);
                }
                finish();
                break;
            case R.id.btn_up_pic:
                dispatchTakePictureIntent();
                break;
            case R.id.btn_up_cancel1:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {  // 고화질 이미지 요청 결과 처리
            setPic();
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

    private void setOriginData(PostDto postDto){
        et_name.setText(postDto.getStore());
        et_position.setText(postDto.getLocation());
        et_contents.setText(postDto.getContents());
        if(postDto.getStatus() == 1){ // 재방문 하겠
            rb_yes.setChecked(true);
        }else{ // 0이면 재방문 안하겠다
            rb_no.setChecked(true);
        }
        mCurrentPhotoPath = postDto.getImg();
        try {
            File imgFile = new File(mCurrentPhotoPath);
            if (imgFile.exists()) {
                img = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                mImageView.setImageBitmap(img);
            }
        }catch (Exception ex) {
            mImageView.setImageResource(R.mipmap.ic_launcher); // 예시 이미지 나중에 바꾸기
        }
    }

    private boolean isDataChanged(){
        if(!postDto.getStore().equals(et_name.getText().toString()) ||
                !postDto.getLocation().equals(et_position.getText().toString()) ||
                !postDto.getContents().equals(et_contents.getText().toString()) ||
                postDto.getStatus() != getCheckedType() ||
                !postDto.getImg().equals(mCurrentPhotoPath)){
            return true;
        }
        return false;
    }

    private int getCheckedType() { // 사용자가 선택을 안하면 no가 디폴트
        if(rb_yes.isChecked()) {
            return 1;
        } else {
            return 0;
        }
    }

    // sns 공유 기능
    public void shareKakao() {
        LocationTemplate params = LocationTemplate.newBuilder(et_position.getText().toString(),
                ContentObject.newBuilder("<빵집 리뷰 공유> : " + et_name.getText().toString(),
                        "http://www.kakaocorp.com/images/logo/og_daumkakao_151001.png",
                        LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .build())
                        .setDescrption("세부내용 : " + et_contents.getText().toString())
                        .build())
                .setAddressTitle("빵집 위치 찾기")
                .build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }
            @Override
            public void onSuccess(KakaoLinkResponse result) { }
        });
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
        Log.i(TAG, "Created file path: " + mCurrentPhotoPath);
        return image;
    }

}
