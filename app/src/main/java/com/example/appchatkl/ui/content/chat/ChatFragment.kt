package com.example.appchatkl.ui.content.chat

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appchatkl.R
import com.example.appchatkl.commomFunction
import com.example.appchatkl.data.db.AppDatabase
import com.example.appchatkl.databinding.ChatFragmentBinding
import com.example.appchatkl.ui.content.BottomFragment
import com.example.appchatkl.ui.content.chat.adapter.ChatAdapter
import com.example.appchatkl.ui.content.listMessage.ListMessageViewModel
import com.example.appchatkl.ui.login.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log

class ChatFragment : Fragment() {
    val TAG = "ChatFragment"
    private var host = ""
    private lateinit var binding: ChatFragmentBinding


    companion object {
        fun newInstance() = ChatFragment()
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var viewModelLM: ListMessageViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.chat_fragment, container, false
        )
        return binding.root
    }

    var list = ArrayList<com.example.appchatkl.data.Message>()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        viewModelLM = ViewModelProvider(this).get(ListMessageViewModel::class.java)
        val chatDB: AppDatabase = AppDatabase.getDatabase(binding.root.context)
        binding.chat = viewModel
        val databse = Firebase.database.reference
        var auth: FirebaseAuth = Firebase.auth
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            host = commomFunction.getId(currentUser!!).toString()
        } else {
            chatDB.chatDao().loadSave().forEach {
                if (!it.id.equals("null")) {
                    host = it.id
                }
            }

        }

        val adapterBinding = ChatAdapter(host)
        binding.recyclerviewmessage.apply {
            adapter = adapterBinding
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL,
                false
            )

        }

        val id = arguments?.getString("id").toString()
        //   databse.child("conversation").child(id).child("idSee").setValue(host)
        adapterBinding.listConversation
        if (commomFunction.checkConnect(requireContext())) {
            viewModel.takeMessage(id, databse, list, host, chatDB)

        } else {
            viewModel.takeMessageOff(id, list, host, chatDB)
        }
        viewModel.takeMessage(id, databse, list, host, chatDB)
        viewModel.list.observe(viewLifecycleOwner, {
            adapterBinding.listConversation = it

            adapterBinding.notifyDataSetChanged()

        })

        viewModel.max.observe(viewLifecycleOwner, {
            binding.recyclerviewmessage.smoothScrollToPosition(
                it
            )
        })

        binding.back.setOnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        }
        viewModel.name.observe(viewLifecycleOwner, {
            binding.namefull.text = it

        })
        viewModel.avata.observe(viewLifecycleOwner, {
            Glide.with(this).load(it).placeholder(R.drawable.personal1).into(binding.imgAvataTop);
        })
        binding.edtInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.recyclerviewmessage.layoutManager?.scrollToPosition(
                    viewModel.max.value!!
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s == null) {
                    binding.imgSend.visibility = View.GONE

                } else {
                    binding.imgSend.visibility = View.VISIBLE
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        binding.imgSend.setOnClickListener {
            viewModel.Send(
                binding.edtInput.text.toString(),
                databse,
                viewModel.idName.value.toString(),
                host,
                chatDB
            )
        }
        viewModel.message.observe(viewLifecycleOwner, {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            val s = it + "@@@" + host + "@@@@" + currentDate + "@@@@@"
            databse.child("conversation").child(viewModel.idName.value.toString()).child("message")
                .setValue(s)
        })
        viewModel.count.observe(viewLifecycleOwner, {
            databse.child("conversation").child(viewModel.idName.value.toString()).child("count")
                .setValue(it.toString())
        })
    }

}