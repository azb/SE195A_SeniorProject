package com.sjsu.se195.irom;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ImageButton img;
    private TextView txt;
    int num = 0;
    int sized = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = (ImageButton) findViewById(R.id.imageID);
        txt = (TextView) findViewById(R.id.textID);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(sized == 0){
                    sized = 1;
                    txt.setTextSize(70);
                }else{
                    sized = 0;
                    txt.setTextSize(40);
                }

                if (num == 0){
                    //change to b
                    num = 1;
                    img.setImageResource(R.drawable.b12633703);
                }else{
                    //change to a
                    num = 0;
                    img.setImageResource(R.drawable.a12633702);
                }
            }
        });
    }

}
