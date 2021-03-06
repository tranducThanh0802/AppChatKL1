package com.example.appchatkl.ui.content.friend.friend

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appchatkl.R
import com.example.appchatkl.commomFunction
import com.example.appchatkl.data.User
import com.example.appchatkl.data.db.AppDatabase
import com.example.appchatkl.databinding.FriendFragmentBinding
import com.example.appchatkl.ui.content.friend.friend.adapter.FriendAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FriendFragment : Fragment() {
    lateinit var binding: FriendFragmentBinding
    lateinit var host: String
    val TAG = "FriendFragment"

    companion object {
        fun newInstance() = FriendFragment()
    }

    private lateinit var viewModel: FriendViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.friend_fragment, container, false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FriendViewModel::class.java)
        val database: DatabaseReference
        database = Firebase.database.reference
        var list = ArrayList<User>()
        var auth: FirebaseAuth = Firebase.auth
        val currentUser: FirebaseUser? = auth.currentUser
        val chatDB: AppDatabase = AppDatabase.getDatabase(binding.root.context)
        if (currentUser != null) {
            host = commomFunction.getId(currentUser!!).toString()
        } else {
            chatDB.chatDao().loadSave().forEach {
                if (!it.id.equals("null")) {
                    host = it.id
                }
            }

        }
        if (commomFunction.checkConnect(requireContext())) {
            viewModel.getAllUser(database, list, host, chatDB)
            Log.d(TAG, "onActivityCreated: 12")
        } else {
            viewModel.getAllUserOff(list, host, chatDB)
            Log.d(TAG, "onActivityCreated: 123")
        }
        viewModel.getAllUser(database, list, host, chatDB)
        val friendAdapter = FriendAdapter()
        binding.stickyListFriend.apply {
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL,
                false
            )
            setHasFixedSize(true)
        }
        viewModel.responseTvShow.observe(viewLifecycleOwner, {
            friendAdapter.listConversation = it
            binding.stickyListFriend.adapter?.notifyDataSetChanged()
        })
    }

}