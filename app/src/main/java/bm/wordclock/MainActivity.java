package bm.wordclock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Collection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PluginsListFragment.OnFragmentInteractionListener,
        ControllerFragment.OnFragmentInteractionListener,
        WCCommCallbacks {

    private ConnectionFragment mConnectionFragment;
    private PluginsListFragment mPluginListFragment;
    private ControllerFragment mControllerFragment;

    private boolean connecting;

    private CommProxy mCommunication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mConnectionFragment = new ConnectionFragment();
        mPluginListFragment = new PluginsListFragment();
        mControllerFragment = new ControllerFragment();

        selectFragment(mConnectionFragment);
    }

    private void selectFragment(Fragment fragment) {
        connecting = (fragment == mConnectionFragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onPause() {
        super.onPause();

        mCommunication.stop();
        mCommunication = null;
    }

    @Override
    public void onPostResume() {
        super.onPostResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String hostName = sharedPref.getString(SettingsActivity.GeneralPreferenceFragment.KEY_HOST_NAME, "");
        mCommunication = new CommProxy(this, hostName, this);
        mCommunication.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (!connecting) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_plugins) {
                selectFragment(mPluginListFragment);
            } else if (id == R.id.nav_keyboard) {
                selectFragment(mControllerFragment);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPluginChange(int index) {
        mCommunication.setActivePlugin(index);
    }

    @Override
    public void setState(STATE state) {
        switch (state) {
            case CONNECTED:
                selectFragment(mPluginListFragment);
                break;
            case DISCONNECTED:
                Toast.makeText(this, R.string.connect_connectionlost, Toast.LENGTH_SHORT).show();
                mConnectionFragment.hideText();
                selectFragment(mConnectionFragment);
                break;
            case COULD_NOT_CONNECT:
                mConnectionFragment.setText(getText(R.string.connect_couldnotconnect));
                break;
            case HOST_UNKNOWN:
                mConnectionFragment.setText(getText(R.string.connect_unknownhost));
                break;
        }
    }

    @Override
    public void setPlugins(Collection<Plugin> plugins) {
        mPluginListFragment.setPlugins(plugins);
    }

    @Override
    public void setActivePlugin(int index) {
        mPluginListFragment.setCurrentPlugin(index);
    }

    @Override
    public void onLeftClicked() {
        mCommunication.btn_left();
    }

    @Override
    public void onRightClicked() {
        mCommunication.btn_right();
    }

    @Override
    public void onReturnClicked() {
        mCommunication.btn_return();
    }
}
