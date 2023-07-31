package com.example.pagepal

import android.app.Activity
import android.content.Context

import android.net.Uri
import java.io.File
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentTransaction
import com.example.pagepal.databinding.ActivityDetailedInfoPostedBinding
import com.example.pagepal.models.LendBookData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.FileNotFoundException


class DetailedInfoPosted : AppCompatActivity() {
    private lateinit var binding: ActivityDetailedInfoPostedBinding
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    companion object {
        private const val ARG_IMAGE_URI = "ARG_IMAGE_URI"

        // Factory method to create a new Intent to start LendBookActivity with the image URI argument
        fun newIntent(context: Context, imageUri: Uri?): Intent {
            val intent = Intent(context, DetailedInfoPosted::class.java)
            intent.putExtra(ARG_IMAGE_URI, imageUri)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedInfoPostedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseRef = FirebaseDatabase.getInstance().getReference("Requested Books Info")
        storageRef = FirebaseStorage.getInstance().getReference("Requested Books Images")



        val bookName = intent.getStringExtra("book_name")
        val author = intent.getStringExtra("author")
        val edition = intent.getStringExtra("edition")
        val pubyr = intent.getStringExtra("pubyr")
        val isbn = intent.getStringExtra("isbn")
        val caption = intent.getStringExtra("caption")
        val imgUri = intent.getStringExtra("img_uri")
        imageUri = Uri.parse(imgUri)

        // Now you can use these values to populate the views in the layout
        // For example, you can set their text values:
        binding.detailedBooktitle.text = bookName
        binding.detailedAuthor.text = author
        binding.detailedEdition.text = edition
        binding.detailedPubyr.text = pubyr
        binding.detailedIsbn.text = isbn
        binding.detailedCaption.text = caption

        // Load the book image using Picasso or any other image loading library
        Picasso.get().load(imgUri).into(binding.detailedBookimg)


        binding.detailedBorrow.setOnClickListener {

            SaveData()
        }
        binding.detailedMessage.setOnClickListener{

            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.detailedcontainer, PalFragment())
            fragmentTransaction.addToBackStack(null) // This allows the user to navigate back to the previous fragment
            fragmentTransaction.commit()
        }
    }

    private fun SaveData() {
        val bookname = binding.detailedBooktitle.text.toString()
        val author = binding.detailedAuthor.text.toString()
        val edition = binding.detailedEdition.text.toString()
        val pubyr = binding.detailedPubyr.text.toString()
        val isbn = binding.detailedIsbn.text.toString()
        val caption = binding.detailedCaption.text.toString()

        val lendbookId = firebaseRef.push().key!!
        val userId = getUserId()

        // No need to download the image and upload it again if it's already on Firebase Storage
        val imgUrl = imageUri?.toString() ?: ""

        val lendbook = LendBookData(
            lendbookId,
            bookname,
            author,
            edition,
            pubyr,
            isbn,
            caption,
            imgUrl,
            userId
        )

        firebaseRef.child(lendbookId).setValue(lendbook)
            .addOnCompleteListener {
                Toast.makeText(this, "Borrowed!", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent().apply {
                    putExtra("position", intent.getIntExtra("position", -1))
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Some error occurred ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


}





