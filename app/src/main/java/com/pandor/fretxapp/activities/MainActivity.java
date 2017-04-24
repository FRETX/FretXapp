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

import com.pandor.fretxapp.R;
import com.pandor.fretxapp.pages.ViewPagerAdapter;
import com.pandor.fretxapp.pages.chord.ChordFragment;
import com.pandor.fretxapp.pages.learn.LearnFragment;
import com.pandor.fretxapp.pages.song.PlayFragment;
import com.pandor.fretxapp.utils.Bluetooth.Bluetooth;
import com.pandor.fretxapp.utils.Bluetooth.BluetoothListener;

public class MainActivity extends BaseActivity {
    private static final String TAG = "KJKP6_MAINACTIVITY";

    private ActionBar actionBar;
    private MenuItem bluetoothIcon;

    private final BluetoothListener bluetoothListener = new BluetoothListener() {
        @Override
        public void onConnect() {
            Log.d(TAG, "Connected!");
            bluetoothIcon.setIcon(getResources().getDrawable(R.drawable.view_fretboard_blue_led, null));
            Bluetooth.getInstance().clearMatrix();
        }

        @Override
        public void onScanFailure() {
            Log.d(TAG, "Failed!");
            bluetoothIcon.setIcon(getResources().getDrawable(R.drawable.view_fretboard_red_led, null));
        }

        @Override
        public void onDisconnect() {
            Log.d(TAG, "Failed!");
            bluetoothIcon.setIcon(getResources().getDrawable(R.drawable.view_fretboard_red_led, null));
        }

        @Override
        public void onFailure(){
            Log.d(TAG, "Failed!");
            bluetoothIcon.setIcon(getResources().getDrawable(R.drawable.view_fretboard_red_led, null));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(false);

        setupTabLayout();

        Bluetooth.getInstance().setListener(bluetoothListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        bluetoothIcon = menu.findItem(R.id.action_bluetooth);
        if (Bluetooth.getInstance().isConnected()) {
            bluetoothIcon.setIcon(getResources().getDrawable(R.drawable.view_fretboard_blue_led, null));
        } else {
            bluetoothIcon.setIcon(getResources().getDrawable(R.drawable.view_fretboard_red_led, null));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth:
                bluetoothIcon.setIcon(getResources().getDrawable(R.drawable.view_fretboard_red_led, null));
                Bluetooth.getInstance().disconnect();
                Bluetooth.getInstance().scan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
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
}
