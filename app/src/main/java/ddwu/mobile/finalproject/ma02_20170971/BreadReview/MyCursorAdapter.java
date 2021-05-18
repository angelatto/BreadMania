package ddwu.mobile.finalproject.ma02_20170971.BreadReview;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import ddwu.mobile.finalproject.ma02_20170971.R;

public class MyCursorAdapter extends CursorAdapter {

    LayoutInflater inflater;
    int layout;

    public MyCursorAdapter(Context context, int layout, Cursor c) {
        super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //아래의 layout은 생성자를 통해 들어온 layout 멤버이다.
        View view = inflater.inflate(layout, parent, false);
        ViewHolder holder = new ViewHolder();
        view.setTag(holder); // 일단 홀더에 비워진 상태로 보관만 해둔다. 아직 뷰가 만들어 지지 않은 상태
        return view;
    }

    // newView 에서 반환된 뷰를 bindView의 매개변수로 들어간다.
    // newView는 항목뷰가 만들어질때마다 호출되고, bindView는 데이터와 연결될 때 호출된다.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag(); // 위에서 setTag로 만들어 놓은거 가져오기
        if (holder.store == null){  //findViewById를 여러번 호출하면 효율이 안좋아지므로 정적객체에 저장함
            holder.store = view.findViewById(R.id.tv_store);
            holder.status = view.findViewById(R.id.tv_status);
            holder.position = view.findViewById(R.id.tv_postition);
        }

        holder.store.setText(cursor.getString(cursor.getColumnIndex(PostDBHelper.COL_STORE)));
        int status = cursor.getInt(cursor.getColumnIndex(PostDBHelper.COL_STATUS));
        if (status == 0) { // 재방문 안하겠다
            holder.status.setText("재방문 X");
        }else{ // 1이면 재방문 하겠다.
            holder.status.setText("재방문 O");
        }
        holder.position.setText(cursor.getString(cursor.getColumnIndex(PostDBHelper.COL_LOCATION)));
    }

    static class ViewHolder {

        public ViewHolder(){ // 명시적으로 null로 초기화해줌 (기본적으로 static객체는 널로 초기화됨)
            store = null;
            status = null;
            position = null;
        }

        TextView store;
        TextView status;
        TextView position;
    }
}
