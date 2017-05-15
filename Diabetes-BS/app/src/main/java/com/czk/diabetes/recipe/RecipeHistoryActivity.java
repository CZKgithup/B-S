package com.czk.diabetes.recipe;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.czk.diabetes.BaseActivity;
import com.czk.diabetes.DB.DBOpenHelper;
import com.czk.diabetes.R;
import com.czk.diabetes.util.FontIconDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 陈忠凯 on 2017/5/9.
 */

public class RecipeHistoryActivity extends BaseActivity {
    private static final int QUERY_FINSH = 0;
    private ImageView ivIcon;
    private List<EatRecord> records = new ArrayList<>();
    private View laoutNothing;
    private ListView lvRecord;
    private RecordAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERY_FINSH:
                    if (records.size() > 0) {
                        laoutNothing.setVisibility(View.GONE);
                        lvRecord.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    } else {
                        laoutNothing.setVisibility(View.VISIBLE);
                        lvRecord.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_history);
        initView();
        initAscnData();
        dealEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initAscnData() {
        getData();
    }

    public void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                records.clear();
                DBOpenHelper helper = new DBOpenHelper(RecipeHistoryActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor c = db.query("eat_record"
                        , new String[]{"add_time", "name", "material", "type", "time", "before_dining", "after_dining", "description"}
                        , null
                        , null
                        , null
                        , null
                        , "add_time" + " ASC");
                while (c.moveToNext()) {
                    EatRecord data =  new EatRecord(
                            c.getString(c.getColumnIndex("add_time")),
                            c.getString(c.getColumnIndex("name")),
                            c.getString(c.getColumnIndex("material")),
                            c.getString(c.getColumnIndex("type")),
                            c.getString(c.getColumnIndex("time")),
                            c.getString(c.getColumnIndex("before_dining")),
                            c.getString(c.getColumnIndex("after_dining")),
                            c.getString(c.getColumnIndex("description")));
                    records.add(data);
                }
                handler.sendEmptyMessage(QUERY_FINSH);
                c.close();
                db.close();
            }
        }).start();
    }

    private void initView() {
        /**头部**/
        ivIcon = (ImageView) findViewById(R.id.icon);
        FontIconDrawable iconArrowLeft = FontIconDrawable.inflate(getApplicationContext(), R.xml.icon_arrow_left);
        iconArrowLeft.setTextColor(getResources().getColor(R.color.white));
        ivIcon.setImageDrawable(iconArrowLeft);
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(getResources().getString(R.string.leechdom_history));

        laoutNothing = findViewById(R.id.nothing_layout);
        lvRecord = (ListView) findViewById(R.id.list);
        adapter = new RecordAdapter(RecipeHistoryActivity.this, records);
        lvRecord.setAdapter(adapter);
    }

    private void dealEvent() {
        //头部
        ivIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //主体
    }

    private class RecordAdapter extends BaseAdapter {
        private List<EatRecord> records;
        private Context context;

        public RecordAdapter(Context context, List<EatRecord> records) {
            this.records = records;
            this.context = context;

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return records.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return records.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;
            EatRecord record = records.get(position);
            if (null == view) {
                view = LayoutInflater.from(context).inflate(R.layout.item_medicine_record_layout, null);
                holder = new ViewHolder();
                holder.txtNamw = (TextView) view.findViewById(R.id.tv_name);
                holder.txtDoses = (TextView) view.findViewById(R.id.tv_doses);
                holder.txtTime = (TextView) view.findViewById(R.id.tv_time);
                holder.ivIcon = (ImageView) view.findViewById(R.id.icon);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.txtNamw.setText(record.name);
            float dValue = Float.parseFloat(record.afterDining) - Float.parseFloat(record.beforeDining);
            String value = "";
            if(dValue<0){
                value = "-"+Math.abs(dValue);
            }else {
                value = "+"+dValue;
            }
            holder.txtDoses.setText(value + getResources().getString(R.string.mmol_l));
            holder.txtTime.setText(records.get(position).time);
            holder.ivIcon.setImageDrawable(FontIconDrawable.inflate(context, R.xml.icon_spoon_knife));
            return view;
        }

        class ViewHolder {
            TextView txtNamw;
            TextView txtDoses;
            TextView txtTime;
            ImageView ivIcon;
        }
    }
}
