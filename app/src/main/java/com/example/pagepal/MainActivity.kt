package com.example.pagepal

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pagepal.databinding.LayoutBinding
import com.example.pagepal.models.SharedViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

import kotlin.reflect.KProperty


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: LayoutBinding
    private lateinit var firebaseAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding =LayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(ProfileFragment())

        val mainDrawer = findViewById<DrawerLayout>(R.id.MainDrawer)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toggle = ActionBarDrawerToggle(this, mainDrawer, toolbar, R.string.open, R.string.close)
        mainDrawer.addDrawerListener(toggle)

        toggle.syncState()

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.pals -> replaceFragment(PalFragment())
                else->{

                }


            }
            true
        }


    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_container,fragment)
        fragmentTransaction.commit()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val profileDrawer = findViewById<DrawerLayout>(R.id.MainDrawer)
        when (item.itemId) {
            R.id.sliding_menu_settings -> {
                profileDrawer.findViewById<ViewGroup>(R.id.main_container)?.removeAllViews()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, SettingsFragment()).commit()}
            R.id.sliding_menu_borrow -> {
                profileDrawer.findViewById<ViewGroup>(R.id.main_container)?.removeAllViews()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, BorrowFragment()).commit()}
            R.id.sliding_menu_lending -> {
                profileDrawer.findViewById<ViewGroup>(R.id.main_container)?.removeAllViews()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, LendingFragment()).commit()}

            R.id.sliding_menu_logout -> {
                logoutUser()
            }

        }
        profileDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {

        val mainDrawer = findViewById<DrawerLayout>(R.id.MainDrawer)
        if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
            mainDrawer.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun logoutUser() {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        // Redirect the user to the login or splash screen
        // You can choose the appropriate activity to redirect the user after logout.
        val intent = Intent(this, LoginScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish() // Optionally, you may want to finish the current activity to prevent going back to it with the back button.
    }
}






