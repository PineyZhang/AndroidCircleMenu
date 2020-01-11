package com.zhy.sample_circlemenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zhy.view.UECircleMenuLayout;

/**
 * Created by zsg on 2020-01-02.
 * Desc:
 * <p>
 * Copyright (c) 2020 UePay.mo All rights reserved.
 */
public class CircleViewActivity extends Activity {

    private UECircleMenuLayout mCircleMenuLayout;

    private String[] mItemTexts = new String[]{
            "0開啟你的電子支付 ", "1開啟你的電子支付", "2開啟你的電子支付", "3開啟你的電子支付",
            "4開啟你的電子支付", "5開啟你的電子支付", "6開啟你的電子支付", "7開啟你的電子支付",
            "8開啟你的電子支付", "9開啟你的電子支付"};
    private int[] mItemImgs = new int[]{
            R.drawable.home_mbank_1_normal, R.drawable.home_mbank_2_normal,
            R.drawable.home_mbank_3_normal, R.drawable.home_mbank_4_normal,
            R.drawable.home_mbank_5_normal, R.drawable.home_mbank_6_normal,
            R.drawable.home_mbank_3_normal, R.drawable.home_mbank_4_normal,
            R.drawable.home_mbank_5_normal, R.drawable.home_mbank_6_normal};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_view);
        mCircleMenuLayout = (UECircleMenuLayout) findViewById(R.id.id_menulayout);
        mCircleMenuLayout.setMenuItemIconsAndTexts(mItemImgs, mItemTexts);


        mCircleMenuLayout.setOnMenuItemClickListener(new UECircleMenuLayout.OnMenuItemClickListener() {

            @Override
            public void itemClick(View view, int pos) {
                Toast.makeText(CircleViewActivity.this, mItemTexts[pos], Toast.LENGTH_SHORT).show();

            }

            @Override
            public void itemCenterClick(View view) {
                Toast.makeText(CircleViewActivity.this, "you can do something just like ccb  ", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
