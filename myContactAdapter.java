package fr.eurecom.android.firebaseintro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class myContactAdapter extends ArrayAdapter<myContact> {
    public myContactAdapter(@NonNull Context context, ArrayList<myContact> contacts){
        super(context, 0,contacts);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        myContact contact_person = getItem(position);
        if(convertView== null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_contact, parent, false);

        }
        TextView txtname= convertView.findViewById(R.id.name);
        TextView txtphone= convertView.findViewById(R.id.phone);
        txtname.setText(contact_person.name);
        txtphone.setText(contact_person.phone);
        return convertView;
    }
}
