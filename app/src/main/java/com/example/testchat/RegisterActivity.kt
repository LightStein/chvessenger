package com.example.testchat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private val REQUESTCODE = 1
    private var mUploadBytes: ByteArray? = null
    private var uri: Uri? = null

    private var saveCurrentDate: String? = null
    private var saveCurrentTime: String? = null
    private var postRandomName: String? = null
    private var downloadUrl: String? = null

    private var username: String? = null
    private var email: String? = null
    private var password: String? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        progressDialog = ProgressDialog(this)

        button_register_register.setOnClickListener {
            if (uri == null)
                Toast.makeText(this,"გთხოვთ აირჩიოთ პროფილის სურათი",Toast.LENGTH_SHORT).show()
            else
                registerUser()
        }

        cv_register_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUESTCODE)
        }

        imageView_register_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUESTCODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK && data != null) {
            uri = data.data
            imageView_register_image.setImageURI(uri)
            //Compress image first
            val resize = BackGroundImageResize(null)
            resize.execute(uri)
        }
    }

    private fun registerUser() {
        username = editText_register_username.text.toString()
        email = editText_register_email.text.toString()
        password = editText_register_password.text.toString()

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this@RegisterActivity, "გთხოვთ შეიყვანოთ სახელი", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(this@RegisterActivity, "გთხოვთ შეიყვანოთ ელ-ფოსტა", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this@RegisterActivity, "გთხოვთ შეიყვანოთ პაროლი", Toast.LENGTH_SHORT).show()
        } else {
            progressDialog!!.setTitle("მიმდინარეობს რეგისტრაცია")
            progressDialog!!.setMessage("გთხოვთ დაიცადოთ...")
            progressDialog!!.show()
            progressDialog!!.setCanceledOnTouchOutside(true)

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "თქვენ წარმატებით დარეგისტრირდით", Toast.LENGTH_SHORT).show()
                        val uid: String = it.result!!.user.uid
                        //finally execute the upload task
                        storeByteImageToFirebaseStorage(uid)
                    } else {
                        progressDialog!!.dismiss()
                        Toast.makeText(this@RegisterActivity, "რეგისტრაცია ჩაიშალა", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    progressDialog!!.dismiss()
                    Log.d("REGISTER", "REGISTER::: User register on login button() failed! \n ${it.message}")
                }
        }
    }

    inner class BackGroundImageResize(bitmap: Bitmap?) : AsyncTask<Uri, Int, ByteArray>() {
        internal var mBitmap: Bitmap? = null

        init {
            if (bitmap != null) {
                this.mBitmap = bitmap
            }
        }

//        override fun onPreExecute() {
//            super.onPreExecute()
//            Toast.makeText(this@RegisterActivity, "Compressing Image", Toast.LENGTH_SHORT).show()
//        }

        override fun doInBackground(vararg uris: Uri): ByteArray? {
            if (mBitmap == null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uris[0])
                } catch (e: IOException) {
                    Log.d("REGISTER", "BackGroundImageResize::: სურათის კომპრესირებისას შეცდომა \n $e")
                }
            }
            //var bytes: ByteArray? = null
            var bytes = getBytesFromBitmap(mBitmap, 85)    //100,
            return bytes
        }

        override fun onPostExecute(bytes: ByteArray) {
            super.onPostExecute(bytes)
            mUploadBytes = bytes

            /*//finally execute the upload task
            storeByteImageToFirebaseStorage()*/
        }
    }

    fun getBytesFromBitmap(bitmap: Bitmap?, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    private fun storeByteImageToFirebaseStorage(uid: String) {
        val calForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MMMM-yyyy")
        saveCurrentDate = currentDate.format(calForDate.time)

        val calForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm")
        saveCurrentTime = currentTime.format(calForTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime
        val storageRef = FirebaseStorage.getInstance().reference
            .child("Profile Images")
            .child(uid + postRandomName + ".jpg")

        storageRef.putBytes(mUploadBytes!!)
            .addOnSuccessListener {
                Toast.makeText(this@RegisterActivity, "სურათი წარმატებით აიტვირთა", Toast.LENGTH_SHORT).show()
                storageRef.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location: $it")
                    savingUserInformationToDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                progressDialog!!.dismiss()
                Toast.makeText(this@RegisterActivity, "სურათის ატვირთვა ჩაიშალა", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savingUserInformationToDatabase(profilePicUrl: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val userMap = HashMap<String, Any>()
        userMap.put("username", username!!)
        userMap.put("profileimageUrl", profilePicUrl)
        userMap.put("fullname", "none")
        userMap.put("countryname", "none")
        userMap.put("status", "none")
        userMap.put("gender", "none")
        userMap.put("dob", "none")
        userMap.put("relationshipstatus", "none")

        if (currentUid != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference
                .child("Users")
                .child(currentUid)

            databaseRef.updateChildren(userMap)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "თქვენ წარმატებით დარეგისტრირდით!!", Toast.LENGTH_LONG).show()
                        sendUserToMainActivity()
                    } else {
                        progressDialog!!.dismiss()
                        Toast.makeText(this@RegisterActivity, "რეგისტრაცია ჩაიშალა!! \n $it", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@RegisterActivity, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(mainIntent)
        finish()
    }

}
