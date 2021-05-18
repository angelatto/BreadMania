package ddwu.mobile.finalproject.ma02_20170971.BreadReview;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PostDBHelper extends SQLiteOpenHelper {

    static final String TAG = "PostDBHelper";
    private final static String DB_NAME = "post_db";
    public final static String TABLE_NAME = "post_table";
    public final static String COL_ID = "_id";
    public final static String COL_STORE = "store";
    public final static String COL_LOCATION = "location";
    public final static String COL_STATUS = "status";  // 라디오 버튼
    public final static String COL_CONTENTS = "contents";
    public final static String COL_IMG = "img";


    public PostDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_NAME + " ( " + COL_ID + " integer primary key autoincrement,"
                + COL_STORE + " TEXT, " + COL_LOCATION + " TEXT, " + COL_STATUS + " integer, "
                        + COL_CONTENTS + " TEXT, "  + COL_IMG + " TEXT)";

        Log.d(TAG, sql);
        db.execSQL(sql);

        // 재구매 O
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '브래드서랭', '서울특별시 강동구 성내동 469-7번지 에스와이빌 102호', 1, '빵이 촉촉해서 맛있다.', '');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '바이재재', '서울특별시 강동구 성내동 405-45', 1, '직원분이 친절해서 너무 맘에 든다.', '');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '블랑제리11-17', '서울특별시 강동구 성내2동 78-1', 1, '가격은 비싸지만 디저트 종류가 다양해서 다시 가고 싶다.', '');");

        // 재구매 X
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '하이몬드', '서울특별시 강동구 성내동 천호옛14길 11', 0, '가격대비 맛있는 줄은 모르겠다.', '');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '뚜레쥬르 천호로데오', '서울특별시 강동구 천호동 453-15', 0, '가게 위생상태가 안좋아 보였다.' ,'');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '모모식빵', '서울특별시 강동구 천호동 450번지 상떼빌 L동 105호', 0, '빵이 오래된 걸 파는 것 같다.', '');");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + TABLE_NAME);
        onCreate(db);
    }
}
