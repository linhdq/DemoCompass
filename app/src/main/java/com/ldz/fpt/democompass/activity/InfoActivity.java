package com.ldz.fpt.democompass.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ldz.fpt.democompass.R;
import com.ldz.fpt.democompass.adapter.ViewPagerAdapter;
import com.ldz.fpt.democompass.fragment.AboutUsFragment;
import com.ldz.fpt.democompass.fragment.CompassFragment;
import com.ldz.fpt.democompass.fragment.StyleGuideFragment;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class InfoActivity extends AppCompatActivity {
    private static final String TAG = InfoActivity.class.getSimpleName();
    //view
    private CircleIndicator circleIndicator;
    private ViewPager viewPager;
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
}
