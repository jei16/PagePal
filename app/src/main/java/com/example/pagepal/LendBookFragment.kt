package com.example.pagepal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import com.example.pagepal.LendingFragment
import androidx.lifecycle.ViewModelProvider
import com.example.pagepal.databinding.FragmentLendbookBinding
import com.example.pagepal.models.LendBookData
import com.example.pagepal.models.LendBookViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class LendBookFragment : Fragment() {
    private var _binding: FragmentLendbookBinding? = null
    private val binding get() = _binding!!


    private lateinit var firebaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    private lateinit var lendBookViewModel: LendBookViewModel

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    companion object {
        private const val ARG_IMAGE_URI = "ARG_IMAGE_URI"
        private const val IMAGE_PICKER_REQUEST_CODE = 1001
        // Factory method to create a new instance of LendBookFragment with the image URI argument
        fun newInstance(imageUri: Uri?): LendBookFragment {
            val fragment = LendBookFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLendbookBinding.inflate(inflater, container, false)

        firebaseRef = FirebaseDatabase.getInstance().getReference("Lend Books Info")
        storageRef = FirebaseStorage.getInstance().getReference("Lend Books Images")


        binding.btnBorrowMode.setOnClickListener {
            val postabookPageContainer =
                requireActivity().findViewById<ViewGroup>(R.id.post_a_book_lendingPage)


            postabookPageContainer?.removeAllViews()

            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.post_a_book_lendingPage, BorrowFragment())
            transaction.commit()
        }

        binding.imgOfbook.setOnClickListener {

            val postabookPageContainer = requireActivity().findViewById<ViewGroup>(R.id.post_a_book_lendingPage)


            postabookPageContainer?.removeAllViews()
            val imagePickerFragment = ImagePickerPABLendPageFragment()
            // Start the fragment with a request code
            imagePickerFragment.setTargetFragment(this, IMAGE_PICKER_REQUEST_CODE)
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.post_a_book_lendingPage, imagePickerFragment)
            transaction.commit()

        }

        binding.pickedimg.setOnClickListener {

            val postabookPageContainer = requireActivity().findViewById<ViewGroup>(R.id.post_a_book_lendingPage)


            postabookPageContainer?.removeAllViews()
            val imagePickerFragment = ImagePickerPABLendPageFragment()
            // Start the fragment with a request code
            imagePickerFragment.setTargetFragment(this, IMAGE_PICKER_REQUEST_CODE)
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.post_a_book_lendingPage, imagePickerFragment)
            transaction.commit()


        }



        binding.postbooklend.setOnClickListener {

            saveData()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lendBookViewModel = ViewModelProvider(requireActivity())[LendBookViewModel::class.java]

        // Retrieve the image URI from the arguments
        val imageUri = arguments?.getParcelable<Uri>(ARG_IMAGE_URI)

        // Check if the imageUri is not null and set it to the pickedimg ImageView
        imageUri?.let { uri ->
            binding.pickedimg.setImageURI(uri)
            ImageOfBook()
        }

        savedInstanceState?.let {
            lendBookViewModel.bookname = it.getString("bookname", "")
            lendBookViewModel.author = it.getString("author", "")
            lendBookViewModel.edition = it.getString("edition", "")
            lendBookViewModel.pubyr = it.getString("pubyr", "")
            lendBookViewModel.isbn = it.getString("pubyr", "")
            lendBookViewModel.caption = it.getString("pubyr", "")
        }

        binding.booktitle.setText(lendBookViewModel.bookname)
        binding.author.setText(lendBookViewModel.author)
        binding.edition.setText(lendBookViewModel.edition)
        binding.pubyr.setText(lendBookViewModel.pubyr)
        binding.isbn.setText(lendBookViewModel.isbn)
        binding.caption.setText(lendBookViewModel.caption)
    }

    private fun ImageOfBook() {
        binding.imgOfbook.visibility = View.INVISIBLE
        binding.imgOfbook.isClickable = true
        // Show the save button
    }

    private fun saveData() {
        val bookname = binding.booktitle.text.toString()
        val author = binding.author.text.toString()
        val edition = binding.edition.text.toString()
        val pubyr = binding.pubyr.text.toString()
        val isbn = binding.isbn.text.toString()
        val caption = binding.caption.text.toString()




        if (bookname.isEmpty() || author.isEmpty() || edition.isEmpty() || pubyr.isEmpty() || isbn.isEmpty())  {
            Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            return
        } else {

            val lendbookId = firebaseRef.push().key!!
            val userId = getUserId()
            var lendbook: LendBookData
            val imageUri = arguments?.getParcelable<Uri>(ARG_IMAGE_URI)
            if (imageUri != null) {
                imageUri.let {
                    storageRef.child(lendbookId).putFile(it)
                        .addOnSuccessListener { task ->
                            task.metadata!!.reference!!.downloadUrl
                                .addOnSuccessListener { url ->
                                    val imgUrl = url.toString()

                                    lendbook = LendBookData(
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
                                        .addOnSuccessListener() {
                                            Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT)
                                                .show()
                                            binding.booktitle.text = null
                                            binding.author.text = null
                                            binding.edition.text = null
                                            binding.pubyr.text = null
                                            binding.isbn.text = null
                                            binding.caption.text = null
                                            binding.imgOfbook.visibility = View.VISIBLE
                                            binding.pickedimg.setImageDrawable(null)

                                            // Clear lendBookViewModel fields
                                            lendBookViewModel.bookname = ""
                                            lendBookViewModel.author = ""
                                            lendBookViewModel.edition = ""
                                            lendBookViewModel.pubyr = ""
                                            lendBookViewModel.isbn = ""
                                            lendBookViewModel.caption = ""

                                            // Redirect to LendingFragment
                                            val lendbookPageContainer = binding.root.findViewById<ViewGroup>(R.id.post_a_book_lendingPage)
                                            lendbookPageContainer?.removeAllViews()
                                            val fragmentTransaction = requireFragmentManager().beginTransaction()
                                            fragmentTransaction.replace(R.id.post_a_book_lendingPage, LendingFragment())
                                            fragmentTransaction.commit()

                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Some error occured ${it.message}",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }

                                }

                        }

                }
            }else {
                Toast.makeText(context, "Please Upload an Image First!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Get the imageUri from the data returned by the ImagePickerPABLendPageFragment
            val imageUri = data?.getParcelableExtra<Uri>("imageUri")
            if (imageUri != null) {
                // Update the pickedimg ImageView with the selected image
                binding.pickedimg.setImageURI(imageUri)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the inputted text data to the bundle
        lendBookViewModel.bookname = binding.booktitle.text.toString()
        outState.putString("bookname", lendBookViewModel.bookname)
        lendBookViewModel.author = binding.author.text.toString()
        outState.putString("author", lendBookViewModel.author)
        lendBookViewModel.edition = binding.edition.text.toString()
        outState.putString("edition", lendBookViewModel.edition)
        lendBookViewModel.pubyr = binding.pubyr.text.toString()
        outState.putString("pubyr", lendBookViewModel.pubyr)
        lendBookViewModel.isbn = binding.isbn.text.toString()
        outState.putString("isbn", lendBookViewModel.isbn)
        lendBookViewModel.caption = binding.caption.text.toString()
        outState.putString("caption", lendBookViewModel.caption)
    }





}

