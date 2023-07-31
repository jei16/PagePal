package com.example.pagepal

import LendBooksInfoAdapter
import android.annotation.SuppressLint

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pagepal.databinding.FragmentBorrowPageBinding
import com.example.pagepal.models.LendBookData
import com.example.pagepal.models.SharedViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class BorrowFragment : Fragment(), LendBooksInfoAdapter.OnItemClickListener {
    private var _binding : FragmentBorrowPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var infoList: ArrayList<LendBookData>
    private lateinit var firebaseReference: DatabaseReference
    private val DETAILED_INFO_REQUEST_CODE = 100
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        infoList = arrayListOf()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBorrowPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.borrowingItems.observe(viewLifecycleOwner, Observer { items ->
            infoList.clear()
            infoList.addAll(items)
            binding.postedlendbooks.adapter?.notifyDataSetChanged()
        })


        binding.btnBorrow.setOnClickListener {
            val borrowPageContainer = requireActivity().findViewById<ViewGroup>(R.id.borrowPage)
            borrowPageContainer?.removeAllViews()

            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.borrowPage, LendingFragment())
            transaction.commit()
        }

        firebaseReference = FirebaseDatabase.getInstance().getReference("Lend Books Info")
        infoList = arrayListOf()

        setUpRecyclerView()
        fetchData()
    }

    private fun setUpRecyclerView() {
        binding.postedlendbooks.apply {
            setHasFixedSize(true)
            val layoutManager = GridLayoutManager(context, 2)
            binding.postedlendbooks.layoutManager = layoutManager
            val lendAdapter = LendBooksInfoAdapter(infoList, this@BorrowFragment)
            binding.postedlendbooks.adapter = lendAdapter
        }

    }

    private fun fetchData() {
        firebaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                infoList.clear()
                if (snapshot.exists()) {
                    for (lendbooksinfoSnap in snapshot.children) {
                        val lendbooksinfo = lendbooksinfoSnap.getValue(LendBookData::class.java)
                        lendbooksinfo?.let { infoList.add(it) }
                    }
                    binding.postedlendbooks.adapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        })

        val lendAdapter = LendBooksInfoAdapter(infoList, this)
        binding.postedlendbooks.adapter = lendAdapter
    }

    override fun onItemClick(item: LendBookData, position: Int) {
        val intent = Intent(requireContext(), DetailedInfoPosted::class.java)
        intent.putExtra("book_name", item.bookname)
        intent.putExtra("author", item.author)
        intent.putExtra("pubyr", item.pubyr)
        intent.putExtra("edition", item.edition)
        intent.putExtra("isbn", item.isbn)
        intent.putExtra("caption", item.caption)
        intent.putExtra("img_uri", item.imgUri)
        intent.putExtra("position", position)
        startActivityForResult(intent, DETAILED_INFO_REQUEST_CODE)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DETAILED_INFO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val position = data?.getIntExtra("position", -1)
            if (position != null && position != -1) {
                val removedItem = infoList.removeAt(position)
                // Check if binding is null before using it
                binding?.postedlendbooks?.adapter?.notifyItemRemoved(position)
                sharedViewModel.addRemovedItem(removedItem)
            }
        }
    }


}