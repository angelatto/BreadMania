package ddwu.mobile.finalproject.ma02_20170971.BreadReview;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

import ddwu.mobile.finalproject.ma02_20170971.R;

public class AllActivity extends AppCompatActivity {

    static final String TAG = "AllActivity";
    ListView list = null;
    PostDBHelper helper;
    Cursor cursor;
    MyCursorAdapter myCursorAdapter;
    EditText editText; // 빵집 이름

    static final int ACT_EDIT = 0;
    int updateDecision = RESULT_OK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all);

        list = (ListView)findViewById(R.id.lvPost);
        editText = (EditText)findViewById(R.id.et_store_name);

        helper = new PostDBHelper(this);
        myCursorAdapter = new MyCursorAdapter(this, R.layout.listview_post, null);
        list.setAdapter(myCursorAdapter);

        //	리스트 뷰 클릭 처리 (수정할 때 상세 페이지 보여주기 )
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //원래는 포지션으로 위치구해서 dto에서 id구해서 연결하는데 커서어댑터는 long id로 바로 pk id와 연결된다.
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(AllActivity.this, UpdateActivity.class);

                PostDto postDto = new PostDto();
                postDto.set_id(id);
                postDto.setStore(cursor.getString(cursor.getColumnIndex(PostDBHelper.COL_STORE)));
                postDto.setLocation(cursor.getString(cursor.getColumnIndex(PostDBHelper.COL_LOCATION)));
                postDto.setStatus(cursor.getInt(cursor.getColumnIndex(PostDBHelper.COL_STATUS)));
                postDto.setContents(cursor.getString(cursor.getColumnIndex(PostDBHelper.COL_CONTENTS)));
                postDto.setImg(cursor.getString(cursor.getColumnIndex(PostDBHelper.COL_IMG)));

                intent.putExtra("postDto", (Serializable)postDto);
                startActivityForResult(intent, ACT_EDIT);
            }
        });

       //	리스트 뷰 롱클릭 처리 (삭제할 떄 대화상자 창으로 확인받고 삭제하기)
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                SQLiteDatabase db = helper.getWritableDatabase();
                cursor = db.rawQuery("SELECT * FROM "
                        + helper.TABLE_NAME + " WHERE _id='" + id + "';", null);

                while (cursor.moveToNext()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AllActivity.this);
                    builder.setTitle("정보 삭제");
                    builder.setMessage(cursor.getString(cursor.getColumnIndex(helper.COL_STORE)) + "의 리뷰를 삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                        DB 삭제 수행
                            SQLiteDatabase db = helper.getWritableDatabase();
                            String whereClause = helper.COL_ID + "=?";
                            String[] whereArgs = new String[]{String.valueOf(id)};
                            db.delete(helper.TABLE_NAME, whereClause, whereArgs);

//                        새로운 DB 내용으로 리스트뷰 갱신
                            readAllDB();

                            Toast.makeText(AllActivity.this, "글 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("취소", null);
                    builder.show();
                }
                return true;
            }
        });
    } // onCreate() 끝나는 괄호

    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //onResume() 보다 먼저 호출된다.
        super.onActivityResult(requestCode, resultCode, data);
        updateDecision = resultCode;
        switch (requestCode) {
            case ACT_EDIT:
                if (resultCode == RESULT_OK) { // 변경되었으면 여기서 디비에 변경 작업을 해준다.
                    SQLiteDatabase db = helper.getWritableDatabase();
                    ContentValues row = new ContentValues();

                    // intent로 넘겨받은 데이터 넣어주기 - 여기 부분 다시 작성하기 안에이름은 업데이트 액티비티에서 지정함
                    row.put(helper.COL_STORE, data.getStringExtra("et_name"));
                    row.put(helper.COL_LOCATION, data.getStringExtra("et_position"));
                    row.put(helper.COL_STATUS, data.getIntExtra("et_status", 0));
                    row.put(helper.COL_CONTENTS, data.getStringExtra("et_contents"));
                    row.put(helper.COL_IMG, data.getStringExtra("et_img"));


                    String whereClause = helper.COL_ID + "=?";
                    String[] whereArgs = new String[]{String.valueOf(data.getLongExtra("id", 0))};

                    int result = db.update(helper.TABLE_NAME, row, whereClause, whereArgs);
                    String msg = result > 0 ? "수정되었습니다!" : "수정실패!";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                }
                break;
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_store_name: // 가게 검색
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(this, "빵집 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                    break;
                } else { // 정상적으로 가게이름을 검색했을 때
                    SQLiteDatabase db = helper.getReadableDatabase();
                    Cursor newCursor = db.rawQuery("SELECT * FROM "
                            + PostDBHelper.TABLE_NAME + " WHERE store LIKE '%" + editText.getText().toString() + "%';", null);

                    cursor = myCursorAdapter.swapCursor(newCursor);
                    helper.close();
                    break;
                }
            case R.id.btn_back: // 전체보기
                myCursorAdapter.changeCursor(cursor);
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
//        DB에서 데이터를 읽어와 Adapter에 설정
        if (updateDecision == RESULT_OK){
            Log.d(TAG, "디비 다시 불러오기");
            readAllDB(); // 디비읽어오는 코드 재사용을 위해 메소드로 작성함 -> onActivityResult메소드로 이동
        }else if (updateDecision == RESULT_CANCELED) {
            Log.d(TAG, "수정된 데이터가 없음");
        }
    }

    private void readAllDB(){
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + PostDBHelper.TABLE_NAME, null);
        myCursorAdapter.changeCursor(cursor);
        helper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cursor 사용 종료
        if (cursor != null) cursor.close();
    }

}
