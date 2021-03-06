package ddwu.mobile.finalproject.ma02_20170971.NaverBlogAPI;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ddwu.mobile.finalproject.ma02_20170971.R;

public class BlogAdapter extends BaseAdapter {

    public static final String TAG = "BlogAdapter";

    private LayoutInflater inflater;
    private Context context;
    private int layout;
    private ArrayList<NaverBlogDto> list;


    public BlogAdapter(Context context, int resource, ArrayList<NaverBlogDto> list) {
        this.context = context;
        this.layout = resource;
        this.list = list;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public NaverBlogDto getItem(int position) {
        return list.get(position);
    }


    @Override
    public long getItemId(int position) {
        return list.get(position).get_id();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView with position : " + position);
        View view = convertView;
        ViewHolder viewHolder = null;

        if (view == null) {
            view = inflater.inflate(layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = view.findViewById(R.id.lv_title);
            viewHolder.tvDescription = view.findViewById(R.id.lv_description);
            viewHolder.tvPostdate = view.findViewById(R.id.lv_postDate);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        NaverBlogDto dto = list.get(position);

        viewHolder.tvTitle.setText(dto.getTitle());
        viewHolder.tvDescription.setText(dto.getDescription());
        viewHolder.tvPostdate.setText(dto.getPostdate());

        return view;
    }


    public void setList(ArrayList<NaverBlogDto> list) {
        this.list = list;
    }


    //    ??? findViewById() ?????? ????????? ?????? ????????? ????????? ???
    static class ViewHolder {
        public TextView tvTitle = null;
        public TextView tvDescription = null;
        public TextView tvPostdate = null;
    }
}
