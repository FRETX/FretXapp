package com.pandor.fretxapp.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.pandor.fretxapp.R;
import com.pandor.fretxapp.pages.ViewPagerAdapter;
import com.pandor.fretxapp.pages.chord.ChordFragment;
import com.pandor.fretxapp.pages.learn.LearnFragment;
import com.pandor.fretxapp.pages.song.PlayFragment;
import com.pandor.fretxapp.utils.Bluetooth;

public class MainActivity extends BaseActivity {

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Bluetooth.getInstance().isEnabled()) {
            if (Bluetooth.getInstance().isConnected()){
                Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(false);

        setupTabLayout();
    }

    private void setupTabLayout() {
        //adapter
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //// TODO: 18/04/17 add icons
        adapter.addFrag(new PlayFragment(), "Songs");
        adapter.addFrag(new LearnFragment(), "Learn");
        adapter.addFrag(new ChordFragment(), "Chords");

        //view pager
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Log.d("FRAGMENT_MANAGER", "stack cleaned");
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                actionBar.setTitle(adapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        //tab layout
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //update activity title
        actionBar.setTitle(adapter.getPageTitle(0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //// TODO: 18/04/17 add search bar action code
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }
}
