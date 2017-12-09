package com.example.wangsl.androidfragment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MyRecyclerViewAdapter mRecyclerViewAdapter;
    private TextView car;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerView);

        mLinearLayoutManager = new LinearLayoutManager(this);

        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerViewAdapter = new MyRecyclerViewAdapter(this,Images.imageUrls);

        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        car=(TextView)findViewById(R.id.car);
        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"ddd",Toast.LENGTH_SHORT).show();
            }
        });

        try {
            Exeception();
        } catch (IOException e) {
            Toast.makeText(this, "----", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }

    }
    public void Exeception() throws IOException {

        throw new IOException("ddd");
    }

}
