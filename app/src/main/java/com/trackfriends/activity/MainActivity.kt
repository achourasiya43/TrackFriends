package com.trackfriends.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.trackfriends.R
import com.trackfriends.adapter.ViewPagerAdapter
import com.trackfriends.fragment.FriendsFragment
import com.trackfriends.fragment.ProfileyFragment
import com.trackfriends.fragment.FriendRequestFragment
import com.trackfriends.session.SessionManager
import com.trackfriends.utils.Constant
import com.trackfriends.utils.Util


class MainActivity : AppCompatActivity() {
    //This is our tablayout
    private var tabLayout: TabLayout? = null
    //This is our viewPager
    private var viewPager: ViewPager? = null
    //Fragments

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        viewPager = findViewById<ViewPager>(R.id.viewpager)
        tabLayout = findViewById<TabLayout>(R.id.tablayout)

        viewPager?.setOffscreenPageLimit(3)
        tabLayout?.setupWithViewPager(viewPager)
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                viewPager!!.setCurrentItem(position, false)

            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        setupViewPager(viewPager!!)
        startLocationUpdates()
        Util.startAlarmService(this)

        viewPager!!.setCurrentItem(1, false)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        // Associate searchable configuration with the SearchView
        return true
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Constant.MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Util.startAlarmService(this)
                } else {
                    Toast.makeText(this@MainActivity, "YOUR  PERMISSION DENIED ", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {
            R.id.action_settings -> {
                Toast.makeText(this, "Home Settings Click", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_search -> {
                var intent = Intent(this,SearchFriendActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_logout -> {
                var session = SessionManager(this)
                session.logout()
                Util.stopAlarmService(this)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val fiendReqFragment: FriendRequestFragment?
        val friendsFragment: FriendsFragment?
        val historyFragmenr: ProfileyFragment?

        val adapter = ViewPagerAdapter(supportFragmentManager)
        fiendReqFragment = FriendRequestFragment()
        friendsFragment = FriendsFragment()
        historyFragmenr = ProfileyFragment()
        adapter.addFragment(fiendReqFragment, "Requests")
        adapter.addFragment(friendsFragment, "Friends")
        adapter.addFragment(historyFragmenr, "Profile")
        viewPager.adapter = adapter
    }




}
