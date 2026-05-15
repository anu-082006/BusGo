package com.example.buss

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvPersonalName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvMobile: TextView
    private lateinit var rvTrustedContacts: RecyclerView
    private lateinit var contactsAdapter: TrustedContactsAdapter
    
    private val contactsList = mutableListOf<TrustedContact>()

    private val contactPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val contactUri = result.data?.data ?: return@registerForActivityResult
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)
                    val number = cursor.getString(1)
                    addContact(name, number)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadUserData()
        setupListeners()
        setupBottomNavigation()
    }

    private fun initViews() {
        tvPersonalName = findViewById(R.id.tvPersonalName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvGender = findViewById(R.id.tvGender)
        tvMobile = findViewById(R.id.tvMobile)
        rvTrustedContacts = findViewById(R.id.rvTrustedContacts)

        rvTrustedContacts.layoutManager = LinearLayoutManager(this)
        contactsAdapter = TrustedContactsAdapter(contactsList) { contact ->
            showDeleteContactDialog(contact)
        }
        rvTrustedContacts.adapter = contactsAdapter
    }

    private fun loadUserData() {
        // Integrate Signup details from AppPrefsHelper
        val name = AppPrefsHelper.getName(this).ifEmpty { "John Doe" }
        val email = AppPrefsHelper.getEmail(this) ?: "john.doe@example.com"
        val mobile = AppPrefsHelper.getMobile(this).ifEmpty { "+91 9876543210" }
        val gender = AppPrefsHelper.getGender(this).ifEmpty { "Male" }

        tvPersonalName.text = "Name: $name"
        tvUserEmail.text = "Email: $email"
        tvGender.text = "Gender: $gender"
        tvMobile.text = "Mobile: $mobile"
        
        loadContacts()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, RouteSearchActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadContacts() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val contactsJson = prefs.getString("contacts", "[]")
        contactsList.clear()
        try {
            val jsonArray = JSONArray(contactsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                contactsList.add(TrustedContact(obj.getString("name"), obj.getString("number")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        contactsAdapter.notifyDataSetChanged()
    }

    private fun saveContacts() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (contact in contactsList) {
            val obj = JSONObject()
            obj.put("name", contact.name)
            obj.put("number", contact.number)
            jsonArray.put(obj)
        }
        prefs.edit().putString("contacts", jsonArray.toString()).apply()
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSOS).setOnClickListener { showSOSDialog() }
        findViewById<TextView>(R.id.btnEditPersonal).setOnClickListener { showEditPersonalDialog() }
        findViewById<TextView>(R.id.btnEditLogin).setOnClickListener { showEditLoginDialog() }
        findViewById<TextView>(R.id.btnAddContact).setOnClickListener { pickContact() }
        findViewById<ImageView>(R.id.ivInfoSOS).setOnClickListener { showSOSInfoDialog() }
        findViewById<TextView>(R.id.btnChangePassword).setOnClickListener { showChangePasswordDialog() }
        findViewById<TextView>(R.id.btnDeleteAccount).setOnClickListener { showDeleteAccountDialog() }
        findViewById<Button>(R.id.btnLogout).setOnClickListener { showLogoutDialog() }
    }

    private fun showEditPersonalDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_personal, null)
        val etName = view.findViewById<EditText>(R.id.etName)
        val rgGender = view.findViewById<RadioGroup>(R.id.rgGender)
        
        val currentName = tvPersonalName.text.toString().removePrefix("Name: ")
        etName.setText(currentName)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Personal Details")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString()
                val gender = when(rgGender.checkedRadioButtonId) {
                    R.id.rbFemale -> "Female"
                    R.id.rbOther -> "Other"
                    else -> "Male"
                }
                savePersonalDetails(newName, gender)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun savePersonalDetails(name: String, gender: String) {
        AppPrefsHelper.saveUser(
            this, 
            name, 
            AppPrefsHelper.getEmail(this) ?: "", 
            AppPrefsHelper.getPassword(this) ?: "",
            AppPrefsHelper.getMobile(this),
            gender
        )
        
        tvPersonalName.text = "Name: $name"
        tvGender.text = "Gender: $gender"
    }

    private fun showEditLoginDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_login, null)
        val etMobile = view.findViewById<EditText>(R.id.etMobile)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        
        etMobile.setText(tvMobile.text.toString().removePrefix("Mobile: "))
        etEmail.setText(tvUserEmail.text.toString().removePrefix("Email: "))

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Login Details")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val mobile = etMobile.text.toString()
                val email = etEmail.text.toString()
                if (mobile.isNotEmpty() && email.contains("@")) {
                    AppPrefsHelper.saveUser(
                        this, 
                        AppPrefsHelper.getName(this), 
                        email, 
                        AppPrefsHelper.getPassword(this) ?: "",
                        mobile,
                        AppPrefsHelper.getGender(this)
                    )
                    
                    tvMobile.text = "Mobile: $mobile"
                    tvUserEmail.text = "Email: $email"
                } else {
                    Toast.makeText(this, "Invalid details", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        contactPickerLauncher.launch(intent)
    }

    private fun addContact(name: String, number: String) {
        contactsList.add(TrustedContact(name, number))
        contactsAdapter.notifyItemInserted(contactsList.size - 1)
        saveContacts()
    }

    private fun showDeleteContactDialog(contact: TrustedContact) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Contact")
            .setMessage("Remove ${contact.name} from trusted contacts?")
            .setPositiveButton("Delete") { _, _ ->
                val index = contactsList.indexOf(contact)
                if (index != -1) {
                    contactsList.removeAt(index)
                    contactsAdapter.notifyItemRemoved(index)
                    saveContacts()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSOSInfoDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About SOS")
            .setMessage("Your trusted contacts will be notified with your current location when you press the SOS button.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val etCurrent = view.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNew = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirm = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        MaterialAlertDialogBuilder(this)
            .setTitle("Change Password")
            .setView(view)
            .setPositiveButton("Save", null) // Set to null to override behavior
            .setNegativeButton("Cancel", null)
            .create().apply {
                show()
                getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val currentStored = AppPrefsHelper.getPassword(this@ProfileActivity)
                    val enteredCurrent = etCurrent.text.toString()
                    val newPass = etNew.text.toString()
                    val confirmPass = etConfirm.text.toString()

                    if (enteredCurrent != currentStored) {
                        etCurrent.error = "Incorrect current password"
                        return@setOnClickListener
                    }
                    if (newPass.isEmpty()) {
                        etNew.error = "New password cannot be empty"
                        return@setOnClickListener
                    }
                    if (newPass != confirmPass) {
                        etConfirm.error = "Passwords do not match"
                        return@setOnClickListener
                    }

                    // Save new password
                    AppPrefsHelper.saveUser(
                        this@ProfileActivity,
                        AppPrefsHelper.getName(this@ProfileActivity),
                        AppPrefsHelper.getEmail(this@ProfileActivity) ?: "",
                        newPass,
                        AppPrefsHelper.getMobile(this@ProfileActivity),
                        AppPrefsHelper.getGender(this@ProfileActivity)
                    )
                    Toast.makeText(this@ProfileActivity, "Password Changed Successfully", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
    }

    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                AppPrefsHelper.logout(this)
                Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                AppPrefsHelper.logout(this)
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSOSDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Emergency SOS")
            .setMessage("Are you in an emergency? This will alert your trusted contacts.")
            .setPositiveButton("YES, SOS") { _, _ ->
                checkPermissionsAndSendSOS()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun checkPermissionsAndSendSOS() {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            sendSOSWithLocation()
        } else {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 2001)
        }
    }

    private fun sendSOSWithLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val locationUrl = if (location != null) {
                "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
            } else {
                "Location unavailable"
            }
            
            val message = "EMERGENCY! I need help. My current location is: $locationUrl"
            sendSmsToContacts(message)
        }
    }

    private fun sendSmsToContacts(message: String) {
        if (contactsList.isEmpty()) {
            Toast.makeText(this, "No trusted contacts added!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val smsManager: SmsManager = this.getSystemService(SmsManager::class.java)
            for (contact in contactsList) {
                smsManager.sendTextMessage(contact.number, null, message, null, null)
            }
            Toast.makeText(this, "SOS Alert sent to ${contactsList.size} contacts!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                sendSOSWithLocation()
            } else {
                Toast.makeText(this, "Permissions required for SOS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class TrustedContactsAdapter(
        private val contacts: List<TrustedContact>,
        private val onDeleteClick: (TrustedContact) -> Unit
    ) : RecyclerView.Adapter<TrustedContactsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvContactName)
            val tvNumber: TextView = view.findViewById(R.id.tvContactNumber)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteContact)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trusted_contact, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contact = contacts[position]
            holder.tvName.text = contact.name
            holder.tvNumber.text = contact.number
            holder.btnDelete.setOnClickListener { onDeleteClick(contact) }
        }

        override fun getItemCount() = contacts.size
    }
}
