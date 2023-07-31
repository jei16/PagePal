package com.example.pagepal.models
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {
    val removedItems = MutableLiveData<ArrayList<LendBookData>>(arrayListOf())
    val borrowingItems = MutableLiveData<ArrayList<LendBookData>>(arrayListOf())
    val lendingItems = MutableLiveData<ArrayList<LendBookData>>(arrayListOf())


    fun addRemovedItem(item: LendBookData) {
        val currentList = removedItems.value ?: arrayListOf()
        currentList.add(item)
        removedItems.value = currentList
    }

    fun addLendingItem(item: LendBookData) {
        removedItems.value?.add(item) // Assuming lendingItems is a MutableLiveData
        removedItems.postValue(removedItems.value)
    }

    fun moveItemToLending(position: Int) {
        val currentBorrowList = borrowingItems.value ?: arrayListOf()
        if (position >= 0 && position < currentBorrowList.size) {
            val item = currentBorrowList.removeAt(position)
            borrowingItems.value = currentBorrowList

            val currentLendingList = lendingItems.value ?: arrayListOf()
            currentLendingList.add(item)
            lendingItems.value = currentLendingList
        }
    }
}