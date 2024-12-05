package com.example.lab11firebase.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab11firebase.R
import com.example.lab11firebase.model.Transaction
import com.example.qlchitieu.adapter.TransactionAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class TransactionFragment : Fragment(R.layout.fragment_transaction) {

    private lateinit var amountEditText: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ánh xạ view
        amountEditText = view.findViewById(R.id.amountEditText)
        typeSpinner = view.findViewById(R.id.typeSpinner)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        saveButton = view.findViewById(R.id.saveButton)
        transactionRecyclerView = view.findViewById(R.id.transactionsRecyclerView)

        // Khởi tạo RecyclerView và Adapter
        transactionAdapter = TransactionAdapter()
        transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        transactionRecyclerView.adapter = transactionAdapter

        // Khởi tạo Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference.child("transactions")

        // Thiết lập Spinner
        val transactionTypes = arrayOf("Thu nhập", "Chi tiêu")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            transactionTypes
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = spinnerAdapter

        // Xử lý khi nhấn nút lưu
        saveButton.setOnClickListener {
            saveTransaction()
        }

        // Tải dữ liệu từ Firebase
        loadTransactions()
    }

    private fun saveTransaction() {
        val amount = amountEditText.text.toString().toDoubleOrNull()
        val type = typeSpinner.selectedItem.toString()
        val description = descriptionEditText.text.toString()

        if (amount == null || amount <= 0) {
            // Báo lỗi nếu số tiền không hợp lệ
            amountEditText.error = "Vui lòng nhập số tiền hợp lệ"
            return
        }

        // Tạo đối tượng giao dịch
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            description = description,
            date = System.currentTimeMillis()
        )

        // Lưu vào Firebase
        database.child(transaction.id).setValue(transaction)
            .addOnSuccessListener {
                // Hiển thị thông báo thành công
                Toast.makeText(requireContext(), "Giao dịch đã được lưu", Toast.LENGTH_SHORT).show()
                amountEditText.text.clear()
                descriptionEditText.text.clear()
                typeSpinner.setSelection(0)
            }
            .addOnFailureListener {
                // Hiển thị lỗi khi lưu thất bại
                Toast.makeText(requireContext(), "Lưu giao dịch thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTransactions() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = mutableListOf<Transaction>()
                for (data in snapshot.children) {
                    val transaction = data.getValue(Transaction::class.java)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }
                // Cập nhật dữ liệu cho adapter
                transactionAdapter.updateTransactions(transactions)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Không thể tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
