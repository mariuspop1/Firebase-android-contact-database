    package fr.eurecom.android.firebaseintro;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

    public class MainActivity extends AppCompatActivity {
        final Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Contacts");
        final Button btn =(Button) findViewById(R.id.goButton1);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                ContentResolver cr= getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,null,null,null);
                myContact myContacts = new myContact();
                String[] projection = new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER

                };
                Cursor names = getContentResolver().query(uri,projection,null,null,null);
                int indexName =names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int indexNumber =names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                names.moveToFirst();
                while(names.moveToNext()){
                    myContacts.name= names.getString(indexName);
                    myContacts.phone=names.getString(indexNumber);
                    String key= myRef.push().getKey();
                    myRef.child(key).setValue(myContacts);
                }
                Toast.makeText(MainActivity.this, "Contacts added", Toast.LENGTH_LONG).show();
            }

        });
        final Button btn1 =(Button) findViewById(R.id.goButton3);
        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ContentResolver contentResolver =getContentResolver();
                Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    contentResolver.delete(uri, null, null);
                    Toast.makeText(MainActivity.this, "All the contacts have been deleted", Toast.LENGTH_LONG).show();
                }
                }
        });
        final Button btn2 =(Button) findViewById(R.id.goButton2);
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
                public void onClick(View v) {
                    final EditText contact_info = new EditText(context);
                    final myContact mycontact = new myContact();

                    contact_info.setHint("Name,Phone number");

                    new AlertDialog.Builder(context)
                            .setTitle("Add a new user to the database")
                            .setMessage("Use the following model :Name,Phone number")
                            .setView(contact_info)
                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String info = contact_info.getText().toString();
                                    String[] contact_data = info.split(",");
                                    mycontact.name = contact_data[0].trim();
                                    mycontact.phone = contact_data[1].trim();
                                    myRef.child(mycontact.name).setValue(mycontact);
                                    Toast.makeText(MainActivity.this, "Contact added successfully", Toast.LENGTH_LONG).show();
                                }
                            })
                            .show();

            }
        });
        final ListView listView =findViewById(R.id.contactList);
        final ArrayList<myContact> contactsArray = new ArrayList<>();
        final myContactAdapter adapter =new myContactAdapter(this,contactsArray);
        adapter.sort(new Comparator<myContact>() {
            @Override
            public int compare(myContact o1, myContact o2) {
                return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
            }
        });

        listView.setAdapter(adapter);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactsArray.clear();
                adapter.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    myContact read_contact = snapshot.getValue(myContact.class);
                    contactsArray.add(read_contact);
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
        private void deleteAllContacts(){
            ContentResolver contentResolver = getContentResolver();

            Uri rawUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add( ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).
                    withSelection(ContactsContract.RawContacts._ID + ">? "
                            ,new String[]{ "-1" }).build()); //sets deleted flag to 1

            ops.add(ContentProviderOperation.newDelete(rawUri).
                    withSelection(ContactsContract.RawContacts._ID + ">? "
                            ,new String[]{ "-1" }).build()); //erases

            try {
                contentResolver.applyBatch( ContactsContract.AUTHORITY, ops );
            } catch ( RemoteException e) {
                Log.d( "ContactsActivity", "RemoteException --> " + e );
                e.printStackTrace();
            } catch ( OperationApplicationException e) {
                Log.d( "ContactsActivity", "OperationApplicationException --> " + e );
                e.printStackTrace();
            }
        }

}

