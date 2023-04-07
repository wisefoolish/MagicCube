package com.example.rubikcube;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private GameView mGameView;
    private String TAG="hxd";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 访问父类的方法
        mGameView=new GameView(this);
        // setContentView(R.layout.try_view);
        // 填充一个面也就是drawlayer_across那里原来的写法有问题，ZERO值不正确
        setContentView(mGameView);
        mGameView.setOnTouchListener(this);
    }
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        mGameView.ReciveMessage(event);
        return true;
    }
}
