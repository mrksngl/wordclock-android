package bm.wordclock;

import android.content.res.Configuration;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PluginsListFragment.OnFragmentInteractionListener {

    private ConnectionFragment mConnectionFragment;
    private PluginsListFragment mPluginListFragment;
    private boolean connecting;

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

        selectFragment(mConnectionFragment);

        /*final List<Plugin> pl = new LinkedList<>();
        pl.add(new Plugin("Test1", null));
        pl.add(new Plugin("Test2", "Desc 2"));

        new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (Exception e) {
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        mPluginListFragment.setPlugins(pl);
                    }
                });
            }
        }.start();*/
    }

    private void selectFragment(Fragment fragment) {
        connecting = (fragment == mConnectionFragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment)
                .commitAllowingStateLoss();
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
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPluginChange(int index) {
        Snackbar.make(findViewById(R.id.toolbar), "Index: " + index, Snackbar.LENGTH_SHORT).show();
    }
}
