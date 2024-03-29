package com.android.contacts

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.contacts.databinding.ContactListFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


@SuppressLint("InlinedApi")
private val PROJECTION: Array<out String> = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.LOOKUP_KEY,
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
    ContactsContract.Contacts.HAS_PHONE_NUMBER
)

var SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'"

class ContactsFragment : Fragment() {

    private lateinit var binding: ContactListFragmentBinding
    private val viewModel: ViewModelContacts by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launch(context = Dispatchers.Main) {
            delay(5000)

            val contact = viewModel.getTransferContact("+56974980000")
            contact?.let {
                binding.transfersContactName.visibility = View.VISIBLE
                binding.textNameContac.text = contact?.name
                binding.imageView.setImageBitmap(contact?.image)
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.contact_list_fragment, container, false)
        binding = DataBindingUtil.bind<ViewDataBinding>(view) as ContactListFragmentBinding
        binding.rvContacts.visibility = View.VISIBLE

        binding.buttonOpenConfiguration.setOnClickListener {

            openConfiguration()

        }
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        validatePermission()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            getAllContacts()
        }
    }

    private fun validatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
            getAllContacts()
        }
    }


    private fun getAllContacts() {
        binding.rvContacts.visibility = View.VISIBLE
        binding.contraintError.visibility = View.GONE

        val contactList: MutableList<ContactInfo> = ArrayList()

        val contentUri: Uri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_FILTER_URI,
            Uri.encode(SEARCH_STRING)
        )

        val cursor = requireContext().contentResolver.query(
            contentUri,
            PROJECTION,
            SELECTION,
            null,
            Phone.DISPLAY_NAME + " ASC"
        )


        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(DISPLAY_NAME))

                val number = retrieveContactNumber(id).replace(" ", "")
                val image = retrieveContactPhoto(id)

                contactList.add(ContactInfo(image, name, number))
            }
            val contactAdapter = ContactAdapter(contactList)
            binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
            binding.rvContacts.adapter = contactAdapter
            viewModel.livDataContacts.value = contactList
        }
        cursor.close()
    }

    private fun retrieveContactNumber(id: String): String {
        var phoneNumber = ""
        val phoneCursor = requireContext().contentResolver.query(
            Phone.CONTENT_URI,
            null,
            Phone.CONTACT_ID + " = ?",
            arrayOf(id),
            null
        )

        if (phoneCursor!!.moveToNext()) {
            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER))
        }

        phoneCursor.close()

        return phoneNumber
    }


    private fun retrieveContactPhoto(id: String): Bitmap? {
        var photo: Bitmap? = null
        try {
            val inputStream =
                ContactsContract.Contacts.openContactPhotoInputStream(
                    requireContext().contentResolver,
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
                )
            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream)
            }
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return photo
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllContacts()
            } else {
                binding.contraintError.visibility = View.VISIBLE
                binding.rvContacts.visibility = View.GONE
            }
        }
    }


    private fun openConfiguration() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:" + requireContext().packageName)
        requireActivity().startActivity(intent)
    }




    companion object {
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 100
        private const val DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME
        private const val SEARCH_STRING: String = "+569%"

    }
}