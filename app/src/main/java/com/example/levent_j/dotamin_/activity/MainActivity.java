package com.example.levent_j.dotamin_.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.levent_j.dotamin_.R;
import com.example.levent_j.dotamin_.base.BaseActivity;
import com.example.levent_j.dotamin_.fragment.HeroFragment;
import com.example.levent_j.dotamin_.fragment.HistoryFragment;
import com.example.levent_j.dotamin_.fragment.UserFragment;
import com.example.levent_j.dotamin_.utils.InputDialog;
import com.example.levent_j.dotamin_.utils.PreferceService;
import com.example.levent_j.dotamin_.utils.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    @Bind(R.id.toolbar)
    Toolbar mTooBarr;
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @Bind(R.id.nav_view)
    NavigationView mNav;
    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.tabLayout)
    TabLayout mTabLayout;


    public MyFragmentAdapter myFragmentAdapter;
    private static final String[] TITLE = {"User","History","Hero"};
    private int[] tabicon = {R.drawable.ic_user,R.drawable.ic_history,R.drawable.ic_dota};
    private int searchType;
    private String searchTitle;
    private long lastExitTime = 0;
    private PreferceService preferceService;
    private boolean isFirst;


    @Override
    protected void init() {

        setSupportActionBar(mTooBarr);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mTooBarr, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
        mNav.setNavigationItemSelectedListener(this);

        myFragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager(),this);

        myFragmentAdapter.addFragment(UserFragment.newInstance(TITLE[0]),TITLE[0]);
        myFragmentAdapter.addFragment(HistoryFragment.newInstance(TITLE[1]),TITLE[1]);
        myFragmentAdapter.addFragment(HeroFragment.newInstance(TITLE[2]), TITLE[2]);
        mViewPager.setAdapter(myFragmentAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);


        //为tab的标题设置icon
        for (int i=0;i< mTabLayout.getTabCount();i++){
            mTabLayout.getTabAt(i).setIcon(tabicon[i]);
        }



        //在此处理显示好友信息界面
        String s = getIntent().getStringExtra("id");
        if (s==null){
            isFirst = true;
            //第一次开启MainActivity
            // 做本地缓存
            preferceService = new PreferceService(this);
            Map<String,String> params = preferceService.getPreferences();
            String id = params.get(PreferceService.KEY);
            if (!id.equals("null")){
                msg("save","share id is"+id);
                UserFragment userFragment = (UserFragment) myFragmentAdapter.getItem(mViewPager.getCurrentItem());
                userFragment.loadUserDate(Util.get64Id(Long.parseLong(id)));
                userFragment.loadFrinedsDate(Util.get64Id(Long.parseLong(id)));
                HistoryFragment historyFragment = (HistoryFragment)myFragmentAdapter.getItem(mViewPager.getCurrentItem()+1);
                historyFragment.setId(id);
                historyFragment.loadDate(id);
            }

            msg("save","main id :"+id);
        }else {
            isFirst = false;
            //通过点击好友开启新的MainActivity时要加载他的好友列表和比赛历史
            UserFragment userFragment = (UserFragment) myFragmentAdapter.getItem(mViewPager.getCurrentItem());
            userFragment.loadUserDate(s);
            userFragment.loadFrinedsDate(s);
            userFragment.flag=true;
            HistoryFragment historyFragment = (HistoryFragment)myFragmentAdapter.getItem(mViewPager.getCurrentItem()+1);
            historyFragment.loadDate(Util.get32Id(Long.parseLong(s)));
        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void setListener() {
        mFab.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (isFirst){
                if ((System.currentTimeMillis() - lastExitTime) > 3000) {
                    Toast.makeText(this,"重复操作退出应用",Toast.LENGTH_SHORT).show();
                    lastExitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }else {
                finish();
            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            new AlertDialog.Builder(this)
                    .setMessage("请点击下方“搜索”按钮并输入自己的dota2ID（如：129639720）/比赛ID（如：2217239289）/英雄名（如：敌法师）")
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            // Handle the camera action
        } else if (id == R.id.nav_news) {
            Toast.makeText(this,"抱歉，该功能暂未实现",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_gba) {
            Toast.makeText(this,"抱歉，该功能暂未实现",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_data) {
            Toast.makeText(this,"抱歉，该功能暂未实现",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }else if (id == R.id.nav_feed){
            startActivity(new Intent(this, FeedActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.fab:
                final String s = myFragmentAdapter.getTitle(mViewPager.getCurrentItem());
                if (s.equals(TITLE[0])){
                    searchType = 1;
                    searchTitle = "dota2 id";
                }else if (s.equals(TITLE[1])){
                    searchType = 2;
                    searchTitle = "比赛 id";
                }else {
                    searchType = 3;
                    searchTitle = "英雄名称";
                }
                new InputDialog.Builder(this)
                        .setTitle("搜索")
                        .setInputMaxWords(10)
                        .setInputHint("在此填入" + searchTitle)
                        .setPositiveButton("搜索", new InputDialog.ButtonActionListener() {
                            @Override
                            public void onClick(CharSequence inputText) {
                                switch (searchType){
                                    case 1:
                                            try {
                                                String s = inputText.toString().trim();
                                                loadAllDate(s);
//
                                            }catch (NumberFormatException e){
                                                Snackbar.make(v, "填写错误！", Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                                msg("debug",e.getMessage());
                                            }

                                        break;
                                    case 2:
                                        //搜索比赛的活动
                                        try {
                                            String s = inputText.toString().trim();
                                            final Intent intent = new Intent(MainActivity.this,MatchDetailActivity.class);
                                            intent.putExtra("matchid",s);
                                            startActivity(intent);
                                        }catch (NumberFormatException e){
                                            Snackbar.make(v, "填写错误！", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            msg("debug",e.getMessage());
                                        }

                                        break;
                                    case 3:
                                        //搜索英雄的活动
                                        Snackbar.make(v, "正在搜索,请稍后", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        final String name = inputText.toString().trim();
                                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Hero");
                                        query.whereEqualTo("heroName", name);
                                        query.findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> objects, ParseException e) {
                                                if (e==null&&objects.size()!=0){

                                                    Intent intent1 = new Intent(MainActivity.this,HeroDetailActivity.class);
                                                    intent1.putExtra("name", name);
                                                    startActivity(intent1);
                                                }else {
                                                    Snackbar.make(v, "输入错误！", Snackbar.LENGTH_LONG)
                                                            .setAction("Action", null).show();
                                                }
                                            }
                                        });
                                        break;
                                }

                            }
                        })
                        .setNegativeButton("取消", new InputDialog.ButtonActionListener() {
                            @Override
                            public void onClick(CharSequence inputText) {
                                //取消之后的动作
                            }
                        })
                        .show();
                break;
        }

    }

    private void loadAllDate(String s) {
        try {
        UserFragment userFragment = (UserFragment) myFragmentAdapter.getItem(mViewPager.getCurrentItem());
        userFragment.loadUserDate(Util.get64Id(Long.parseLong(s)));
        userFragment.loadFrinedsDate(Util.get64Id(Long.parseLong(s)));
        userFragment.mPopPoint.setVisibility(View.VISIBLE);
        //在此同时加载比赛记录列表
        HistoryFragment historyFragment = (HistoryFragment)myFragmentAdapter.getItem(mViewPager.getCurrentItem()+1);
        historyFragment.loadDate(s);
        save("id", s);
        }catch (NumberFormatException ignored){

        }
    }

    private void save(String id, String s) {
        msg("save",s);

        preferceService.save(id,s);
    }

    private class MyFragmentAdapter extends FragmentPagerAdapter{

        private final List<Fragment> fragmentList;
        public final List<String> titleList;
        private Context context;

        public MyFragmentAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
            fragmentList = new ArrayList<>();
            titleList = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }


        @Override
        public int getCount() {
            return titleList.size();
        }


        public void addFragment(Fragment fragment,String title){
            fragmentList.add(fragment);
            titleList.add(title);
        }

        public String getTitle(int position){
            switch (position){
                case 0:
                    return "User";
                case 1:
                    return "History";
                default:
                    return "hero";
            }
        }
    }
}
