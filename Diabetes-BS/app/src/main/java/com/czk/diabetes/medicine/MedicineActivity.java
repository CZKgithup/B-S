package com.czk.diabetes.medicine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.czk.diabetes.BaseActivity;
import com.czk.diabetes.DB.DBOpenHelper;
import com.czk.diabetes.R;
import com.czk.diabetes.util.FontIconDrawable;
import com.czk.diabetes.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 陈忠凯 on 2017/5/8.
 */

public class MedicineActivity extends BaseActivity {
    private static final int QUERY_FINSH = 0;
    private ImageView ivIcon;
    private View laoutAdd;
    private List<MedicineRecord> records = new ArrayList<>();
    private ListView lvRecord;
    private RecordAdapter adapter;
    private ImageView ivRightIcon;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERY_FINSH:
                    if (records.size() > 0) {
                        laoutAdd.setVisibility(View.GONE);
                        lvRecord.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        ivRightIcon.setVisibility(View.VISIBLE);
                    } else {
                        laoutAdd.setVisibility(View.VISIBLE);
                        lvRecord.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        ivRightIcon.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);
        initView();
        dealEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        getData();
    }

    public void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                records.clear();
                DBOpenHelper helper = new DBOpenHelper(MedicineActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor c = db.query("medicine_record"
                        , new String[]{"add_time","name", "doses", "time", "peroid_start", "peroid_end", "notifition", "description"}
                        , "peroid_end >= ? and peroid_start <= ? and time >= ?"
                        , new String[]{TimeUtil.getYearMonthDay(System.currentTimeMillis())
                                , TimeUtil.getYearMonthDay(System.currentTimeMillis())
                                ,TimeUtil.getSringByFormat(System.currentTimeMillis(),"HH:mm")}
                        , "add_time"
                        , "min(time)"
                        , "time" + " ASC");
                while (c.moveToNext()) {
                    MedicineRecord data = new MedicineRecord(
                            c.getString(c.getColumnIndex("add_time")),
                            c.getString(c.getColumnIndex("name")),
                            c.getString(c.getColumnIndex("doses")),
                            c.getString(c.getColumnIndex("time")),
                            c.getString(c.getColumnIndex("peroid_start")),
                            c.getString(c.getColumnIndex("peroid_end")),
                            c.getString(c.getColumnIndex("notifition")),
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
        tvTitle.setText(getResources().getString(R.string.leechdom));
        /**主体**/
        FontIconDrawable iconPill = FontIconDrawable.inflate(getApplicationContext(), R.xml.icon_pill);
        iconPill.setTextColor(getResources().getColor(R.color.white));
        FontIconDrawable iconClipboardEdit = FontIconDrawable.inflate(getApplicationContext(), R.xml.icon_clipboard_edit);
        iconClipboardEdit.setTextColor(getResources().getColor(R.color.white));
        ImageView ivPill = (ImageView) findViewById(R.id.left_icon);
        ImageView ivHistory = (ImageView) findViewById(R.id.right_icon);
        ivPill.setImageDrawable(iconPill);
        ivHistory.setImageDrawable(iconClipboardEdit);

        /**添加**/
        FontIconDrawable iconPlus = FontIconDrawable.inflate(getApplicationContext(), R.xml.icon_plus);
        iconPlus.setTextColor(getResources().getColor(R.color.txt_light_color));
        ImageView ivPlus = (ImageView) findViewById(R.id.add_icon);
        ivPlus.setImageDrawable(iconPlus);
        laoutAdd = findViewById(R.id.add_layout);

        ivRightIcon = (ImageView) findViewById(R.id.title_right_icon);
        ivRightIcon.setImageDrawable(iconPlus);

        lvRecord = (ListView) findViewById(R.id.list);
        adapter = new RecordAdapter(MedicineActivity.this, records);
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
        findViewById(R.id.left_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MedicineActivity.this, SearchMedicineActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.right_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MedicineActivity.this, MedicineHistoryActivity.class);
                startActivity(intent);
            }
        });

        laoutAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MedicineActivity.this, AddMedicineActivity.class);
                startActivity(intent);
            }
        });

        ivRightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MedicineActivity.this, AddMedicineActivity.class);
                startActivity(intent);
            }
        });

        lvRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MedicineActivity.this, AddMedicineActivity.class);
                intent.putExtra(AddMedicineActivity.INTENT_ADD_TIME, records.get(position).addTime);
                startActivity(intent);
            }
        });
    }


    private class RecordAdapter extends BaseAdapter {
        private List<MedicineRecord> records;
        private Context context;

        public RecordAdapter(Context context, List<MedicineRecord> records) {
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
            MedicineRecord record = records.get(position);
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
            holder.txtDoses.setText(record.doses + getResources().getString(R.string.mg));
            holder.txtTime.setText(records.get(position).time);
            holder.ivIcon.setImageDrawable(FontIconDrawable.inflate(context, R.xml.icon_pill));
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
