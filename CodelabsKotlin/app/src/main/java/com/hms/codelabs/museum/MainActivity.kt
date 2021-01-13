package com.hms.codelabs.museum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hms.codelabs.museum.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var viewModel: SharedViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        if (navHostFragment != null) {
            val navController = navHostFragment.navController
            NavigationUI.setupWithNavController(navView, navController)
        }
    }
}