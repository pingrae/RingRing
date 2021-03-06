package mokpoharbor.ringring;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import mokpoharbor.ringring.GuideActivity.StudentMainGuide;

public class StudentMainActivity extends AppCompatActivity {
    ArrayList<String> myclass = new ArrayList<>(), homework = new ArrayList<>(),
            homework_context = new ArrayList<>(), homework_limit = new ArrayList<>();
    String[] my_homework = homework.toArray(new String[homework.size()]),
            my_homework_context = homework_context.toArray(new String[homework_context.size()]),
            my_homework_limit = homework_limit.toArray(new String[homework_limit.size()]);
    FirebaseDatabase database;
    DatabaseReference userRef, classRef;
    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    BackPressClose back_pressed;

    @Override
    public void onBackPressed() {
        back_pressed.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_student);
        SharedPreferences pref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("user");
        classRef = database.getReference("class");
        setTitle("HOME");
        back_pressed = new BackPressClose(this);
        ImageView setting = (ImageView) findViewById(R.id.setting_image);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentMainActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mListView = (ListView) findViewById(R.id.homework_list);
        mAdapter = new StudentMainActivity.ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        for (int n = 0; n < homework.size(); n++) {
            mAdapter.addItem(my_homework[n] + " : ", my_homework_context[n], my_homework_limit[n]);
        }
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final String class_name = ((TextView) view.findViewById(R.id.mTitle)).getText().toString(),
                        class_context = ((TextView) view.findViewById(R.id.mText)).getText().toString();
                final LinearLayout row = (LinearLayout) view.findViewById(R.id.row_layout);
                ColorDrawable color = (ColorDrawable) row.getBackground();
                if (color.getColor() == Color.rgb(255, 204, 204)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(StudentMainActivity.this);
                    dialog.setTitle(class_name);
                    dialog.setMessage(class_context + " - 아직 덜함?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            row.setBackgroundColor(Color.rgb(255, 255, 255));
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean(class_context, false);
                            editor.commit();
                        }
                    });
                    dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(StudentMainActivity.this);
                    dialog.setTitle(class_name);
                    dialog.setMessage(class_context + " - 완료하셨습니까?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            row.setBackgroundColor(Color.rgb(255, 204, 204));
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean(class_context, true);
                            editor.commit();
                        }
                    });
                    dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }
                return true;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(StudentMainActivity.this, id + "", Toast.LENGTH_SHORT).show();
                final String class_name = ((TextView) v.findViewById(R.id.mTitle)).getText().toString(),
                        date = ((TextView) v.findViewById(R.id.mDate)).getText().toString();
                Date curDAte = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date reqDate = new Date();
                try {
                    reqDate = dateFormat.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long reqDateTime = reqDate.getTime();
                try {
                    curDAte = dateFormat.parse(dateFormat.format(curDAte));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long curDateTime = curDAte.getTime(), minute = (curDateTime - reqDateTime) / 60000;
                if (minute > 0) {
                    minute = -1;
                } else {
                    minute = Math.abs(minute);
                }
                long limit_day = minute / 1440, limit_hour = (minute - (limit_day * 1440)) / 60,
                        limit_minute = (minute - (limit_day * 1440 + limit_hour * 60));
                String left_time;
                if (limit_day > 0) {
                    left_time = Long.toString(limit_day) + "일 " + Long.toString(limit_hour) + "시간 " + Long.toString(limit_minute) + "분 남았습니다.";
                } else {
                    if (limit_hour > 0) {
                        left_time = Long.toString(limit_hour) + "시간 " + Long.toString(limit_minute) + "분 남았습니다.";
                    } else {
                        if (limit_minute > 0) {
                            left_time = Long.toString(limit_minute) + "분 남았습니다.";
                        } else {
                            left_time = "과제 제출기간이 지났습니다.";
                        }
                    }
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(StudentMainActivity.this);
                dialog.setTitle(class_name + "과제 남은 시간");
                dialog.setMessage(left_time);
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
        classRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myclass.clear();
                homework.clear();
                homework_context.clear();
                homework_limit.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot find_me : snapshot.child("Student").getChildren()) {
                        if (find_me.getKey().equals(MyInfo.my_id)) {
                            for (DataSnapshot my_homework : snapshot.child("Homework").getChildren()) {
                                String title = snapshot.getKey();
                                String text = my_homework.getKey();
                                String date = my_homework.getValue().toString();
                                homework.add(title);
                                homework_context.add(text);
                                homework_limit.add(date);
                            }
                        }
                    }
                    my_homework = homework.toArray(new String[homework.size()]);
                    my_homework_context = homework_context.toArray(new String[homework_context.size()]);
                    my_homework_limit = homework_limit.toArray(new String[homework_limit.size()]);
                    mListView = (ListView) findViewById(R.id.homework_list);
                    mAdapter = new StudentMainActivity.ListViewAdapter(StudentMainActivity.this);
                    mListView.setAdapter(mAdapter);
                    for (int n = 0; n < homework.size(); n++) {
                        mAdapter.addItem(my_homework[n] + " : ", my_homework_context[n], my_homework_limit[n]);
                    }
                    mAdapter.sort();
                    for (DataSnapshot find_me : snapshot.child("Student").getChildren()) {
                        if (find_me.getKey().equals(MyInfo.my_id)) {
                            for (DataSnapshot snapshot_child : snapshot.child("Homework").getChildren()) {
                                SimpleDateFormat year_formatter = new SimpleDateFormat("yyyy", Locale.KOREA),
                                        month_formatter = new SimpleDateFormat("MM", Locale.KOREA),
                                        date_formatter = new SimpleDateFormat("dd", Locale.KOREA),
                                        hour_formatter = new SimpleDateFormat("HH", Locale.KOREA),
                                        minute_formatter = new SimpleDateFormat("mm", Locale.KOREA);
                                Date currentTime = new Date();

                                int year_now = Integer.parseInt(year_formatter.format(currentTime)),
                                        month_now = Integer.parseInt(month_formatter.format(currentTime)),
                                        date_now = Integer.parseInt(date_formatter.format(currentTime)),
                                        hour_now = Integer.parseInt(hour_formatter.format(currentTime)),
                                        minute_now = Integer.parseInt(minute_formatter.format(currentTime));

                                String title = snapshot_child.getRef().getParent().getParent().getKey(),
                                        text = snapshot_child.getKey(), date = snapshot_child.getValue().toString();

                                int homework_year = -1, homework_month = -1, homework_date = -1, homework_hour = -1, homework_minute = -1;
                                //int homework_year = -1;
                                //int homework_month = -1;
                                //int homework_date = -1;
                                //int homework_hour = -1;
                                //int homework_minute = -1;

                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm"),
                                        year_only_format = new SimpleDateFormat("yyyy"),
                                        month_only_format = new SimpleDateFormat("MM"),
                                        date_only_format = new SimpleDateFormat("dd"),
                                        hour_only_format = new SimpleDateFormat("HH"),
                                        minute_only_format = new SimpleDateFormat("mm");

                                Date date_detail = null;
                                try {
                                    date_detail = format.parse(date);
                                    homework_year = Integer.parseInt(year_only_format.format(date_detail));
                                    homework_month = Integer.parseInt(month_only_format.format(date_detail));
                                    homework_date = Integer.parseInt(date_only_format.format(date_detail));
                                    homework_hour = Integer.parseInt(hour_only_format.format(date_detail));
                                    homework_minute = Integer.parseInt(minute_only_format.format(date_detail));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                int reverse_To_minute_now = (date_now * 60 * 24) + (hour_now * 60) + minute_now;
                                int reverse_To_minute_homework = (homework_date * 60 * 24) + (homework_hour * 60) + homework_minute;

                                if (year_now > homework_year) {
                                    classRef.child(title).child("Homework").child(text).removeValue();
                                    //userRef.child(MyInfo.my_id).child("my_class").child(title).child("Homework").child(text).removeValue();
                                } else if (year_now == homework_year) {
                                    if (month_now > homework_month) {
                                        classRef.child(title).child("Homework").child(text).removeValue();
                                        //userRef.child(MyInfo.my_id).child("my_class").child(title).child("Homework").child(text).removeValue();
                                    } else if (month_now == homework_month) {
                                        if ((reverse_To_minute_now - reverse_To_minute_homework) >= 1440) {
                                            classRef.child(title).child("Homework").child(text).removeValue();
                                            //userRef.child(MyInfo.my_id).child("my_class").child(title).child("Homework").child(text).removeValue();
                                        }
                                    }
                                }

                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentMainActivity.this, StudentMainGuide.class);
                startActivity(intent);
            }
        });
    }

    private class ViewHolder {
        public TextView mTitle, mText, mDate;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public void addItem(String mTitle, String mText, String mDate) {
            ListData addInfo = null;
            addInfo = new ListData();
            addInfo.mTitle = mTitle;
            addInfo.mText = mText;
            addInfo.mDate = mDate;
            mListData.add(addInfo);
        }

        public void remove(int position) {
            mListData.remove(position);
            dataChange();
        }

        public void sort() {
            Collections.sort(mListData, ListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.myrow, null);
                holder.mTitle = (TextView) convertView.findViewById(R.id.mTitle);
                holder.mText = (TextView) convertView.findViewById(R.id.mText);
                holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ListData mData = mListData.get(position);
            holder.mTitle.setText(mData.mTitle);
            holder.mText.setText(mData.mText);
            holder.mDate.setText(mData.mDate);
            Boolean homework;
            SharedPreferences pref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            homework = pref.getBoolean(mData.mText, false);
            if (homework) {
                convertView.setBackgroundColor(Color.rgb(255, 204, 204));
            } else {
                convertView.setBackgroundColor(Color.rgb(255, 255, 255));
            }
            return convertView;
        }
    }
}