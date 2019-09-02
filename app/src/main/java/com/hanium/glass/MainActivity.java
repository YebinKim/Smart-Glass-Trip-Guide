package com.hanium.glass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonNavigationClicked(View v) {
        Intent intent=new Intent(MainActivity.this, NavigationActivity.class);
        startActivity(intent);
    }

    public void onButtonTranslationClicked(View v) {
        Intent intent=new Intent(MainActivity.this, TranslationActivity.class);
        startActivity(intent);
    }
}
