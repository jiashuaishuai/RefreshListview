package com.example.administrator.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {

    private RefreshListView mRefreshListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshListView = ((RefreshListView) findViewById(R.id.refreshListView));
        mRefreshListView.setRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onRefreshing() {
                mRefreshListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshListView.onRefreshed();
                    }
                }, 2000);
            }

            @Override
            public void onLoading() {
                mRefreshListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshListView.onLoaded();
                    }
                }, 2000);
            }
        });

        String[] array = new String[15];
        for (int i = 0; i < 15; i++) {
            array[i] = i + "层";
        }
        mRefreshListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array));
    }
}
