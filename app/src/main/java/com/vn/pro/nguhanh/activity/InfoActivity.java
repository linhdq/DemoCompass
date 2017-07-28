package com.vn.pro.nguhanh.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.vn.pro.nguhanh.R;
import com.vn.pro.nguhanh.adapter.ViewPagerAdapter;
import com.vn.pro.nguhanh.fragment.AboutUsFragment;
import com.vn.pro.nguhanh.fragment.CompassFragment;
import com.vn.pro.nguhanh.fragment.StyleGuideFragment;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = InfoActivity.class.getSimpleName();
    //view
    private CircleIndicator circleIndicator;
    private ViewPager viewPager;
    private ImageView btnBack;
    private ImageView imvLogo;
    //
    private ViewPagerAdapter viewPagerAdapter;
    //fragment
    private List<Fragment> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        //
        init();
        addListener();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void init() {
        //view
        circleIndicator = (CircleIndicator) findViewById(R.id.circle_indicator);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        btnBack = (ImageView) findViewById(R.id.btn_back);
        imvLogo = (ImageView) findViewById(R.id.imv_logo);
        //fragment
        list = new ArrayList<>();
        list.add(new AboutUsFragment());
        list.add(new CompassFragment());
        list.add(new StyleGuideFragment());
        //
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(viewPagerAdapter);
        circleIndicator.setViewPager(viewPager);
    }

    private void addListener() {
        btnBack.setOnClickListener(this);
        imvLogo.setOnClickListener(this);
    }

    private void openWebBrowser() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(HomeActivity.URL));
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.imv_logo:
                openWebBrowser();
                break;
            default:
                break;
        }
    }
}
